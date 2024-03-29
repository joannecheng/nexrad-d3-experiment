(ns nexrad.main
  (:require [goog.object :as gobj]
            [goog.dom :as dom]
            [reagent.core :as r]
            [reagent.dom :as rd]
            [d3-geo :as d3-geo]
            [d3-scale :as d3-scale]
            [topojson-client :as topojson]
            [re-frame.core :as rf]))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;-- Data Cleaning Helpers
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

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;-- HTTP requests
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
(def ts ["20170825_120316_V06"
         "20170825_120859_V06"
         "20170825_121442_V06"
         "20170825_122025_V06"
         "20170825_122608_V06"])

(defn radar-url [timestamp]
  (str "radar/2017_08_25_KHGX_KHGX"
       timestamp
       ".csv"))

(defn load-radar [ts-index]
  (prn "ts-index" ts-index)
  (let [filename (radar-url (get ts ts-index))]
    (-> (js/fetch filename)
        (.then #(.text %))
        (.then #(parse-csv %))
        (.then #(rf/dispatch [:load-radar-data %])))))

(defn load-us-map []
  (-> (js/fetch "us.json")
      (.then #(.text %))
      (.then #(.parse js/JSON %))
      (.then #(rf/dispatch [:load-map-data %]))))

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
 :ts-index
 (fn [db _]
   (:ts-index db)))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;-- Event Handlers
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
(rf/reg-event-db
 :init-db
 (fn [_ _]
   {:radar-data []
    :map-data nil
    :ts-index 0}))

(rf/reg-event-db
 :load-radar-data
 (fn [db [_ radar-data]]
   (assoc db :radar-data radar-data)))

(rf/reg-event-db
 :load-map-data
 (fn [db [_ map-data]]
   (assoc db :map-data map-data)))

(rf/reg-event-fx
 :update-timestamp
 (fn [cofx [_ ts-index]]
   (load-radar ts-index)
   {:db (assoc (:db cofx) :ts-index ts-index)}))

;;---------------------------------------------------------------
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Components
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

;; navigation ---------------------------------------------------
(defn slider []
  (r/with-let [!val (r/atom 0)
               ts-index-subscribe (rf/subscribe [:ts-index])]
    [:input {:type "range"
             :name "snapshot"
             :value @ts-index-subscribe
             :max (dec (count ts))
             :min 0
             :step 1
             :onChange (fn [^js/Event e]
                         (when (not= (js/parseInt (.-target.value e)) @ts-index-subscribe)
                           (rf/dispatch [:update-timestamp (js/parseInt (.-target.value e))])))}]))

(defn navigation []
  [:div
   [slider]])

;; maps ---------------------------------------------------------
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

(defn- draw-radar [projection context radar-data]
  (let [radar-color-scale (-> d3-scale
                              .scaleLinear
                              (.domain #js [10 20 30 35 40 45 50 55])
                              (.range #js ["#FFFFFF00" "#808080"
                                           "#ADD8E6" "#00FB90"
                                           "#00BB00" "#FFFF70"
                                           "#D0D060" "#FF6060"
                                           "#DA0000"]))]
    (.clearRect context 0 0 900 500)
    (doseq [{:keys [lon lat value]} radar-data]
      (let [coords (projection #js [lon lat])]
        (.beginPath context)
        (.rect context (aget coords 0) (aget coords 1) 1 1)
        (set! (.-fillStyle context) (radar-color-scale value))
        (.fill context)
        (.closePath context)))))

(defn radar-layer []
  (let [!canvas-ref (atom nil)
        !context (atom nil)]
    (r/create-class
     {:component-did-update (fn [this]
                              (let [{:keys [projection radar-data]} (r/props this)]
                                (when (some? @!context)
                                  (draw-radar projection @!context radar-data))))
      :reagent-render
      (fn [_]
        (when (some? @!canvas-ref)
          (reset! !context (cond-> @!canvas-ref
                             (some? @!canvas-ref) (.getContext "2d"))))
        [:foreignObject {:x 0 :y 0 :style {:width (str 900 "px") :height (str 500 "px")}}
         [:canvas.radar {:ref #(reset! !canvas-ref %) :height 500 :width 900}]])})))

(defn NexradApp []
  ;; Main components go here
  (let [projection (.geoAlbers ^js/Object d3-geo)
        path (d3-geo/geoPath projection)
        radar-data @(rf/subscribe [:radar-data])]
    [:div
     [:svg.nexrad-map
      [base-map {:path path}]
      [radar-layer {:path path :projection projection :radar-data radar-data}]]
     [navigation]]))

(defn init []
  (let [el (dom/getElement "viz")]
    (rf/dispatch-sync [:init-db])
    (load-us-map)
    (load-radar 0)
    (rd/render [NexradApp] el)))