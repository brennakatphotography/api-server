(ns photo-api.api.v1
  (:use compojure.core)
  (:require [photo-api.api.v1.folders :as folders]
            [photo-api.api.v1.photos :as photos]))

(defroutes core
  (GET "/token" []
    {:status 200 :body {:success true :data {:access_token (env :dev-token)}}})
  (context "/folders" request
    (if (:authenticated? request)
      folders/authed
      folders/unauthed))
  (context "/photos" request
    (if (:authenticated? request)
      photos/authed
      photos/unauthed)))
