(ns photo-api.services.logger
  (:require [clojure.string :as s]))

(defn ^:private kw->caps [kw]
  (->> kw
    (str)
    (s/upper-case)
    (rest)
    (apply str)))

(defn continue
  ([value] (continue value identity))
  ([value extracter] (println (extracter value)) value))

(defn inbound [app]
  (fn [request]
    (continue request (fn [r]
      (-> r
        (:request-method)
        (kw->caps)
        (str " \"" (request :uri) "\""))))
    (app request)))
