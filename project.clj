(defproject photo-api "1.0.0-SNAPSHOT"
  :description "API for Brenna's Photo app."
  :main photo-api.core
  :aot [photo-api.core]
  :plugins [[lein-beanstalk "0.2.7"]]
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [environ "0.5.0"]
                 [compojure "1.5.1"]
                 [ring/ring-core "1.3.2"]
                 [ring/ring-json "0.3.1"]
                 [ring/ring-defaults "0.2.1"]
                 [jumblerg/ring.middleware.cors "1.0.1"]
                 [clj-aws-s3 "0.3.10" :exclusions [joda-time]]
                 [mysql/mysql-connector-java "5.1.39"]
                 [org.clojure/java.jdbc "0.3.6"]
                 [http-kit "2.1.18"]
                 [clojure.jdbc/clojure.jdbc-c3p0 "0.3.1"]
                 [org.clojure/data.json "0.2.6"]]
  :jar-name "photo-api.jar"
  :uberjar-name "photo-api-standalone.jar"
  :min-lein-version "2.6.1")
