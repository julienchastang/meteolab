(ns examples.cdm-client
  (:use [clojure.set
         :only [intersection]]
        [meteolab.cdm
         :only [unit-convert time-series
                dataset-latest catalog-vars]]
        [incanter.charts
         :only [time-series-plot add-lines]]
        [incanter.core
         :only [view]]))

;; sample REPL session could go like this

(def catalogs
  ["http://motherlode.ucar.edu/thredds/catalog/fmrc/NCEP/GFS/CONUS_80km/runs/catalog.xml"
   "http://motherlode.ucar.edu/thredds/catalog/fmrc/NCEP/GFS/Global_0p5deg/runs/catalog.xml"
   "http://motherlode.ucar.edu/thredds/catalog/fmrc/NCEP/NAM/CONUS_12km/runs/catalog.xml"])

(defn plot-latest-var
  [catalogs v [lat lon z]]

  )

;; (apply intersection (map catalog-vars catalogs))

;; Temperature_height_above_ground height above ground could be interesting

(def v "Temperature_height_above_ground")
;; (def v "Relative_humidity_height_above_ground")

;; (def v "Total_precipitation")

;; Get the data

(def models
  (pmap #(unit-convert
          (time-series
           (dataset-latest %)
           v [40 -105.27 0])
          "Fahrenheit")
        catalogs))

;; (def models
;;   (pmap #(time-series
;;           (dataset-latest %)
;;           v [40 -105.27 0])
;;         catalogs))


;; Plot the data

(def chart
  (time-series-plot
   (:time (first models))
   (-> (first models) :data :vals)
   :title (-> (first models) :data :desc)
   :x-label "time"
   :y-label (-> (first models) :data :unit str)
   :series-label (-> (first models) :name)
   :legend true))

(doall
 (map #(add-lines
        chart
        (:time %) (-> % :data :vals)
        :series-label (:name %))
      (next models)))

(view chart)
