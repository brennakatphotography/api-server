(ns photo-api.services.db.queries
  (:use photo-api.services.db)
  (:require [photo-api.services.db.queries.db-calls :as db]
            [photo-api.services.db.queries.folder-helpers :as hf]
            [photo-api.services.db.queries.photo-helpers :as hp]))

(defn get-all-folders []
  (hf/parse-folders (db/get-all-folders)))

(defn get-all-subfolders [id]
  (->> (get-all-folders)
    (hf/extract-folder id)
    (:sub_folders)))

(defn get-all-public-folders []
  (get-all-subfolders 1))

(def get-photos-in-folder db/get-photos-in-folder)

(defn get-root-folder []
  {:name "ROOT FOLDER" :sub_folders (get-all-folders) :photos (get-photos-in-folder nil)})

(defn build-folder [folder]
  (let [id (:id folder)
        photos (get-photos-in-folder id)
        sub-folders (get-all-subfolders id)]
    (assoc folder :photos photos :sub_folders (or sub-folders []))))

(defn get-folder-by-name [name]
  (let [folder (db/get-folder-by-name name)]
    (if folder
      (build-folder folder)
      {:photos [] :sub_folders []})))

(defn get-public-folder-by-name [name]
  (let [folder (db/get-folder-by-name name)]
    (if (and
          folder
          (hf/is-public? (:id folder) (db/get-all-folders)))
      (build-folder folder)
      {:photos [] :sub_folders []})))

(defn get-folder [id]
  (->> id
    (db/get-folder)
    (build-folder)))

(defn get-public-folder [id]
  (if (hf/is-public? id (db/get-all-folders))
    (get-folder id)))

(def get-photo db/get-photo)

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
          new-id (db/insert-photo! name description taken-at folder-id)
          ver-id (db/insert-photo-version! new-id ext)
          name-maker #(hp/insert->filename new-id ver-id % ext)]
      (db/update-photo! new-id {:active_photo_id ver-id})
      {:id new-id :name-maker name-maker :file file :filename filename :ext ext})))

(defn save-new-photos!
  [{taken-at :taken_at name :name description :description file :file folder-id :folder_id}]
  (if file
    (->> file
      (#(if (% 0) % (cons % '())))
      (map (save-new-photo! taken-at name description folder-id)))))

(defn delete-photo! [id type]
  (let [filename (get-photo-filename id type)]
    (db/delete-photo! id)
    filename))

(defn update-photo! [id data]
  (db/update-photo! id (db/stamp data)))

(defn save-new-folder! [data]
  {:id (db/insert-folder! data)})

(defn update-folder! [id data]
  (db/update-folder! id (db/stamp data)))

(defn delete-folder! [id]
  (db/delete-folder! id))
