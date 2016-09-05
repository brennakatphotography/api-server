(ns photo-api.helpers.photo-helpers
  (:require [photo-api.services.db.queries :as db]))

(defn get-photo-or-data
  [id meta type {api :api err :err download :download img :img get-filename :get-filename}]
  (if meta
    (->> (db/get-photo id)
      (#(if %
        (api %)
        (err))))
    (->> (or type :full)
      (get-filename id)
      (#(if %
        (img (download %))
        (err))))))
