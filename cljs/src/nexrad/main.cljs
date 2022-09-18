(ns nexrad.main
  (:require [goog.object :as gobj]
            [goog.dom :as dom]
            [reagent.core :as r]
            [reagent.dom :as rd]
            [d3-geo :as d3-geo]
            [topojson-client :as topojson]
            [re-frame.core :as rf]))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;-- Subscriptions
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
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
      (update :lon js/parseFloat)
      (update :lat js/parseFloat)
      (update :value js/parseFloat)))

(defn- parse-csv [csv-body]
  (let [all-rows (->> (clojure.string/split csv-body #"\r\n")
                      (map #(clojure.string/split % #",")))
        csv-keys (map keyword (first all-rows))]
    (mapv #(-> (zipmap csv-keys %)
               clean-row)
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
  [:g.base-layer
   [:g.land
    [:path.land {:d land-path}]]
   [:g.states
    [:path.states {:d states-path}]]])

(defn base-map [{:keys [path]}]
  (let [map-data @(rf/subscribe [:map-data])]
    (when (some? map-data)
      (let [states-path (-> ^js/Array map-data
                            (topojson/mesh (gobj/getValueByKeys map-data #js ["objects" "states"])
                                           (fn [a b] (not= a b)))
                            path)
            land-path (-> ^js/Array map-data
                          (topojson/mesh (gobj/getValueByKeys map-data #js ["objects" "land"]))
                          path)]
        [map-canvas {:states-path  states-path
                     :land-path land-path}]))))

(defn radar-layer []
  (let [!canvas-ref (atom nil)]
    (fn [{:keys [projection]}]
      (let [radar-data @(rf/subscribe [:radar-data])]
        (.log js/console "context?" @!canvas-ref)
        [:foreignObject {:x 0 :y 0 :width "100%" :height "100%"}
         [:canvas {:ref #(reset! !canvas-ref %)}
          #_(for [{:keys [value lon lat]} radar-data]
              (let [coords (projection #js [lon lat])]
                #_[:rect {:key (str lon lat) :value value :x lon :y lat}]))
          #_[:text.radar-layer (str "radar data here")]]]))))

(defn NexradApp []
  ;; Main components go here
  (let [projection (.geoAlbers ^js/Object d3-geo)
        path (d3-geo/geoPath projection)]
    [:svg.nexrad-map
     [base-map {:path path}]
     [radar-layer {:path path}]]))

(defn init []
  (let [el (dom/getElement "viz")]
    (rf/dispatch-sync [:init-db])
    (load-us-map)
    (load-radar "radar/2017_08_25_KHGX_KHGX20170825_122025_V06.csv")
    (rd/render [NexradApp] el)))