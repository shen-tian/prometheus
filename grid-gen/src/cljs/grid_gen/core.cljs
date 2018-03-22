(ns grid-gen.core
  (:require
   [reagent.core :as reagent]
   [grid-gen.coords :as coords]
   [cljsjs.clipboard]))


;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Vars

(defonce app-state
  (reagent/atom {:pixel-pm 5}))

;; coordinate pixels per meter.
(def scale 60)

(defn coords
  [{:keys [start end] :as spar}]
  (let [[sx sy] start
        [ex ey] end
        dx      (- ex sx)
        dy      (- ey sy)
        n       (Math/ceil (/ (Math/sqrt (+ (* dx dx)
                                            (* dy dy)))
                              (/ scale (:pixel-pm @app-state))))
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

(defn transform-to-unit
  [x y r max-x max-y]
  [(- (* (/ x max-x) 2 r) r)
   (- (* (/ y max-y) 2 r) r)])

(defn lines->edn
  [spars]
  (->> spars
       (map
        (fn [spar]
          (map
           (fn [[x y]]
             (let [[tx ty] (transform-to-unit x y 10 1000 1000)]
               {:point [tx ty 0]}))
           (coords spar))))
       (reduce concat)
       vec))

(defn edn->pstring
  [edn]
  (.stringify js/JSON (clj->js edn) nil 2))

(defn clipboard-button [label text]
  (let [clipboard-atom (atom nil)]
    (reagent/create-class
     {:display-name "clipboard-button"
      :component-did-mount
      #(let [clipboard (new js/Clipboard (reagent/dom-node %))]
         (reset! clipboard-atom clipboard))
      :component-will-unmount
      #(when-not (nil? @clipboard-atom)
         (.destroy @clipboard-atom)
         (reset! clipboard-atom nil))
      :reagent-render
      (fn []
        [:button.pure-button.clipboard
         {:data-clipboard-text text}
         label])})))

(defn layout-1 [ratom]
  [:div
   [:div
    [:div {:style {:position         "absolute"
                   :z-index          -1
                   :width            600
                   :height           600
                   :background-image "url(template.jpeg)"
                   :background-size  570
                   :filter           "brightness(20%)"}}]
   (into
    [:svg
     {:width   600
      :viewBox "0 0 1000 1000"}]
    (for [spar coords/spars]
      (let [coord (coords spar)]
        ^{:key (str (rand))}
        (into [:g]
              (concat
               (map (fn [[x y]]
                      [:circle {:cx   x :cy y
                                :r    3
                                :fill :purple}])
                    coord)
               (let [[x y] (:end spar)]
                 [[:circle {:cx x :cy y :r 4 :fill :green}]
                  [:text {:x (+ x 30) :y y :fill "yellow"}
                   (count coord)]]))))))]
   [:h3 "Total leds: " (total-pixels coords/spars)]
   [:h4 "Total spars: " (count coords/spars)]
   [:h3 "LED/m: " (:pixel-pm @app-state)]
   [:div
    [:input {:type      "range"
             :min       1.0
             :max       10
             :step      0.5
             :value     (:pixel-pm @app-state)
             :on-change #(swap! app-state assoc :pixel-pm
                                (-> % .-target .-value))}]]
   [clipboard-button "Copy JSON" (edn->pstring (lines->edn coords/spars))]
   [:pre (edn->pstring (lines->edn coords/spars))]])

(def group-j
  {:x1     9 :y1 1
   :x2     9 :y2 2
   :strips [{:len 5.28 :fc 3 :ch 0}
            {:len 5.38 :fc 3 :ch 1}
            {:len 4.89 :fc 3 :ch 2}
            {:len 5.67 :fc 3 :ch 3}
            {:len 4.63 :fc 3 :ch 3 :reverse? true}]})

