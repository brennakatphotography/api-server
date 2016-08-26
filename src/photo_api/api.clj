(ns photo-api.api
  (:use compojure.core)
  (:require [photo-api.api.v1 :as v1]))

(defroutes core
  (context "/v1" [] v1/core))
