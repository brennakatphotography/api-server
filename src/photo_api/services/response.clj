(ns photo-api.services.response)

(defn json
  ([data] (json data 200))
  ([data status] {"Content-Type" "application/json" :status status :body data}))

(defn err
  ([] (err "Not found" {:status 404}))
  ([message] (err message {:status 404}))
  ([message {status :status}]
    (json {:message message :success false} (or status 404))))

(defn unauthorized []
  (err "Not Authorized" {:status 403}))

(defn api
  ([data] (api data {:status 200}))
  ([data {status :status message :message}]
    (if data
      (json {:data data :message message :success true} (or status 200))
      (err "Unknown resource" {:status 400}))))

(defn img [{{type :content-type length :content-length} :metadata content :content}]
  {:headers {"Content-Type" type "Content-Length" length} :body content})
