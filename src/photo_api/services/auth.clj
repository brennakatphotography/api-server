(ns photo-api.services.auth
  (:require [photo-api.services.jwt :as jwt]
            [environ.core :refer [env]]))

(defn ^:private auth-message [auth response]
  (let [headers (:headers response)]
    (if (= (headers "Content-Type") "application/json")
      (update-in response [:body :messages] #(assoc % :authenticated auth))
      response)))

(defn authenticate [app]
  (fn [request]
    (let [jwt (jwt/get-jwt request)
          verified (jwt/verify-jwt jwt)
          auth {:role (jwt/extract jwt (comp :role :data))
                :email (jwt/extract jwt (comp :email :data))
                :verified verified}]
      (->> auth
        (assoc request :auth)
        (app)
        (auth-message auth)))))

(defn get-role [request]
  (->> request
    (:auth)
    (:role)
    (keyword)))

(defn check-role [request allowed]
  (->> request
    (get-role)
    (contains? allowed)))
