(ns grid-gen.layout-2
  (:require [reagent.core :as reagent]))

(def scale 100)

(def pixel-per-m (/ 1 0.14))

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
   :x2     8 :y2 2
   :x3     1 :y3 1
   :x4     1 :y4 1.5
   :strips [{:len 5.96 :fc 3 :ch 4}
            {:len 5.80 :fc 3 :ch 5}
            {:len 4.38 :fc 3 :ch 6 :strip 1 :reverse? true}
            {:len 5.90 :fc 3 :ch 6 :strip 0}
            {:len 4.61 :fc 3 :ch 7}]})

(def group-l
  {:x1     8 :y1 2
   :x2     9 :y2 4
   :x3     1 :y3 1.5
   :x4     1 :y4 2
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
  {:x1     4.45 :y1 12.26
   :x2     3.42 :y2 12.39
   :x3     2.44 :y3 7.69
   :x4     2.30 :y4 7.583
   :strips [{:len 2.85 :fc 6 :ch 4 :reverse? true}
            {:len 5.12 :fc 6 :ch 4}
            {:len 2.00 :fc 6 :ch 5 :reverse? true}
            {:len 4.32 :fc 6 :ch 5}
            {:len 3.59 :fc 6 :ch 5}]})

(defn length-check
  [{:keys [x1 x2 x3 x4
           y1 y2 y3 y4]}]
  (let [dx1 (- x1 x3)
        dx2 (- x2 x4)
        dy1 (- y1 y3)
        dy2 (- y2 y4)]
    [(Math/sqrt (+ (* dx1 dx1) (* dy1 dy1)))
     (Math/sqrt (+ (* dx2 dx2) (* dy2 dy2)))]))

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
  (let [longest (apply max (map :len strips))
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
                     n)]
    (->> (range n)
         (map #(+ theta1 (* increment (+ % 0.5))))
         vec)))

(defn group-outline
  [{:keys [x1 x2 x3 x4
           y1 y2 y3 y4]
    :as   group}]
  (let [d-str (str "M" (* scale x3) " " (* scale y3) " "
                   "L" (* scale x1) " " (* scale y1) " "
                   "L" (* scale x2) " " (* scale y2) " "
                   "L" (* scale x4) " " (* scale y4) " "
                   "Z")]
    [:path {:d            d-str
            :fill         "#eee"
            :fill-opacity "0.8"
            :stroke       "#ccc"}]))

(defn orientate
  [reverse? coords]
  (if reverse?
    coords
    (reverse coords)))

(defn comp-fn
  [strip-x strip-y]
  (let [cx (first strip-x)
        cy (first strip-y)]
    (if (not= (:fc cx) (:fc cy))
      (< (:fc cx) (:fc cy))
      (if (not= (:ch cx) (:ch cy))
        (< (:ch cx) (:ch cy))
        (< (:strip cx) (:strip cy))))))

(defn global-index
  [strip-coords]
  (->> strip-coords
       (sort comp-fn)
       (map-indexed (fn [idx strips]
                    (vec (map #(assoc % :g-strip idx) strips))))
       vec))

(defn pixel-coords
  [{:keys [strips x1 x2 y1 y2] :as group}]
  (let [n       (count strips)
        x-pitch (/ (- x2 x1) n)
        y-pitch (/ (- y2 y1) n)
        angles  (calc-angles group n)]
    (->> strips
         (map
          (fn [idx angle {:keys [len fc ch strip reverse?]}]
            (let [y      (* scale (+ y1 (* y-pitch (+ idx 0.5))))
                  x      (* scale (+ x1 (* x-pitch (+ idx 0.5))))
                  sx     (- x (* len scale (Math/cos angle)))
                  sy     (- y (* len scale (Math/sin angle)))
                  pixels (Math/ceil (* len pixel-per-m))]
              (->> (range pixels)
                   (map (fn [i]
                          {:x  (- x (* i (/ (- x sx)
                                            pixels)))
                           :y  (- y (* i (/ (- y sy)
                                            pixels)))
                           :fc fc
                           :ch ch
                           :strip strip}))
                   (orientate reverse?)
                   #_(map-indexed (fn [idx pixel]
                                  (assoc pixel :strip-idx idx)))
                   vec)))
          (range n) angles)
         vec)))

(defonce text (reagent/atom ""))

(defn round
  [x]
  (/ (Math/round (* 100 x)) 100))

(defn pixel-circles
  [coords]
  (->> coords
       ;;(apply concat)
       (map (fn [{:keys [x y fc ch strip g-strip idx]}]
              (let [prop-str (str "fc: " fc " "
                                  "ch: " ch " "
                                  "x: " (round x) " "
                                  "y: " (round y) " "
                                  (when strip
                                    (str "strip: " strip " "))
                                  (when idx
                                    (str "idx: " idx " "))
                                  "g-strip: " g-strip)]
                [:circle {:cx            x
                          :cy            y
                          :fill          "#aaa"
                          :fill-opacity  "0.5"
                          :r             5
                          :on-mouse-over #(reset! text prop-str)}])))
       (into [:g])))

(defn apply-index
  [coords]
  (vec (map-indexed
        (fn [idx coord]
          (assoc coord :idx idx))
        (apply concat coords))))

(defn shifted-lines
  [{:keys [strips x1 x2 y1 y2] :as group} label]
  (let [n       (count strips)
        x-pitch (/ (- x2 x1) n)
        y-pitch (/ (- y2 y1) n)
        angles  (calc-angles group n)]
    (into [:g
           [group-outline group]
           #_[pixel-circles group]]
          (map-indexed
           (fn [idx {:keys [len fc ch reverse?]}]
             (let [y      (* scale (+ y1 (* y-pitch (+ idx 0.5))))
                   x      (* scale (+ x1 (* x-pitch (+ idx 0.5))))
                   hue    (if ch (* 45 ch) 0)
                   light  (if ch (if (even? ch) 40 80) 60)
                   hsl    (str "hsl("
                               hue ", 60%, "
                               light "%)")
                   sx     (- x (* len scale (Math/cos (nth angles idx))))
                   sy     (- y (* len scale (Math/sin (nth angles idx))))
                   pixels (Math/ceil (* len pixel-per-m))]
               [:g {:fill hsl}
                [:line (-> (if-not reverse?
                             {:x1 sx :y1 sy
                              :x2 x  :y2 y}
                             {:x1 x  :y1 y
                              :x2 sx :y2 sy}      )
                           (assoc :style {:stroke       hsl
                                          :stroke-width 3
                                          :marker-end   "url(#arrow-marker)"}))]
                ]))
           strips))))

(defn layout-2
  [ratom]
  (let [groups [group-k
                group-l]]
    (into
     [:svg {:width 600
            :view-box "0 0 1000 2000"}
      [:defs arrow-marker]]
     (conj
      (->> groups
           (map #(shifted-lines % nil))
           vec)
      (->> groups
           (map pixel-coords)
           (apply concat)
           global-index
           apply-index
           pixel-circles)))))

(defn main
  [app-state]
  [:div
   [:div [:pre @text]]
   [layout-2 app-state]
   #_[:div [:pre (pixel-coords group-k)]]])
