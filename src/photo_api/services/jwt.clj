(ns photo-api.services.jwt
  (:require [clj-jwt.core :refer :all]
            [clj-time.core :refer [now plus days minutes]]
            [clojure.string :as s]
            [environ.core :refer [env]]
            [clojure.data.json :as json]))

(defn verify-jwt [token]
  (if (> (count token) 0)
    (-> token
      (str->jwt)
      (verify (env :token-secret)))
    false))

(def decode str->jwt)

(defn token->data [jwt]
  (if (verify-jwt jwt)
    (:claims (decode jwt))))

(defn extract [jwt key]
  (->> jwt
    (token->data)
    (key)))

(defn encode [payload]
  (-> payload
    (jwt)
    (sign :HS256 (env :token-secret))
    (to-str)))

(defn check-headers [{headers :headers}]
  (->> "authorization"
    (headers)
    (str)
    (split-at 7)
    (last)
    (apply str)
    (#(if (empty? %) nil %))))

(defn check-query [{query :query}]
  (:access_token query))

(defn get-jwt [request]
  (or
    (check-headers request)
    (check-query request)
    ""))

(defn encode-data [data expiry]
  (->> expiry
    (plus (now))
    (assoc {:data data :iat (now)} :exp)
    (encode)))

(defn encode-temp [data]
  (->> (minutes 1)
    (encode-data data)))

(defn encode-extended [data]
  (->> (days 30)
    (encode-data data)))
