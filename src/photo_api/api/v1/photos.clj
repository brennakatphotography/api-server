(ns photo-api.api.v1.photos
  (:use compojure.core)
  (:require [clojure.walk :refer [keywordize-keys]]
            [photo-api.services.response :as >>>]
            [photo-api.services.s3 :as s3]
            [photo-api.services.db.queries :as db]
            [photo-api.helpers.photo-helpers :as help]
            [photo-api.services.logger :as log]))

(def fns {:api >>>/api :err >>>/err :img >>>/img :download s3/download})

(defroutes unauthed
  (GET "/:id" [id meta type]
    (let [pub-type (if (or (nil? type) (= type :full)) :large type)]
      (help/get-photo-or-data id meta pub-type (assoc fns :get-filename db/get-public-photo-filename))))
  (POST "/" [] (>>>/unauthorized))
  (DELETE "/:id" [] (>>>/unauthorized)))

(defroutes authed
  (GET "/:id" [id meta type]
    (help/get-photo-or-data id meta type (assoc fns :get-filename db/get-photo-filename)))
  (POST "/" {data :multipart-params}
    (->> data
      (keywordize-keys)
      (db/save-new-photos!)
      (map s3/map-uploads!)
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
