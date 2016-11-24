(ns photo-api.services.db.queries.photo-helpers
  (:require [clojure.string :as s]
            [photo-api.services.jwt :as jwt]))

(defn insert->filename [uuid version-id type ext]
  (let [nt (or type :full)]
    (str uuid "_" version-id "_" (name nt) "." ext)))

(defn result->filename [result]
  (if result
    (let [{uuid :uuid version-id :version_id ext :ext type :type} result]
      (insert->filename uuid version-id type ext))))

(defn filename->ext [filename]
  (-> filename
    (s/split #"\.")
    (last)))

(defn get-photo-filename
  ([id type photo] (get-photo-filename id type photo identity))
  ([id type photo check]
    (if (and photo (check photo))
      (result->filename (assoc photo :type type)))))
