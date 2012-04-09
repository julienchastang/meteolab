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
  [uri]
  (.readXML (InvCatalogFactory. "default" true) uri))

(defn catalog-vars
  "Get THREDDS variables from XML catalog given THREDDS URI."
  [uri]
  (when-let
      [ds (-> (read-catalog-xml uri) .getDataset)]
    (set (apply
          concat
          (map #(map (memfn getName) %)
               (map (memfn getVariableList)
                    (.getVariables ds)))))))

(defn datasets
  "Given a THREDDS catalog, hands back a list of datasets as THREDDS URIs"
  [uri]
  (map #(zipmap [:name :uri]
                (vector (.getName %)
                        (-> % access :ncdods)))
       (sort-by millisec
                (filter #(not (branch? %))
                        (tree-seq branch? #(.getDatasets %)
                                  (read-catalog-xml uri))))))

(defn dataset-latest
  "Given a THREDDS catalog, hands back a list of datasets as THREDDS URIs"
  [uri]
  (last (datasets uri)))


(defn- attribute-map
  "Takes a list of attributes and put them in a map"
  [attrbs] (into {}
                 (map (fn [y]
                        (vector
                         (keyword (.getName y))
                         (.getObject (.getValues y) 0))) attrbs)))

(defn metadata
  "Get metadata associated with THREDDS URI"
  [uri]
  (with-open [gds (GridDataset/open uri)]
    (let [vrs (.getDataVariables gds)
          atbs (map (memfn getAttributes) vrs)
          md (map attribute-map atbs)]
      (zipmap
       (map (memfn getName) vrs)
       md))))

(defn time-series
  "Returns time series for data that can be read through the CDM API."
  [dataset v [lat lon z]]
  (with-open [gds  (GridDataset/open (:uri dataset))]
    (let [grid (.findGridByName gds v)
          gcs (.getCoordinateSystem grid)
          [x y]  (.findXYindexFromLatLon gcs lat lon (int-array 2))
          valid? (and (>= x 0) (>= y 0))
          unit (-> grid .getVariable .getUnitsString)
          desc (-> grid .getVariable .getDescription)
          name (-> grid .getVariable .getAttributes attribute-map :GRIB_param_name)
          dates (if valid?
                  (doall (map
                          (memfn getTime)
                          (-> gcs .getTimeAxis1D .getTimeDates)))
                  [])
          d (if valid?
               (doall (pmap
                       #(-> (.readDataSlice grid % z y x) .get)
                       (range (count dates))))
               [])]
      {:name (:name dataset) :time dates
       :data {:vals d :var v :name name :unit unit :desc desc}})))

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
