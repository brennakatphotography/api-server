(ns photo-api.bin.v1
  (:use compojure.core)
  (:require [photo-api.bin.v1.photos :as photos]
            [photo-api.services.auth :as auth]
            [environ.core :refer [env]]))

(defroutes core
  (context "/photos" request
    (if (auth/check-role request #{:admin :muck-about})
      photos/authed
      photos/unauthed)))
