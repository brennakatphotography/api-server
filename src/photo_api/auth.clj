(ns photo-api.auth
  (:use compojure.core)
  (:use ring.util.response)
  (:require [environ.core :refer [env]]
            [ring.util.codec :as codec]
            [clojure.data.json :as json]
            [base64-clj.core :as base64]
            [photo-api.services.jwt :as jwt]
            [clj-oauth2.client :as oauth2]
            [clj-http.client :as http]))

(defn oauth-config [{{host "host"} :headers scheme :scheme}]
  {:redirect-uri (str (name scheme) "://" host "/auth/google/callback")
   :client-id (env :google-client-id)
   :client-secret (env :google-client-secret)
   :scope ["https://www.googleapis.com/auth/userinfo.email" "https://www.googleapis.com/auth/userinfo.profile"]
   :authorization-uri "https://accounts.google.com/o/oauth2/auth"
   :access-token-uri "https://accounts.google.com/o/oauth2/token"
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
  (let [email (:email (:body data)) admin (env :admin-email) dev (env :dev-email)
        access-level (cond
                       (= email admin) :admin
                       (and (= email dev) (env :dev-token)) :muck-about
                       :else :customer)]
    {:email email :role access-level}))

(defn get-info [token]
  (->> token
    (:access-token)
    (assoc {} :access_token)
    (assoc {:as :json} :query-params)
    (http/get "https://www.googleapis.com/oauth2/v1/tokeninfo")))

(defn send-token [state token]
  (redirect (str (deconstruct-state state) "&token=" token)))

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
  (GET "/google/login" request (redirect (:uri (redirect-url request))))
  (GET "/google/callback" request (callback request)))
