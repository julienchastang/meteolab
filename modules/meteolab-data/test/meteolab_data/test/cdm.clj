(ns meteolab-data.test.cdm
  (:use [meteolab.cdm])
  (:use [clojure.test])
  (:import [ucar.units UnitFormatManager]))

(deftest sanity
  (is (= 4 (+ 2 2))))


(deftest unit-conversion
  (let [ts {:time '(0 1 2)
            :data {:vals '(0.0 273.15)
                   :var "temperature"
                   :unit (.parse (UnitFormatManager/instance) "K")
                   :desc "temperature"}}]
    (is (= (-> (unit-convert "celsius" ts) :data :vals)
           '(-273.15 0.0)))))


(deftest catalog-vars-bogus
  (let [v (catalog-vars "bogus")]
       (is (empty? v))))

(deftest datasets-bogus
  (let [ds (datasets "bogus")]
       (is (empty? ds))))

(deftest dataset-latest-bogus
  (let [ds (dataset-latest "bogus")]
       (is (nil? ds))))
