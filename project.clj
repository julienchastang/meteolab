(defproject meteolab "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :scm {:name "git" :url "https://github.com/julienchastang/meteolab"}
  :min-lein-version "2.0.0"
  :dependencies [[org.clojure/clojure "1.5.1"]
                 [edu.ucar/netcdf "4.3.16"]
                 [edu.ucar/opendap "4.3.16"]
                 [incanter/incanter-charts "1.5.1"]
                 [meteolab/meteolab-data "0.1.0-SNAPSHOT"]]
  :jvm-opts ["-Xmx1g"])
