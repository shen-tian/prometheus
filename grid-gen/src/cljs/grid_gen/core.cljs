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

(def group-l
  [{:len 5.73}
   {:len 4.36}
   {:len 5.15}
   {:len 3.16}
   {:len 5.78}
   {:len 3.56}])

(def group-m
  [{:len 8.5}
   {:len 7.83}
   {:len 5.75}
   {:len 7.04}
   {:len 7.86}
   {:len 6.19}
   {:len 8.5}])

(def group-n
  [{:len 1.69}
   {:len 4.59}
   {:len 5.04}
   {:len 3.3}
   {:len 5.5}
   {:len 3.8}
   {:len 5.37}])

(def group-o
  [{:len 2.85}
   {:len 5.12}
   {:len 2}
   {:len 4.325}
   {:len 3.59}])



(defn lines
  [group x]
  (let [scale   100
        longest (apply max (map :len group))
        far     (+ 20 (* longest scale))]
    (map-indexed
     (fn [idx {:keys [len]}]
       (let [y (+ x (* 20 idx))]
         [:g
          [:line {:x1    (- far (* scale len)) :y1 y
                  :x2    far                   :y2 y
                  :style {:stroke :black}}]
          [:text {:x (+ far 5)
                  :y (+ y 5)}
           (str len "m (" (Math/ceil (* len 7)) " px)")]]))
     group)))

(defn layout-2
  [ratom]
  (let [scale 100]
    (into
     [:svg {:width 600
            :view-box "0 0 1000 1000"}]
     (concat
      (lines group-l 100)
      (lines group-m 300)
      (lines group-n 500)
      (lines group-o 700)))))

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
