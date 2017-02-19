(ns photo-api.api.v1.photos
  (:use compojure.core)
  (:require [clojure.walk :refer [keywordize-keys]]
            [photo-api.services.response :as >>>]
            [photo-api.services.s3 :as s3]
            [photo-api.services.db.queries :as db]
            [photo-api.helpers.photo-helpers :as help]
            [photo-api.services.logger :as log]))

(defroutes unauthed
  (GET "/:id" [id]
    (->> id
      ;TODO -> public only
      (db/get-photo)
      (>>>/api)))
  (POST "/" [] (>>>/unauthorized))
  (DELETE "/:id" [] (>>>/unauthorized)))

(defroutes authed
  (GET "/:id" [id]
    (->> id
      (db/get-photo)
      (>>>/api)))
  (POST "/" {data :multipart-params}
    (try
      (->> data
        (keywordize-keys)
        (db/save-new-photos!)
        (map s3/map-uploads!)
        (#(>>>/api % {:message "Photo(s) saved" :status 201})))
      (catch Exception e
        (>>>/err "Unable to upload file" {:status 500}))))
  (PATCH "/:id" {{id :id} :params data :multipart-params}
    (->> data
      (keywordize-keys)
      (db/update-photo! id))
    (>>>/api nil {:message "Photo updated."})))
  ; (DELETE "/:id" [id]
  ;   (->> :full
  ;     (db/delete-photo! id)
  ;     (s3/delete!))
  ;   (>>>/api nil {:message "Photo deleted."})))
