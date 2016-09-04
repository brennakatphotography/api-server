(ns photo-api.services.db.helpers.folders)

(defn ->int [val]
  (try (Integer. val)
    (catch Exception e nil)))

(defn has-children? [folder folders]
  (or
    (nil? (:parent_folder_id folder))
    (some #(= (:parent_folder_id %) (:id folder)) folders)))

(defn get-childless [folders]
  (reduce (fn [{childless :childless other :other} folder]
    (if (has-children? folder folders)
      {:childless childless :other (cons folder other)}
      {:childless (cons folder childless) :other other}))
    {:childless '() :others '()} folders))

(defn insert-children [folders orphans]
  (map (fn [folder]
    (let [sub-folders (:sub_folders folder)
          children (filter #(= (:parent_folder_id %) (:id folder)) orphans)]
      (assoc folder :sub_folders (into sub-folders children)))) folders))

(defn init-children [folder]
  (assoc folder :sub_folders '()))

(defn parse-folders [folders]
  (loop [{childless :childless other :other} (get-childless (map init-children folders))]
    (if (or (empty? childless) (every? #(nil? (:parent_folder_id %)) childless))
      (into childless other)
      (recur (get-childless (insert-children other childless))))))

(defn extract-folder [id folders]
  (->> folders
    (filter #(= (Integer. id) (:id %)))
    (first)))

(defn is-public? [id folders]
  (let [folder (extract-folder id folders)
        parent-id (:parent_folder_id folder)]
    (or
      (= (->int id) 1)
      (= (->int parent-id) 1)
      (and parent-id (is-public? parent-id folders)))))
