(ns photo-api.services.response)

(defn ->json
  ([data] (->json data 200))
  ([data status] {:status status :body data}))

(defn ->img [{{type :content-type length :content-length} :metadata content :content}]
  {:headers {"Content-Type" type "Content-Length" length} :body content})
