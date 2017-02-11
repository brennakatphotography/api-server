(ns photo-api.services.db.queries.db-calls
  (:use photo-api.services.db)
  (:require [clj-time.core :refer [now]]
            [clj-time.coerce :refer [to-sql-time]]
            [korma.core :as db]))

(defn stamp [data]
  (assoc data :updated_at (to-sql-time (now))))

;FOLDER QUERIES
(defn get-all-folders []
  (db/select :folders))

(defn get-folder [id]
  (->>
    (db/select :folders
      (db/where {:id id}))
    (first)))

(defn get-folder-by-name [name]
  (->>
    (db/select :folders
      (db/where {:name (.toUpperCase name)}))
    (first)))

(defn insert-folder! [data]
  (->>
    (db/insert :folders
      (db/values data))
    (:generated_key)))

(defn update-folder! [id data]
  (db/update :folders
    (db/set-fields data)
    (db/where {:id id})))

(defn delete-folder! [id]
  (db/delete :folders
    (db/where {:id id})))

;PHOTOS QUERIES
(defn get-photo [id]
  (->>
    (db/select :photos
      (db/fields [:photos.id :id]
                 [:photos.active_photo_id :version_id]
                 [:photo_versions.file_extension :ext]
                 [:photos.folder_id :folder_id]
                 [:photo_versions.uploaded_at :uploaded_at]
                 [:photos.updated_at :updated_at]
                 [:photos.created_at :created_at]
                 [:photo_files.uuid :uuid])
      (db/join :photo_versions (= :photos.id :photo_versions.photo_id))
      (db/join :photo_files (= :photos.photo_file_id :photo_files.id))
      (db/where {:photos.id id :photo_versions.id :photos.active_photo_id}))
    (first)))

(defn get-photo-with-uuid [id]
  (->>
    (db/select :photos
      (db/fields [:photos.id :id]
                 [:photos.active_photo_id :version_id]
                 [:photos.folder_id :folder_id]
                 [:photos.updated_at :updated_at]
                 [:photos.created_at :created_at]
                 [:photo_files.uuid :uuid])
      (db/join :photo_files (= :photos.photo_file_id :photo_files.id))
      (db/where {:photos.id id}))
    (first)))

(defn get-photos-in-folder [id]
  (db/select :photos
    (db/where {:folder_id id})))

(defn insert-photo! [name description taken-at folder-id]
  (let [photo-file-id (->>
                        (db/insert :photo_files
                          (db/values {:uuid nil}))
                        (:generated_key))]
    (->>
      (db/insert :photos
        (db/values {:name name
                    :description description
                    :taken_at taken-at
                    :folder_id folder-id
                    :photo_file_id photo-file-id}))
      (:generated_key)
      (get-photo-with-uuid))))

(defn insert-photo-version! [photo-id ext]
  (->>
    (db/insert :photo_versions
      (db/values {:photo_id photo-id :file_extension ext}))
    (:generated_key)))

(defn update-photo! [id updates]
  (db/update :photos
    (db/set-fields updates)
    (db/where {:id id})))

(defn delete-photo! [id]
  (db/delete :photos
    (db/where {:id id})))

; MISC DB CALLS
(defn get-user-role [email]
  (->>
    (db/select :user_roles
      (db/where {:email email}))
    (first)
    (:role)))