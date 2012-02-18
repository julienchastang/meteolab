(ns examples.cdm-client
  (:use [meteolab cdm]
        [incanter core charts])
  (:import [ucar.units UnitFormatManager]))

(def catalogs
  ["http://motherlode.ucar.edu/thredds/catalog/fmrc/NCEP/GFS/CONUS_80km/runs/catalog.xml"
   "http://motherlode.ucar.edu/thredds/catalog/fmrc/NCEP/GFS/Global_0p5deg/runs/catalog.xml"
   "http://motherlode.ucar.edu/thredds/catalog/fmrc/NCEP/NAM/CONUS_12km/runs/catalog.xml"
   "http://motherlode.ucar.edu/thredds/catalog/fmrc/NCEP/RUC2/CONUS_13km/runs/catalog.xml"])

(def v "Temperature_height_above_ground")

(def models
  (pmap #(unit-convert
          (time-series
           (dataset-latest %)
           v [40 -105.27 0])
          "Fahrenheit")
        catalogs))

(def chart
  (time-series-plot (:time (first models))
                    (-> (first models) :data :vals)
                    :title v
                    :x-label "time"
                    :y-label (-> (first models) :data :unit str)
                    :legend true))

;; (view chart)

(doall
 (map #(add-lines
        chart
        (:time %)
        (-> % :data :vals)) (next models)))

(view chart)
