(ns photo-api.auth
  (:use compojure.core)
  (:use ring.util.response)
  (:require [environ.core :refer [env]]
            [ring.util.codec :as codec]
            [clojure.data.json :as json]
            [base64-clj.core :as base64]
            [photo-api.services.jwt :as jwt]
            [clj-oauth2.client :as oauth2]
            [clj-http.client :as http]
            [photo-api.services.db.queries :as db]))

(def ^:private google-apis "https://www.googleapis.com")
(def ^:private google-accounts "https://accounts.google.com/o")

(defn oauth-config [{{host "host"} :headers scheme :scheme}]
  {:redirect-uri (str (name scheme) "://" host "/auth/callback")
   :client-id (env :google-client-id)
   :client-secret (env :google-client-secret)
   :scope (map #(str google-apis "/auth/userinfo." %) ["email" "profile"])
   :authorization-uri (str google-accounts "/oauth2/auth")
   :access-token-uri (str google-accounts "/oauth2/token")
   :access-query-param :access_token
   :grant-type "authorization_code"
   :access-type "online"
   :approval_prompt ""})

(defn construct-state [{query :query}]
  (codec/url-encode (:redirect_uri query)))

(defn deconstruct-state [state]
  (loop [url state]
    (if (re-find #"%" url)
      (recur (codec/url-decode url))
      url)))

(defn redirect-url [request]
  (->> request
    (construct-state)
    (oauth2/make-auth-request (oauth-config request))))

(defn parse-info [data]
  (let [email (:email (:body data)) access-level (or (db/get-user-role email) :customer)]
    {:email email :role access-level}))

(defn get-info [token]
  (->> token
    (:access-token)
    (assoc {} :access_token)
    (assoc {:as :json} :query-params)
    (http/get (str google-apis "/oauth2/v1/tokeninfo"))))

(defn send-token [state token]
  (redirect (str (deconstruct-state state) "?token=" token)))

(defn callback [request]
  (let [state (:state (:query request))]
    (->> request
      (:query)
      (oauth2/get-access-token (oauth-config request))
      (get-info)
      (parse-info)
      (jwt/encode-extended)
      (send-token state))))

(defroutes core
  (GET "/login" request (redirect (:uri (redirect-url request))))
  (GET "/callback" request (callback request)))
