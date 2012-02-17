(ns meteolab.cdm
  (:import [ucar.nc2.dt.grid GridDataset]
           [ucar.nc2.dt.grid GeoGrid]
           [ucar.nc2.dataset CoordinateAxis1DTime]
           [ucar.units UnitFormatManager]
           [thredds.catalog InvCatalogFactory]))

(defn metadata
  "Get metadata associated with THREDDS URI"
  [data-uri]
  (with-open [gds (GridDataset/open data-uri)]
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
  [data-uri v lat lon z]
  (with-open [gds (GridDataset/open data-uri)]
    (let [grid (.findGridByName gds v)
          unit (.parse (UnitFormatManager/instance)
                       (-> grid .getVariable .getUnitsString))
          desc (-> grid .getVariable .getDescription)
          gcs (.getCoordinateSystem grid)
          dates (doall (map
                        (memfn getTime)
                        (-> gcs .getTimeAxis1D .getTimeDates)))
          [x y]  (.findXYindexFromLatLon gcs lat lon (int-array 2))
          d (doall (map
                    #(-> (.readDataSlice grid % z y x) .get)
                    (range (count dates))))]
      {:time dates :data {:vals d :var v :unit unit :desc desc}})))

(defn unit-convert
  "Given a time series and a unit string, convert the time series to the unit.
If the unit is not compatible, the original data is handed back"
  [ts unit]
  (let [from-unit (-> ts :data :unit)
        to-unit (.parse (UnitFormatManager/instance) unit)]
    (if (.isCompatible from-unit to-unit)
      (assoc-in
       (update-in ts [:data :vals]
                  (fn [c]
                    (map
                     #(.convertTo from-unit (double  %) to-unit)
                     c)))
       [:data :unit] unit)
      ts)))

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

(defn datasets
  "Given a THREDDS catalog, hands back a list of datasets as THREDDS URIs"
  [catalog-uri]
  (map #(-> % access :ncdods)
       (let [cat (.readXML (InvCatalogFactory. "default" true) catalog-uri)]
         (sort-by millisec
                  (filter #(not (branch? %))
                          (tree-seq branch? #(.getDatasets %) cat))))))

(defn dataset-latest [catalog-uri]
  "Given a THREDDS catalog, hands back a list of datasets as THREDDS URIs"
  (last (datasets catalog-uri)))
