(ns photo-api.api.v1
  (:use compojure.core)
  (:require [photo-api.api.v1.folders :as folders]
            [photo-api.api.v1.photos :as photos]))

(defroutes core
  (context "/folders" [] folders/core)
  (context "/photos" [] photos/core))
