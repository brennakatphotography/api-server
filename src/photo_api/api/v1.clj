(ns photo-api.api.v1
  (:use compojure.core)
  (:require [photo-api.api.v1.folders :as folders]
            [photo-api.api.v1.photos :as photos]
            [photo-api.services.auth :as auth]
            [environ.core :refer [env]]))

(defroutes core
  (context "/folders" request
    (if (auth/check-role request #{:admin :power-user})
      folders/authed
      folders/unauthed))
  (context "/photos" request
    (if (auth/check-role request #{:admin :power-user})
      photos/authed
      photos/unauthed)))
