(ns photo-api.services.auth
  (:require [photo-api.services.jwt :as jwt]
            [environ.core :refer [env]]))

(defn authenticate [app]
  (fn [request]
    (->> request
      (:headers)
      (jwt/get-jwt)
      (jwt/verify-jwt)
      (or (env :bypass-auth))
      (assoc request :authenticated?)
      (app))))
