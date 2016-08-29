(ns photo-api.api.v1.photos
  (:use compojure.core)
  (:require [photo-api.services.response :refer [->json ->img]]
            [photo-api.services.s3 :as s3]))

(defroutes core
  (GET "/test/:name" [name] (->img (s3/download name)))
  (DELETE "/test/:name" [name] (s3/delete! name) (->json {:message "success"}))
  (POST "/test" {{file "file" name "name"} :multipart-params}
    (println name)
    (s3/upload file)
    (->json {:message "success"} 201)))
