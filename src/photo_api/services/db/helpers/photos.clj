(ns photo-api.services.db.helpers.photos
  (:require [clojure.string :as s]))

(defn insert->filename [id version-id type ext]
  (let [nt (or type :full)]
    (str id "_" version-id "_" (name nt) "." ext)))

(defn result->filename [result]
  (if result
    (let [{id :id version-id :version_id ext :ext type :type} result]
      (insert->filename id version-id type ext))))

(defn filename->ext [filename]
  (-> filename
    (s/split #"\.")
    (last)))
