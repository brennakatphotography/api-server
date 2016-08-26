(ns photo-api.services.response)

(defn ->response
  ([data] (->response data 200))
  ([data status] {:status status :body data}))
