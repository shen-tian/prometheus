(ns grid-gen.core
  (:require
   [reagent.core :as reagent]
   [grid-gen.coords :as coords]))


;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Vars

(defonce app-state
  (reagent/atom {}))

;; coordinate pixels per meter.
(def scale 60)

;; LEDs per meter
(def pixel-pm 2.5)

(defn coords
  [{:keys [start end] :as spar}]
  (let [[sx sy] start
        [ex ey] end
        dx      (- ex sx)
        dy      (- ey sy)
        n       (Math/ceil (/ (Math/sqrt (+ (* dx dx)
                                            (* dy dy)))
                              (/ scale pixel-pm)))
        pitch   (/ 1 n)]
    (vec (map
          (fn [idx]
            [(+ sx  (* idx dx pitch))
             (+ sy  (* idx dy pitch))])
          (range (inc n))))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Page

(defn total-pixels
  [spars]
  (reduce +
          (map (fn [spar] (count (coords spar)))
               spars)))

(defn page [ratom]
  [:div
   [:div
    [:div {:style {:position         "fixed"
                   :z-index          -1
                   :width            600
                   :height           600
                   :background-image "url(template.jpeg)"
                   :background-size  570
                   :filter           "brightness(5%)"}}]
   (into
    [:svg
     {:width   600
      :viewBox "0 0 1000 1000"}]
    (for [spar coords/spars]
      (let [foo (coords spar)]
        ^{:key (str (rand))}
        (into [:g]
              (conj
               (map (fn [[x y]]
                      [:circle {:cx   x :cy y
                                :r    3
                                :fill :purple}])
                    foo)
               (let [[x y] (:end spar)]
                 [:circle {:cx x :cy y :r 4 :fill :green}]))))))]
   [:h3 "Total leds: " (total-pixels coords/spars)]])

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Initialize App

(defn dev-setup []
  (when ^boolean js/goog.DEBUG
    (enable-console-print!)
    (println "dev mode")))

(defn reload []
  (reagent/render [page app-state]
                  (.getElementById js/document "app")))

(defn ^:export main []
  (dev-setup)
  (reload))