(def group-k
  {:x1     9 :y1 1
   :x2     9 :y2 2
   :strips [{:len 5.96 :fc 3 :ch 4}
            {:len 5.80 :fc 3 :ch 5}
            {:len 4.38 :fc 3 :ch 6 :reverse? true}
            {:len 5.90 :fc 3 :ch 6}
            {:len 4.61 :fc 3 :ch 7}]})

(def group-l
  {:x1     9 :y1 1
   :x2     9 :y2 2
   :strips [{:len 5.73 :fc 4 :ch 0}
            {:len 4.36 :fc 4 :ch 1}
            {:len 5.15 :fc 4 :ch 2}
            {:len 3.16 :fc 4 :ch 2 :reverse? true}
            {:len 5.78 :fc 4 :ch 3}
            {:len 3.56 :fc 4 :ch 3 :reverse? true}]})

(def group-m
  {:x1     7.98 :y1 13.07
   :x2     7.05 :y2 14.15
   :x3     2.48 :y3 7.44
   :x4     2.48 :y4 7.39
   :strips [{:len 8.50 :fc 5 :ch 0}
            {:len 7.83 :fc 5 :ch 1}
            {:len 5.75 :fc 5 :ch 2}
            {:len 7.04 :fc 5 :ch 3}
            {:len 7.86 :fc 5 :ch 4}
            {:len 6.19 :fc 5 :ch 5}
            {:len 8.50 :fc 5 :ch 6}]})

(def group-n
  {:x1     6.09 :y1 12.2
   :x2     4.45  :y2 12.26
   :x3     2.45  :y3 4.54
   :x4     2.41  :y4 7.20
   :strips [{:len 1.69 :fc 6 :ch 0 :reverse? true}
            {:len 4.59 :fc 6 :ch 0}
            {:len 5.04 :fc 6 :ch 1}
            {:len 3.30 :fc 6 :ch 1 :reverse? true}
            {:len 5.50 :fc 6 :ch 2}
            {:len 3.80 :fc 6 :ch 2 :reverse? true}
            {:len 5.37 :fc 6 :ch 3}]})

(def group-o
  {:x1     4.45  :y1 12.26
   :x2     3.42 :y2 12.39
   :x3     2.44  :y3 7.69
   :x4     2.30  :y4 7.583
   :strips [{:len 2.85 :fc 6 :ch 4 :reverse? true}
            {:len 5.12 :fc 6 :ch 4}
            {:len 2.00 :fc 6 :ch 5 :reverse? true}
            {:len 4.32 :fc 6 :ch 5}
            {:len 3.59 :fc 6 :ch 5}]})


(map (fn [[x y]]
       [(/ x 407) (/ y 407)])
     [[3250 5320]
      [2870 5760]
      [1010 3030]
      [1010 3010]]
     )

(defn hyp [x y]
  (Math/sqrt (+ (* x x) (* y y))))
(/ (hyp 467 1970) 4.97)
;; 407px per meter
(defn length-check
  [{:keys [x1 x2 x3 x4
           y1 y2 y3 y4]}]
  (let [dx1 (- x1 x3)
        dx2 (- x2 x4)
        dy1 (- y1 y3)
        dy2 (- y2 y4)]
    [(Math/sqrt (+ (* dx1 dx1) (* dy1 dy1)))
     (Math/sqrt (+ (* dx2 dx2) (* dy2 dy2)))]))

(length-check group-o)

(def arrow-marker
  [:marker
   {:id            "arrow-marker"
    :marker-width  4
    :marker-height 4
    :ref-x         0
    :ref-y         2
    :orient        "auto"
    :marker-units  "strokeWidth"}
   [:path
    {:d "M0,0 L0,4 L4,2 L0,0"}]])

