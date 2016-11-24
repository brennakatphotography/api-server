(defproject photo-api "1.0.0-SNAPSHOT"
  :description "API for Brenna's Photo app."
  :main photo-api.core
  :aot [photo-api.core]
  :dependencies [[base64-clj "0.1.1"]
                 [clj-aws-s3 "0.3.10" :exclusions [joda-time]]
                 [clj-jwt "0.1.1"]
                 [clj-time "0.6.0"]
                 [compojure "1.5.1"]
                 [environ "0.5.0"]
                 [fivetonine/collage "0.2.1"]
                 [http-kit "2.1.18"]
                 [jumblerg/ring.middleware.cors "1.0.1"]
                 [korma "0.4.3"]
                 [mysql/mysql-connector-java "5.1.39"]
                 [org.clojure/clojure "1.8.0"]
                 [org.clojure/data.json "0.2.6"]
                 [ring/ring-core "1.3.2"]
                 [ring/ring-json "0.3.1"]
                 [ring/ring-defaults "0.2.1"]
                 [sudharsh/clj-oauth2 "0.5.3"]]
  :jar-name "photo-api.jar"
  :uberjar-name "photo-api-standalone.jar"
  :min-lein-version "2.6.1")
