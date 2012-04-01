(ns examples.cdm-client
  (:use [clojure.set
         :only [intersection]]
        [clojure.string
         :only [join]]
        [meteolab.cdm
         :only [unit-convert time-series
                dataset-latest catalog-vars]]
        [incanter.charts
         :only [time-series-plot add-lines]]
        [incanter.core
         :only [view]]))

;; sample REPL session could go like this

(defn plot-latest-var
  ([catalogs v [lat lon z]]
     (plot-latest-var catalogs v nil [lat lon z]))
  ([catalogs v u [lat lon z]]
      (let [f (if u
                (partial unit-convert u)
                identity)
            [fmodel & nmodel]
            (pmap #(-> (time-series
                        (dataset-latest %)
                        v [lat lon z]) f)
                  catalogs)
            chart (time-series-plot
                   (:time fmodel)
                   (-> fmodel :data :vals)
                   :title (join " " (list (-> fmodel :data :name) "@" lat lon))
                   :x-label "time"
                   :y-label (-> fmodel :data :unit str)
                   :series-label (-> fmodel :name)
                   :legend true)]
        (do
          (doall
           (map #(add-lines
                  chart
                  (:time %) (-> % :data :vals)
                  :series-label (:name %))
                nmodel))
          (view chart)))))

(def cats
  ["http://motherlode.ucar.edu/thredds/catalog/fmrc/NCEP/GFS/CONUS_80km/runs/catalog.xml"
   "http://motherlode.ucar.edu/thredds/catalog/fmrc/NCEP/GFS/Global_0p5deg/runs/catalog.xml"
   "http://motherlode.ucar.edu/thredds/catalog/fmrc/NCEP/NAM/CONUS_12km/runs/catalog.xml"])

(def vs [["Temperature_height_above_ground" "Fahrenheit"]
         ["Relative_humidity_height_above_ground"]
         ["Total_precipitation"]])

(for [[v u] vs]
  (plot-latest-var cats v u [40 -105.27 0]))
