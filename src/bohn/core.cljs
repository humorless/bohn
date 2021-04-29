(ns bohn.core
  (:require [clojure.string :as string]
            [clojure.set]
            [reagent.core :as r]
            [reagent.dom :as d]))

(def debug true)
(defonce state (r/atom {:date "2021-04-25"
                        :base []
                        :inner {:1 [] :2 [] :3 []}
                        :outer {:left-2 [] :left-3 []
                                :right-2 [] :right-3 []
                                :top-2 [] :top-3 []}}))

(defonce result (r/atom {:primary nil
                         :left-primary nil
                         :right-primary nil
                         :inner-no nil
                         :outer-no nil}))

(defn op
  [[x y]]
  (let [sum (+ x y)]
    (if (= sum 9)
      9
      (mod sum 9))))

(defn climb [coll]
  (mapv op (partition 2 coll)))

(defn parse-date
  " \"2021-04-25\" -> [2 4 0 4 2 0 2 1]"
  [d]
  (mapv #(js/parseInt %)
        (mapcat #(map identity %)
                (reverse
                 (string/split d #"-")))))

(def one-to-nine #{1 2 3 4 5 6 7 8 9})
;; -------------------------
;; Views

(defn handle-input-change
  [e]
  (let [d (-> e .-target .-value)
        base (parse-date d)
        f (climb base)
        s (climb f)
        t (climb s)
        l-f (vec (interleave (repeat (first s)) (take 2 f)))
        l-s (climb l-f)
        l-t (climb l-s)
        r-f (vec (interleave (repeat (second s)) (take-last 2 f)))
        r-s (climb r-f)
        r-t (climb r-s)
        t-f (vec (interleave (repeat (first t)) (reverse s)))
        t-s (climb t-f)
        t-t (climb t-s)
        inner-yes (set (concat f s t))
        outer-yes (set (concat l-s l-t r-s r-t t-s t-t))]
    (swap! state
           assoc :date d :base base
           :inner {:1 f :2 s :3 t}
           :outer {:left-2 l-s :left-3 l-t
                   :right-2 r-s :right-3 r-t
                   :top-2 t-s :top-3 t-t})
    (swap! result
           assoc :primary (first t)
           :left-primary (first f)
           :right-primary (last f)
           :inner-no (clojure.set/difference one-to-nine inner-yes)
           :outer-no (clojure.set/difference one-to-nine outer-yes)))
  (when debug
    (prn "process: " @state)
    (prn "result: " @result)))

(defn home-page []
  [:div
   [:h2 "Select a date"]
   [:input {:type "date"
            :placeholder "2014-03-18"
            :value (:date @state)
            :on-change handle-input-change}]
   [:div
    [:div
     [:h2 "綜合分析"]
     [:p "主性格 " (:primary @result)]
     [:p "左副 " (:left-primary @result)]
     [:p "右副 " (:right-primary @result)]
     [:p "內缺 " (:inner-no @result)]
     [:p "外缺 " (:outer-no @result)]]
    [:div
     [:h2 "內三角形"]
     [:p "上層　"  (pr-str (get-in @state [:inner :3]))]
     [:p "中層　" (pr-str (get-in @state [:inner :2]))]
     [:p "下層　" (pr-str (get-in @state [:inner :1]))]]
    [:div
     [:h2 "左三角形"]
     [:p "上層　"  (pr-str (get-in @state [:outer :left-3]))]
     [:p "下層　" (pr-str (get-in @state [:outer :left-2]))]]
    [:div
     [:h2 "右三角形"]
     [:p "上層　"  (pr-str (get-in @state [:outer :right-3]))]
     [:p "下層　" (pr-str (get-in @state [:outer :right-2]))]]
    [:div
     [:h2 "上三角形"]
     [:p "上層　"  (pr-str (get-in @state [:outer :top-3]))]
     [:p "下層　" (pr-str (get-in @state [:outer :top-2]))]]]])
;; -------------------------
;; Initialize app


(defn mount-root []
  (d/render [home-page] (.getElementById js/document "app")))

(defn ^:export init! []
  (mount-root))
