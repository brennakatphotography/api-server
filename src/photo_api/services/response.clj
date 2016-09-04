(ns photo-api.services.response)

(defn ->json
  ([data] (->json data 200))
  ([data status] {:status status :body data}))

(defn ->api
  ([data] (->api data {:status 200}))
  ([data {status :status message :message}]
    (if data
      (->json {:data data :message message :success true} (or status 200))
      (->json {:message "Unknown resource" :success false} 400))))

(defn ->img [{{type :content-type length :content-length} :metadata content :content}]
  {:headers {"Content-Type" type "Content-Length" length} :body content})
