(ns photo-api.bin.v1.photos
  (:use compojure.core)
  (:require [clojure.walk :refer [keywordize-keys]]
            [photo-api.services.response :as >>>]
            [photo-api.services.s3 :as s3]
            [photo-api.services.db.queries :as db]
            [photo-api.utils.pipe :as pipe]
            [photo-api.services.logger :as log]))

(defroutes unauthed
  (GET "/:id" [id type]
    (->> type
      (keyword)
      (pipe/set-if-not :large #{:small :thumbnail})
      (db/get-public-photo-filename id)
      (s3/download)
      (>>>/img))))

(defroutes authed
  (GET "/:id" [id type]
    (->> type
      (keyword)
      (pipe/set-if-not :full #{:small :thumbnail :large})
      (db/get-public-photo-filename id)
      (s3/download)
      (>>>/img))))
