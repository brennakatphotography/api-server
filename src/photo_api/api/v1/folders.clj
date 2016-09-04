(ns photo-api.api.v1.folders
  (:use compojure.core)
  (:require [photo-api.services.response :as >>>]
            [photo-api.services.db.queries :as db]))

(defroutes shared
  (GET "/public" [] (>>>/api (db/get-public-folder 1))))

(defroutes unauthed
  shared
  (GET "/" [] (>>>/api (db/get-all-public-folders)))
  (GET "/:id" [id] (>>>/api (db/get-public-folder id))))

(defroutes authed
  shared
  (GET "/" [] (>>>/api (db/get-all-folders)))
  (GET "/:id" [id] (>>>/api (db/get-folder id))))
