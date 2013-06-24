(defproject meteolab/meteolab-data  "0.1.0-SNAPSHOT"
  :description "Data access module"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :scm {:name "git" :url "https://github.com/julienchastang/meteolab"}
  :min-lein-version "2.0.0"
  :dependencies [[org.clojure/clojure "1.5.1"]
                 [edu.ucar/netcdf "4.3.16"]
                 [edu.ucar/opendap "4.3.16"]
                 [com.revelytix.logbacks/slf4j-log4j12 "1.0.0"]])
