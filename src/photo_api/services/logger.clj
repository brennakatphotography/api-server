(ns photo-api.services.logger
  (:require [clojure.string :as s]
            [clj-time.core :as time]
            [clj-time.format :as format]))

(defn ^:private kw->caps [kw]
  (->> kw
    (str)
    (s/upper-case)
    (rest)
    (apply str)))

(defn ^:private stamp [log]
  (-> "[MM/dd/yyyy HH:mm:ss]: "
    (format/formatter)
    (format/unparse (time/now))
    (#(str "INFO " %))
    (str log)))

(defn continue
  ([value] (continue value identity))
  ([value extracter] (println (stamp (extracter value))) value))

(defn inbound [app]
  (fn [request]
    (continue request #(-> %
      (:request-method)
      (kw->caps)
      (str " \"" (request :uri) "\"")))
    (app request)))

(defn request [app]
  (fn [request]
    (continue request)
    (app request)))

(defn authenticated? [app]
  (fn [request]
    (continue request #(str "Authenticated: " (:verified (:auth %)) " : " (:role (:auth %))))
    (app request)))
