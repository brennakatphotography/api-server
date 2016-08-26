(ns photo-api.services.request-parser
  (:require [clojure.string :as string]
            [clojure.data.json :as json]))

(defn ^:private parse-kv [req pair]
  (let [[key value] (string/split pair #"=")]
    (if (> (count key) 0)
      (assoc req (keyword key) value)
      req)))

(defn parse-query [app]
  (fn [request]
    (->>
      (-> request
        (:query-string)
        (or "")
        (string/split #"&"))
      (reduce parse-kv {})
      (assoc request :query)
      (app))))

(defn parse-body [app]
  (fn [request]
    (try
      (-> request
        (:body)
        (slurp)
        (json/read-str :key-fn keyword)
        ((fn [body] (assoc request :body body)))
        (app))
      (catch Exception e (app request)))))
