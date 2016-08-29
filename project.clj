(defproject photo-api "0.1.0-SNAPSHOT"
  :description "API for Brenna's Photo app."
  :main photo-api.app
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [environ "0.5.0"]
                 [compojure "1.5.1"]
                 [ring/ring-devel "1.3.2"]
                 [ring/ring-core "1.3.2"]
                 [ring/ring-json "0.3.1"]
                 [ring/ring-defaults "0.2.1"]
                 [jumblerg/ring.middleware.cors "1.0.1"]
                 [clj-aws-s3 "0.3.10" :exclusions [joda-time]]
                 [mysql/mysql-connector-java "5.1.39"]
                 [org.clojure/java.jdbc "0.3.6"]
                 [http-kit "2.1.18"]
                 [clojure.jdbc/clojure.jdbc-c3p0 "0.3.1"]
                 [org.clojure/data.json "0.2.6"]])
