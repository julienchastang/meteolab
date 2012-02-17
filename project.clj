(defproject meteolab "1.0.0-SNAPSHOT"
  :description "Matlab for meteorologists. Clojure based programming environment built on Incanter and JFreeChart."
  :dependencies [[org.clojure/clojure "1.3.0"]
                 [edu.ucar/netcdf "4.3.2-SNAPSHOT"]
                 [edu.ucar/opendap "4.3.2-SNAPSHOT"]
                 [incanter/incanter-charts "1.3.0-SNAPSHOT"]
                 [meteolab/meteolab-data "1.0.0-SNAPSHOT"
                  :exclusions [org.clojure/clojure]]]
  :dev-dependencies [[repl-utils/repl-utils "1.0.0-SNAPSHOT"]])
