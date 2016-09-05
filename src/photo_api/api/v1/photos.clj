(ns photo-api.api.v1.photos
  (:use compojure.core)
  (:require [clojure.walk :refer [keywordize-keys]]
            [clojure.java.io :as io]
            [photo-api.services.response :as >>>]
            [photo-api.services.s3 :as s3]
            [photo-api.services.db.queries :as db]
            [photo-api.services.logger :as log]
            [photo-api.services.images :as img]))

(def fns {:api >>>/api :err >>>/err :img >>>/img :download s3/download})

(defroutes unauthed
  (GET "/:id" [id meta type]
    (db/get-photo-or-data id meta type (assoc fns :get-filename db/get-public-photo-filename)))
  (POST "/" [] (>>>/unauthorized))
  (DELETE "/:id" [] (>>>/unauthorized)))

(defroutes authed
  (GET "/:id" [id meta type]
    (db/get-photo-or-data id meta type (assoc fns :get-filename db/get-photo-filename)))
  (POST "/" {data :multipart-params}
    (->> data
      (keywordize-keys)
      (db/save-new-photos!)
      (map #(let [{id :id filename :filename file :file name-maker :name-maker ext :ext tmp :tmp} %]
        (s3/upload! file (name-maker :full))
        (s3/upload! file (name-maker :preview) (partial img/throttle 720 ext id))
        (s3/upload! file (name-maker :small) (partial img/throttle 400 ext id))
        (s3/upload! file (name-maker :thumbnail) (partial img/throttle 120 ext id))
        (io/delete-file (str "./tempfile-" 720 "-" id "." ext))
        (io/delete-file (str "./tempfile-" 400 "-" id "." ext))
        (io/delete-file (str "./tempfile-" 120 "-" id "." ext))
        {:id id :filename filename :url (str "/api/v1/photos/" id)}))
      (#(>>>/api % {:message "Photo(s) saved" :status 201}))))
  (PUT "/:id" {{id :id} :params data :multipart-params}
    (->> data
      (keywordize-keys)
      (db/update-photo! id)
    (>>>/api {:message "Photo updated."}))))
  ; (DELETE "/:id" [id]
  ;   (->> :full
  ;     (db/delete-photo! id)
  ;     (s3/delete!))
  ;   (>>>/api {:message "Photo deleted."})))
