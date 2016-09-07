(ns photo-api.services.auth
  (:require [photo-api.services.jwt :as jwt]
            [environ.core :refer [env]]))

(defn authenticate [app]
  (fn [request]
    (let [jwt (jwt/get-jwt request)]
      (->> {:role (jwt/extract jwt (comp :role :data))
            :email (jwt/extract jwt (comp :email :data))
            :verified (jwt/verify-jwt jwt)}
        (assoc request :auth)
        (app)))))

(defn get-role [request]
  (->> request
    (:auth)
    (:role)
    (keyword)))

(defn check-role [request allowed]
  (->> request
    (get-role)
    (contains? allowed)))
