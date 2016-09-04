(ns photo-api.api.v1.photos
  (:use compojure.core)
  (:require [photo-api.services.response :as >>>]
            [photo-api.services.s3 :as s3]
            [photo-api.services.db.queries :as db]
            [photo-api.services.logger :as log]))

(defroutes unauthed
  (GET "/:id" [id meta type]
    (db/get-photo-or-data >>> s3 db/get-public-photo-filename id meta type))
  (POST "/" [] (>>>/unauthorized))
  (DELETE "/:id" [] (>>>/unauthorized)))

(defroutes authed
  (GET "/:id" [id meta type]
    (db/get-photo-or-data >>> s3 db/get-photo-filename id meta type))
  (POST "/" {data :multipart-params}
    (->> data
      (db/save-new-photos!)
      (map (fn [{id :id filename :filename file :file name :name}]
        (s3/upload! file name)
        {:id id :filename filename :url (str "/api/v1/photos/" id)}))
      (#(>>>/api % {:message "Photo(s) saved" :status 201}))))
  (DELETE "/:id" [id]
    (->> :full
      (db/delete-photo! id)
      (s3/delete!))
    (>>>/api {:message "Photo deleted."})))
