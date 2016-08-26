(ns photo-api.services.logger
  (:require [clojure.string :as s]))

(defn ^:private kw->caps [kw]
  (->> kw
    (str)
    (s/upper-case)
    (rest)
    (apply str)))

(defn in [app]
  (fn [request]
    (-> request
      (:request-method)
      (kw->caps)
      (str " \"" (request :uri) "\"")
      (println))
    (app request)))
