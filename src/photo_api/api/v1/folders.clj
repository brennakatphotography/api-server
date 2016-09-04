(ns photo-api.api.v1.folders
  (:use compojure.core)
  (:require [photo-api.services.response :refer [->json ->api]]
            [photo-api.services.db.queries :as db]))

(defroutes core
  (GET "/" [] (->api (db/get-all-public-folders)))
  (GET "/public" [] (->api (db/get-public-folder 1)))
  (GET "/:id" [id] (->api (db/get-public-folder id))))
