(defproject beatbox "1.0.0-SNAPSHOT"
  :description "FIXME: write"
  :dependencies [[org.clojure/clojure "1.2.0"]
                 [org.clojure/clojure-contrib "1.2.0"]
                 [clj-glob "0.1.0"]
                 [overtone "0.1.5"]
                 [polynome "0.1.5"]]
  :jvm-opts ["-XX:+UseParNewGC" "-XX:MaxNewSize=256m" "-XX:NewSize=256m" "-Xms1g" "-Xmx1g" "-XX:+UseConcMarkSweepGC" "-XX:SurvivorRatio=128" "-XX:MaxTenuringThreshold=0" "-XX:+UseTLAB"])
