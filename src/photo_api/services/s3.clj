(ns photo-api.services.s3
  (:require [aws.sdk.s3 :as s3]
            [environ.core :refer [env]]
            [clojure.java.io :as io]))

(def cred {:access-key (env :aws-access-key-id) :secret-key (env :aws-access-key-secret)})

(def bucket (env :s3-bucket))

(defn upload! [{data :tempfile type :content-type length :size} name]
  (s3/put-object cred bucket name (io/input-stream data)
    {:content-type type :content-length length}))

(defn download [name]
  (s3/get-object cred bucket name))

(defn delete! [name]
  (s3/delete-object cred bucket name))
