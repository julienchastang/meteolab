(ns meteolab.cdm
  (:import [ucar.nc2.dt.grid GridDataset]
           [ucar.nc2.dt.grid GeoGrid]
           [ucar.nc2.dataset CoordinateAxis1DTime]
           [ucar.units UnitFormatManager]
           [thredds.catalog InvCatalogFactory]))

(defn- branch? [x]
  (if (instance? thredds.catalog.InvCatalogImpl x)
    true
    (when (instance? thredds.catalog.InvDatasetImpl x)
      (-> x .getAccess empty?))))

(defn- millisec [x]
  (-> x .getTimeCoverage .getStart .getDate .getTime))

(defn- access [x]
  (into {}
        (map #(vector (-> % .getService .getName keyword)
                      (-> % .getStandardUrlName))
             (.getAccess x))))

(defn- read-catalog-xml
  [catalog-uri]
  (.readXML (InvCatalogFactory. "default" true) catalog-uri))

(defn catalog-vars
  "Get THREDDS variables from XML catalog given THREDDS URI."
  [catalog-uri]
  (when-let
      [ds (-> (read-catalog-xml catalog-uri) .getDataset)]
    (set (apply
          concat
          (map #(map (memfn getName) %)
               (map (memfn getVariableList)
                    (.getVariables ds)))))))

(defn datasets
  "Given a THREDDS catalog, hands back a list of datasets as THREDDS URIs"
  [catalog-uri]
  (map #(zipmap [:name :uri]
                (vector (.getName %)
                        (-> % access :ncdods)))
       (sort-by millisec
                (filter #(not (branch? %))
                        (tree-seq branch? #(.getDatasets %)
                                  (read-catalog-xml catalog-uri))))))

(defn dataset-latest
  "Given a THREDDS catalog, hands back a list of datasets as THREDDS URIs"
  [catalog-uri]
  (last (datasets catalog-uri)))

(defn metadata
  "Get metadata associated with THREDDS dataset"
  [dataset]
  (with-open [gds (GridDataset/open (:uri dataset))]
    (let [vrs (.getDataVariables gds)
          atbs (map (memfn getAttributes) vrs)
          md (map (fn [x]
                    (into {}
                          (map (fn [y]
                                 (vector
                                  (keyword (.getName y))
                                  (.getObject (.getValues y) 0))) x))) atbs)]
      (zipmap
       (map (memfn getName) vrs)
       md))))

(defn time-series
  "Returns time series for data that can be read through the CDM API."
  [dataset v [lat lon z]]
  (with-open [gds  (GridDataset/open (:uri dataset))]
    (let [grid (.findGridByName gds v)
          unit (-> grid .getVariable .getUnitsString)
          desc (-> grid .getVariable .getDescription)
          gcs (.getCoordinateSystem grid)
          dates (doall (map
                        (memfn getTime)
                        (-> gcs .getTimeAxis1D .getTimeDates)))
          [x y]  (.findXYindexFromLatLon gcs lat lon (int-array 2))
          d  (doall (pmap
                     #(-> (.readDataSlice grid % z y x) .get)
                     (range (count dates))))]
      {:name (:name dataset) :time dates
       :data {:vals d :var v :unit unit :desc desc}})))

(defn unit-convert
  "Given a time series and a unit string, convert the time series to the unit.
If the unit is not compatible, the original data is handed back"
  [unit ts]
  (let [parse-unit #(.parse (UnitFormatManager/instance) %)
        from-unit (parse-unit (-> ts :data :unit))
        to-unit (parse-unit unit)]
    (if (.isCompatible from-unit to-unit)
      (assoc-in
       (update-in ts [:data :vals]
                  (fn [c]
                    (map
                     #(.convertTo from-unit (double  %) to-unit)
                     c)))
       [:data :unit] unit)
      ts)))
