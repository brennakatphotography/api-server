(ns photo-api.services.jwt
  (:require [clj-jwt.core :refer :all]
            [clj-time.core :refer [now plus days]]
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

(defn encode [payload]
  (-> payload
    (jwt)
    (sign :HS256 (env :token-secret))
    (to-str)))

(defn get-jwt [headers]
  (->> "authorization"
    (headers)
    (#(or % ""))
    (split-at 7)
    (last)
    (apply str)))

(defn encode-data [data expiry]
  (->> (days expiry)
    (plus (now))
    (assoc {:data data :iat (now)} :exp)
    (encode)))

(defn token->data [jwt]
  (if (verify-jwt jwt)
    (:claims (decode jwt))))
