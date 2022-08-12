(ns nexrad.main
  (:require [goog.dom :as dom]
            [uix.core.alpha :as uix :refer [defui]]
            [uix.dom.alpha :as uix.dom]
            [clojure.string :as str]
            [re-frame.core :as rf]))

;;-- Subscriptions
(rf/reg-sub
 :radar-data
 (fn [db _]
   (:data db)))

(rf/reg-sub
 :title
 (fn [db _]
   (:title db)))

;;-- Event Handlers
(rf/reg-event-db
 :init-db
 (fn [_ _]
   {:data []
    :title "Test title"}))

(rf/reg-event-db
 :load-radar-data
 (fn [db [_ radar-data]]
   (assoc db :data radar-data)))

;;-- Components

(def !csv-data (atom nil))
(def !us-map (atom nil))

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
  (prn "rendering!!!" (first radar-data))
  radar-data)

(defn load-radar [filename]
  (-> (js/fetch filename)
      (.then #(.text %))
      (.then #(reset! !csv-data %))
      (.then #(-> @!csv-data parse-csv render-radar))))

(defn load-us-map []
  (-> (js/fetch "us.json")
      (.then #(.text %))
      (.then #(.parse js/JSON %))
      (.then #(reset! !us-map %))))

(defn app []
  (let [title @(rf/subscribe [:title])]
    (prn "title" title)
    [:div title]))

(defn draw-radar []
  (let [el (dom/getElement "viz")
        radar (load-radar "radar/2017_08_25_KHGX_KHGX20170825_122025_V06.csv")]
    (uix.dom/render [app] el)))

(defn draw-map []
  (let [el (dom/getElement "viz")]
    (load-us-map)))

(defn init []
  (rf/dispatch-sync [:init-db])
  (draw-map))