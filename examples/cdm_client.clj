(ns examples.cdm-client
  (:use [meteolab cdm]
        [incanter core charts])
  (:import [ucar.units UnitFormatManager]))

;;(def catalog-uri "http://motherlode.ucar.edu/thredds/catalog/fmrc/NCEP/GFS/CONUS_80km/runs/catalog.xml")
;;(def catalog-uri "http://motherlode.ucar.edu/thredds/catalog/fmrc/NCEP/GFS/Global_0p5deg/runs/catalog.xml")
(def catalog-uri "http://motherlode.ucar.edu/thredds/catalog/fmrc/NCEP/NAM/CONUS_12km/runs/catalog.xml")

(def v "Temperature_height_above_ground")

(def ts-data
  (unit-convert
   (time-series (dataset-latest catalog-uri) v 40 -105.27 0)
   "Fahrenheit"))

(let [time (:time ts-data)
      temperature (-> ts-data :data :vals)]
  (view
   (time-series-plot time
                     temperature
                     :title (-> ts-data :data :desc)
                     :x-label "time"
                     :y-label (-> ts-data :data :unit str)
                     :legend true)))
