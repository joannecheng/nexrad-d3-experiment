(ns nexrad.main
  (:require [goog.dom :as dom]
            [clojure.string :as str]))

(def !csv-data (atom nil))

(defn- clean-row [row]
  (-> row
      (update "lon" js/parseFloat)
      (update "lat" js/parseFloat)
      (update "value" js/parseFloat)))

(defn- parse-csv [csv-body]
  (let [all-rows (->> (str/split csv-body #"\r\n")
                      (map #(str/split % #",")))]
    (map #(-> (zipmap (first all-rows) %) clean-row)
         (rest all-rows))))


(defn render-radar [radar-data]
  (prn "rendering" radar-data))

(defn load-radar [filename]
  (prn "filename" filename)
  (-> (js/fetch filename)
      (.then #(.text %))
      (.then #(reset! !csv-data %))
      (.then #(-> @!csv-data parse-csv render-radar))))

(comment
  (load-radar "radar/2017_08_25_KHGX_KHGX20170825_122025_V06.csv")

  (parse-csv @!csv-data)

  (js/parseFloat "1.23")

  (let [k ["a" "b"]
        v [[1 2] [3 4]]]
    (map #(zipmap k %) v))

  (zipmap ["a" "b"] [1 2]))

(defn draw-map []
  ;; You could use the "." notation, but using goog.dom lets us skip that
  ;; using google closure allows us to reduce
  ;; dependencies and ensure compatibility with advanced mode compilation
  (let [el (dom/getElement "viz")
        radar (load-radar "radar/2017_08_25_KHGX_KHGX20170825_122025_V06.csv")]
    (prn "element" el)))

(defn init []
  (prn "test!!")

  (draw-map))