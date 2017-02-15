(ns photo-api.services.error
  (:require [photo-api.services.response :as >>>]
            [photo-api.services.logger :as log]))

(defn handle-error [app]
  (fn [request]
    (try (app request)
      (catch Exception e
        (println e)
        (>>>/err "An unknown error occurred" {:status 500})))))
    
    