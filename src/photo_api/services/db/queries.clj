(ns photo-api.services.db.queries
  (:use photo-api.services.db korma.core)
  (:require [photo-api.services.db.helpers.folders :as hf]
            [photo-api.services.db.helpers.photos :as hp]))

(defn get-all-folders []
  (hf/parse-folders (select folders)))

(defn get-all-public-folders []
  (->> (get-all-folders)
    (hf/extract-folder 1)
    (:sub_folders)))

(defn get-folder [id]
  (let [folder (->> (select folders (where {:id id})) (first))
        photos (select photos (where {:folder_id id}))]
    (assoc folder :photos photos)))

(defn get-public-folder [id]
  (if (hf/is-public? id (select folders))
    (get-folder id)))

(defn get-photo-filename [id type]
  (->>
    (select photos
      (fields [:photos.id :id]
              [:photos.active_photo_id :version_id]
              [:photo_versions.file_extension :ext])
      (join photo_versions (= :photos.id :photo_versions.photo_id))
      (where {:photos.id id :photo_versions.id :photos.active_photo_id}))
    (first)
    (hp/result->filename)))

(defn get-public-photo-filename [id type]
  (get-photo-filename id type))

(defn save-new-photo! [taken-at name description folder-id]
  (fn [file]
    (let [filename (:filename file)
          ext (hp/filename->ext filename)
          new-id ((insert photos
                    (values {:name name
                             :description description
                             :taken_at taken-at
                             :folder_id folder-id})) :generated_key)
          version-id ((insert photo_versions
                        (values {:photo_id new-id
                                 :file_extension ext})) :generated_key)]
      (update photos
        (set-fields {:active_photo_id version-id})
        (where {:id new-id}))
      {:id new-id
       :name (hp/insert->filename new-id version-id :full ext)
       :file file
       :filename filename})))

(defn save-new-photos! [{taken-at "taken_at"
                        name "name"
                        description "description"
                        file "file"
                        folder-id "folder_id"}]
  (->> file
    (#(if (% 0) % (cons % '())))
    (map (save-new-photo! taken-at name description folder-id))))
