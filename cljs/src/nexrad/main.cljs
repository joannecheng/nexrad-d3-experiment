(ns nexrad.main
  (:require [goog.object :as gobj]
            [goog.dom :as dom]
            [reagent.core :as r]
            [reagent.dom :as rd]
            [d3-geo :as d3-geo]
            [topojson-client :as topojson]
            [re-frame.core :as rf]))

;;-- Subscriptions
(rf/reg-sub
 :radar-data
 (fn [db _]
   (:radar-data db)))

(rf/reg-sub
 :map-data
 (fn [db _]
   (:map-data db)))

(rf/reg-sub
 :title
 (fn [db _]
   (:title db)))

;;-- Event Handlers
(rf/reg-event-db
 :init-db
 (fn [_ _]
   {:radar-data []
    :map-data nil
    :title "Test title"}))

(rf/reg-event-db
 :load-radar-data
 (fn [db [_ radar-data]]
   (assoc db :radar-data radar-data)))

(rf/reg-event-db
 :load-map-data
 (fn [db [_ map-data]]
   (assoc db :map-data map-data)))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;-- Random Helpers
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
(defn- clean-row [row]
  (-> row
      (update "lon" js/parseFloat)
      (update "lat" js/parseFloat)
      (update "value" js/parseFloat)))

(defn- parse-csv [csv-body]
  (let [all-rows (->> (clojure.string/split csv-body #"\r\n")
                      (map #(clojure.string/split % #",")))]
    (mapv #(-> (zipmap (first all-rows) %) clean-row)
          (rest all-rows))))

(defn load-radar [filename]
  (-> (js/fetch filename)
      (.then #(.text %))
      (.then #(parse-csv %))
      (.then #(rf/dispatch [:load-radar-data %]))))

(defn load-us-map []
  (-> (js/fetch "us.json")
      (.then #(.text %))
      (.then #(.parse js/JSON %))
      (.then #(rf/dispatch [:load-map-data %]))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Components
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
(defn map-canvas [{:keys [states-path land-path]}]
  (prn "states-path" states-path)
  (when (some? states-path)
    (prn "lrendering states ")
    [:svg.base-map
     [:g.land
      [:path.land {:d land-path}]]
     [:g.states
      [:path.states {:d states-path}]]]))

(defn base-map []
  (let [!el (atom nil)
        projection (.geoAlbers ^js/Object d3-geo)
        path (d3-geo/geoPath projection)]
    (fn []
      (let [map-data @(rf/subscribe [:map-data])]
        (when (some? map-data)
          (let [states-path (-> ^js/Array map-data
                                (topojson/mesh (gobj/getValueByKeys map-data #js ["objects" "states"])
                                               (fn [a b] (not= a b)))
                                path)
                land-path (-> ^js/Array map-data
                              (topojson/mesh (gobj/getValueByKeys map-data #js ["objects" "land"]))
                              path)]
            [:div
             [map-canvas {:states-path  states-path
                          :land-path land-path}]]))))))

(defn radar-layer []
  (let [radar-data @(rf/subscribe [:radar-data])]
    (prn "radar data"  (first radar-data))
    [:div (str "radar data here")]))

(defn App []
  ;; Main components go here
  [:div
   [base-map]
   [radar-layer]])

(defn init []
  (let [el (dom/getElement "viz")]
    (rf/dispatch-sync [:init-db])
    (load-us-map)
    (load-radar "radar/2017_08_25_KHGX_KHGX20170825_122025_V06.csv")
    (rd/render [App] el)))