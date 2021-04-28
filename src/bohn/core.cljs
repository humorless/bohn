(ns bohn.core
  (:require
   [reagent.core :as r]
   [reagent.dom :as d]))

(def debug true)
(defonce state (r/atom {:date "2021-04-25"}))
;; -------------------------
;; Views

(defn handle-input-change
  [entity-key e]
  (swap! state
         assoc entity-key (-> e .-target .-value))
  (when debug
    (prn @state)))

(defn home-page []
  [:div [:h2 "Select a date"]
   [:input {:type "date"
            :placeholder "2014-03-18"
            :value (:date @state)
            :on-change (partial handle-input-change :date)}]])

;; -------------------------
;; Initialize app


(defn mount-root []
  (d/render [home-page] (.getElementById js/document "app")))

(defn ^:export init! []
  (mount-root))
