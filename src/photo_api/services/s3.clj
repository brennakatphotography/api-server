(ns photo-api.services.s3
  (:require [aws.sdk.s3 :as s3]
            [environ.core :refer [env]]
            [clojure.java.io :as io]
            [photo-api.services.images :as img]))

(def cred {:access-key (env :aws-access-key-id) :secret-key (env :aws-access-key-secret)})

(def bucket (env :s3-bucket))

(defn ^:private silent-delete! [filename]
  (try (io/delete-file filename)
    (catch Exception e nil)))

(defn upload! [{data :tempfile type :content-type length :size} name throttler]        (throttler data) (println (str "uploading: " name)))
    ; (s3/put-object cred bucket name (throttler data)
    ;   {:content-type type :content-length length}))

(defn reduce-uploads!
  [{id :id file :file name-maker :name-maker ext :ext} urls {size :size type :type}]
  (let [img-key (name-maker type)
        process (if size (partial img/throttle! size ext id) identity)
        url-qry (if size (str "?type=" (name type)) "")]
    (upload! file img-key process)
    (silent-delete! (img/tempfile size id ext))
    (cons (str "/api/v1/photos/" id url-qry) urls)))

(defn map-uploads!
  [{id :id filename :filename file :file name-maker :name-maker ext :ext}]
    (let [reducer (partial reduce-uploads! {:id id :file file :name-maker name-maker :ext ext})
          urls (reduce reducer '() img/sizes)]
      {:id id :filename filename :urls urls}))

(defn download [name]
  (s3/get-object cred bucket name))

(defn delete! [name]
  (s3/delete-object cred bucket name))
