(ns photo-api.services.db
  (:use korma.db)
  (:require [environ.core :refer [env]]))

(defdb db (mysql {:db (env :db-name)
                    :user (env :db-user)
                    :password (env :db-password)
                    :host (env :db-host)
                    :port (env :db-port)}))
