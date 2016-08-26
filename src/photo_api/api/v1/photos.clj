(ns photo-api.api.v1.photos
  (:use compojure.core)
  (:require [photo-api.services.response :refer [->response]]))

(defroutes core
  (GET "/test" [] (->response {:test "successful"})))
