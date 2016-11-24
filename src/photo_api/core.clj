(ns photo-api.core
  (:gen-class)
  (:use compojure.core org.httpkit.server)
  (:require [compojure.handler :refer [site]]
            [compojure.route :refer [not-found]]
            [environ.core :refer [env]]
            [ring.middleware.json :refer [wrap-json-response]]
            [ring.middleware.cors :refer [wrap-cors]]
            [photo-api.services.request-parser :as parser]
            [photo-api.services.logger :as log]
            [photo-api.services.response :as >>>]
            [photo-api.api :as api]
            [photo-api.bin :as bin]
            [photo-api.services.auth :as auth]
            [photo-api.auth :as oauth]))

(defroutes app-routes
  (HEAD "/" [] "")
  (GET "/healthcheck" [] (>>>/json {:system "OK"}))
  (context "/api" [] api/core)
  (context "/bin" [] bin/core)
  (context "/auth" [] oauth/core)
  (not-found (>>>/json {:message "Unknown resource"} 404)))

(def app
  (-> app-routes
    (wrap-json-response)
    (log/authenticated?)
    ; (log/request)
    (auth/authenticate)
    (parser/parse-query)
    (parser/parse-body)
    (log/inbound)
    (wrap-cors :access-control-allow-origin (re-pattern (or (env :allowed-origin) "http://localhost.*"))
               :access-control-allow-methods [:get :put :post :delete])))

(defn -main [& args]
  (let [port (-> env (:port) (or 3000) (Integer.))]
    (run-server (site app) {:port port})
    (log/out (str "Server is listening on port: " port))))
