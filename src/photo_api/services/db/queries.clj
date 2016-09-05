(ns photo-api.services.db.queries
  (:use photo-api.services.db)
  (:require [clj-time.core :refer [now]]
            [clj-time.coerce :refer [to-sql-time]]
            [korma.core :as db]
            [photo-api.services.db.helpers.folders :as hf]
            [photo-api.services.db.helpers.photos :as hp]))

(defn get-all-folders []
  (hf/parse-folders (db/select :folders)))

(defn get-all-subfolders [id]
  (->> (get-all-folders)
    (hf/extract-folder id)
    (:sub_folders)))

(defn get-all-public-folders []
  (get-all-subfolders 1))

(defn get-photos-in-folder [id]
  (db/select :photos (db/where {:folder_id id})))

(defn get-root-folder []
  {:name "ROOT FOLDER" :sub_folders (get-all-folders) :photos (get-photos-in-folder nil)})

(defn get-folder [id]
  (let [folder (->> (db/select :folders (db/where {:id id})) (first))
        photos (get-photos-in-folder id)
        sub-folders (get-all-subfolders id)]
    (assoc folder :photos photos :sub_folders (or sub-folders []))))

(defn get-public-folder [id]
  (if (hf/is-public? id (db/select :folders))
    (get-folder id)))

(defn get-photo [id]
  (->>
    (db/select :photos
      (db/fields [:photos.id :id]
              [:photos.active_photo_id :version_id]
              [:photo_versions.file_extension :ext]
              [:photos.folder_id :folder_id]
              [:photo_versions.uploaded_at :uploaded_at]
              [:photos.updated_at :updated_at]
              [:photos.created_at :created_at])
      (db/join :photo_versions (= :photos.id :photo_versions.photo_id))
      (db/where {:photos.id id :photo_versions.id :photos.active_photo_id}))
    (first)))

(defn get-photo-filename
  ([id type] (get-photo-filename id type (get-photo id)))
  ([id type photo]
    (hp/get-photo-filename id type photo)))

(defn get-public-photo-filename
  ([id type] (get-public-photo-filename id type (get-photo id)))
  ([id type photo]
    (hp/get-photo-filename id type photo (comp get-public-folder :folder_id))))

(defn save-new-photo! [taken-at name description folder-id]
  (fn [file]
    (let [filename (:filename file)
          ext (hp/filename->ext filename)
          new-id ((db/insert :photos
                    (db/values {:name name
                             :description description
                             :taken_at taken-at
                             :folder_id folder-id})) :generated_key)
          ver-id ((db/insert :photo_versions
                    (db/values {:photo_id new-id
                             :file_extension ext})) :generated_key)
          name-maker #(hp/insert->filename new-id ver-id % ext)]
      (db/update :photos
        (db/set-fields {:active_photo_id ver-id})
        (db/where {:id new-id}))
      {:id new-id :name-maker name-maker :file file :filename filename :ext ext})))

(defn save-new-photos!
  [{taken-at :taken_at name :name description :description file :file folder-id :folder_id}]
  (if file
    (->> file
      (#(if (% 0) % (cons % '())))
      (map (save-new-photo! taken-at name description folder-id)))))

(defn delete-photo! [id type]
  (let [filename (get-photo-filename id type)]
    (db/delete :photos
      (db/where {:id id}))
    filename))

(defn update-photo! [id data]
  (db/update :photos
    (db/set-fields (assoc data :updated_at (to-sql-time (now))))
    (db/where {:id id})))

(defn get-photo-or-data
  [id meta type {api :api err :err download :download img :img get-filename :get-filename}]
  (if meta
    (->> (get-photo id)
      (#(if %
        (api %)
        (err))))
    (->> (or type :full)
      (get-filename id)
      (#(if %
        (img (download %))
        (err))))))

(defn save-new-folder! [data]
  (->>
    (db/insert :folders
      (db/values data))
    (:generated_key)
    (assoc {} :id)))

(defn update-folder! [id data]
  (->>
    (db/update :folders
      (db/set-fields (assoc data :updated_at (to-sql-time (now))))
      (db/where {:id id}))))

(defn delete-folder! [id]
  (->>
    (db/delete :folders
      (db/where {:id id}))))
