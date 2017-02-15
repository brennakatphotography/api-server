(ns photo-api.core
  (:gen-class)
  (:use compojure.core org.httpkit.server)
  (:require [compojure.handler :refer [site]]
            [compojure.route :refer [not-found]]
            [environ.core :refer [env]]
            [ring.middleware.json :refer [wrap-json-response]]
            [photo-api.services.request-parser :as parser]
            [photo-api.services.logger :as log]
            [photo-api.services.response :as >>>]
            [photo-api.api :as api]
            [photo-api.bin :as bin]
            [photo-api.services.error :as err]
            [photo-api.services.auth :as auth]
            [photo-api.auth :as oauth]))

(defroutes app-routes
  (HEAD "/" [] "")
  (GET "/healthcheck" [] (>>>/json {:a :ok}))
  (context "/api" [] api/core)
  (context "/bin" [] bin/core)
  (context "/auth" [] oauth/core)
  (not-found (>>>/api nil {:message "Unknown resource" :status 404})))

(def app
  (-> app-routes
    (err/handle-error)
    ; (log/authenticated?)
    (auth/authenticate)
    (parser/parse-query)
    (parser/parse-body)
    (log/inbound)
    (wrap-json-response)))

(defn -main [& args]
  (let [port (-> env (:port) (or 3000) (Integer.))]
    (run-server (site app) {:port port})
    (log/out (str "Server is listening on port: " port))))
