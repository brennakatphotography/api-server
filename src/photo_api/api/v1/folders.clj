(ns photo-api.api.v1.folders
  (:use compojure.core)
  (:require [clojure.walk :refer [keywordize-keys]]
            [photo-api.services.response :as >>>]
            [photo-api.services.db.queries :as db]
            [photo-api.services.logger :as log]))

(defroutes shared
  (GET "/public" [] (>>>/api (db/get-public-folder 1))))

(defroutes unauthed
  shared
  (GET "/" [name]
    (if name
      (>>>/api (db/get-public-folder-by-name name))
      (>>>/api (db/get-all-public-folders))))
  (GET "/:id" [id] (>>>/api (db/get-public-folder id))))

(defroutes authed
  shared
  (GET "/" [name]
    (if name
      (>>>/api (db/get-folder-by-name name))
      (>>>/api (db/get-all-folders))))
  (GET "/trash" [] (>>>/api (db/get-folder 2)))
  (GET "/root" [] (>>>/api (db/get-root-folder)))
  (GET "/:id" [id] (>>>/api (db/get-folder id)))
  (POST "/" {data :multipart-params}
    (->> data
      (keywordize-keys)
      (db/save-new-folder!)
      (>>>/api)))
  (PUT "/:id" {data :multipart-params {id :id} :params}
    (->> data
      (keywordize-keys)
      (db/update-folder! id))
    (>>>/json {:success true :message "Folder updated"})))
  ; (DELETE "/:id" [id]
  ;   (db/delete-folder! id)
  ;   (>>>/json {:success true :message "Folder deleted"})))
