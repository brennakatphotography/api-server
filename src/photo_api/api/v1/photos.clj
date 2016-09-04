(ns photo-api.api.v1.photos
  (:use compojure.core)
  (:require [photo-api.services.response :refer [->json ->img ->api ->err]]
            [photo-api.services.s3 :as s3]
            [photo-api.services.db.queries :as db]
            [photo-api.services.logger :as log]))

(defroutes core
  ; (GET "/test/:name" [name] (->img (s3/download name)))
  ; (DELETE "/test/:name" [name] (s3/delete! name) (->json {:message "success"}))
  (POST "/test" {data :multipart-params}
    (->> data
      (db/save-new-photos!)
      (map (fn [{id :id filename :filename file :file name :name}]
        (s3/upload! file name)
        {:id id :filename filename :url (str "/api/v1/photos/" id)}))
      (#(->api % {:message "Photo(s) saved" :status 201}))))

  (DELETE "/test/:id" [id]
    (->> :full
      (db/delete-photo! id)
      (s3/delete!))
    (->api {:message "Photo deleted."}))

  (GET "/:id" [id]
    (->> :full
      (db/get-public-photo-filename id)
      (#(if %
        (->img (s3/download %))
        (->err))))))
