(ns photo-api.services.images
  (:require [fivetonine.collage.core :as collage]
            [fivetonine.collage.util :as util]
            [clojure.java.io :refer [file resource]]))

(def sizes [{:type :full :size nil}
            {:type :large :size 720}
            {:type :small :size 500}
            {:type :thumbnail :size 120}])

(defn tempfile [size id ext]
  (str "./tempfile-" size "-" id "." ext))

(def ->img util/load-image)

(defn ->file! [size ext id image]
  (let [tmp (tempfile size id ext)]
    (util/save image tmp)
    (file tmp)))

(defn throttle-size [max-size image]
  (let [width (.getWidth image) height (.getHeight image)]
    (if (or (> width max-size) (> height max-size))
      (cond
        (> width height) (collage/resize image :width max-size)
        :else (collage/resize image :height max-size))
      image)))

(defn throttle! [max-size ext id file]
  (->> file
    (->img)
    (throttle-size max-size)
    (->file! max-size ext id)))
