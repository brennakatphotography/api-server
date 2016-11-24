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

(defn ^:private make-url [method request]
  (let [[query-string uri] (map request [:query-string :uri])
        query-string (if query-string (str "?" query-string) "")]
    (str method " \"" uri query-string "\"")))

(defn continue
  ([value] (continue value identity))
  ([value extracter] (println (stamp (extracter value))) value))

(defn out [& args]
  (println (stamp (apply str args))))

(defn inbound [app]
  (fn [request]
    (continue request #(-> %
      (:request-method)
      (kw->caps)
      (make-url request)))
    (app request)))

(defn request [app]
  (fn [request]
    (continue request)
    (app request)))

(defn authenticated? [app]
  (fn [request]
    (continue request #(str "Authenticated: " (:verified (:auth %)) " : " (:role (:auth %))))
    (app request)))