(defn lines
  [{strips :strips} start-y label]
  (let [scale   100
        longest (apply max (map :len strips))
        far     (+ 20 (* longest scale))]
    (into [:g
           [:text {:x 20 :y (- start-y 10)}
            label]]
          (map-indexed
           (fn [idx {:keys [len fc ch reverse?]}]
             (let [y     (+ start-y (* 18 idx))
                   hue   (if ch (* 45 ch) 0)
                   light (if ch (if (even? ch) 40 80) 60)
                   hsl   (str "hsl("
                              hue ", 60%, "
                              light "%)")
                   x1    (if-not reverse? (- far (* scale len)) far)
                   x2    (if reverse? (- far (* scale len)) far)]
               [:g {:fill hsl}
                [:line {:x1    x1 :y1 y
                        :x2    x2 :y2 y
                        :style {:stroke       hsl
                                :stroke-width 3
                                :marker-end   "url(#arrow-marker)"}}]
                [:text {:x     (+ far 15)
                        :y     (+ y 5)
                        :style {:fill :black}}
                 (str len "m (" (Math/ceil (* len 7)) " px)")]]))
           strips))))

(defn calc-angles
  [{:keys [x1 x2 x3 x4
           y1 y2 y3 y4]} n]
  (let [theta1    (Math/atan (/ (- y1 y3) (- x1 x3)))
        theta2    (Math/atan (/ (- y2 y4) (- x2 x4)))
        increment (/ (- theta2 theta1)
                     (dec n))]
    (->> (range n)
         (map #(+ theta1 (* increment %)))
         vec)))

(defn shifted-lines
  [{:keys [strips x1 x2 y1 y2] :as group} label]
  (let [scale   100
        n       (count strips)
        x-pitch (/ (- x2 x1) (dec n))
        y-pitch (/ (- y2 y1) (dec n))
        longest (apply max (map :len strips))
        far     (+ 20 (* longest scale))
        angles  (calc-angles group n)]
    (into [:g
           #_[:text {:x 20 :y (- y1 10)}
            label]]
          (map-indexed
           (fn [idx {:keys [len fc ch reverse?]}]
             (let [y (* scale (+ y1 (* y-pitch idx)))
                   x (* scale (+ x1 (* x-pitch idx)))

                   hue   (if ch (* 45 ch) 0)
                   light (if ch (if (even? ch) 40 80) 60)
                   hsl   (str "hsl("
                              hue ", 60%, "
                              light "%)")
                   sx    (- x (* len scale (Math/cos (nth angles idx))))
                   sy    (- y (* len scale (Math/sin (nth angles idx))))
                   ;;    (if-not reverse? (- x2 (* scale len)) x2)
                   ;;    (if reverse? (- x2 (* scale len)) x2)
                   ]
               [:g {:fill hsl}
                [:line {:x1    sx :y1 sy
                        :x2    x                   :y2 y
                        :style {:stroke       hsl
                                :stroke-width 3
                                :marker-end   "url(#arrow-marker)"}}]
                #_[:text {:x     (+ far 15)
                          :y     (+ y 5)
                          :style {:fill :black}}
                 (str len "m (" (Math/ceil (* len 7)) " px)")]]))
           strips))))

(defn layout-2
  [ratom]
  (let [scale 100]
    (into
     [:svg {:width 600
            :view-box "0 0 1000 2000"}
      [:defs arrow-marker]]
     (concat
      (shifted-lines group-m "Group M")
      #_(shifted-lines group-n "Group N")
      (shifted-lines group-o "Group O")
      ;;(lines group-j 50  "Group J")
      ;;(lines group-k 200 "Group K")
      ;;(lines group-l 350 "Group L")
      ;;(lines group-m 500 "Group M")
      ;;(lines group-n 650 "Group N")
      ;;(lines group-o 800 "Group O")
      ))))

(defn page [ratom]
  [:div
   [:div
    [:button.pure-button {:on-click #(swap! ratom assoc :page 1)} "1"]
    [:button.pure-button {:on-click #(swap! ratom assoc :page 2)} "2"]]
   (case (:page @ratom)
     1 [layout-1 ratom]
     2 [layout-2 ratom]
     [layout-2 ratom])])

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
