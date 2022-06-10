(ns nexrad.main
  (:require [goog.dom :as dom]))

(defn render-radar []
  ;; TODO: this will render one radar file
  )

(defn draw-map []
  ;; You could use the "." notation, but using goog.dom lets us skip that
  ;; using google closure allows us to reduce
  ;; dependencies and ensure compatibility with advanced mode compilation
  (let [el (dom/getElement "viz")]
    (prn "element" el)))

(defn init []
  (prn "test!!")

  (draw-map))