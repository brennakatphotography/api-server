(ns photo-api.services.images
  (:require [fivetonine.collage.core :as collage]
            [fivetonine.collage.util :as util]
            [clojure.java.io :refer [file resource]]))

(def ->img util/load-image)

(defn ->file [size ext id image]
  (util/save image (str "./tempfile-" size "-" id "." ext))
  (file (str "./tempfile-" size "-" id "." ext)))

(defn throttle-size [max-size image]
  (let [width (.getWidth image) height (.getHeight image)]
    (if (or (> width max-size) (> height max-size))
      (cond
        (> width height) (collage/resize image :width max-size)
        :else (collage/resize image :height max-size))
      image)))

(defn throttle [max-size ext id file]
  (->> file
    (->img)
    (throttle-size max-size)
    (->file max-size ext id)))
