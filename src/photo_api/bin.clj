(ns photo-api.bin
  (:use compojure.core)
  (:require [photo-api.bin.v1 :as v1]))

(defroutes core
  (context "/v1" [] v1/core))
