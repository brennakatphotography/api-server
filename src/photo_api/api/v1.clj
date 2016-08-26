(ns photo-api.api.v1
  (:use compojure.core)
  (:require [photo-api.api.v1.photos :as photos]))

(defroutes core
  (context "/photos" [] photos/core))
