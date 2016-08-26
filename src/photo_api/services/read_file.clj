(ns photo-api.services.read-file
  (:require [clojure.data.json :as json]))

(defn file->json [file]
  (->> file
    (slurp)
    (json/read-str)
    (assoc {} :body)))
