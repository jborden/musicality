(ns musicality.core
  (:require [cljsjs.howler]
            [reagent.core :as r]))

(enable-console-print!)

;; define your app data so that it doesn't get over-written on reload

(def state (r/atom {:current-progression nil
                    :reveal false}))

(defn on-js-reload []
  ;; optionally touch your app-state to force rerendering depending on
  ;; your application
  ;; (swap! app-state update-in [:__figwheel_counter] inc)
)

(def chord-progressions [[:A :D :E :D]
                         [:A :E :D :E]
                         [:A :D :A :E]
                         [:D :A :E :A]])

(def chord-map {:A "audio/A.m4a"
                 :D "audio/D.m4a"
                 :E "audio/E.m4a"})

(defn play-chord-progression
  [progression]
  (let [chord-sound-vector (mapv (fn [chord]
                                   (js/Howl. (clj->js {:src [(chord chord-map)]})))
                                 progression)]
    (doall (map-indexed (fn [i sound]
                          (when (< (+ i 1) (count chord-sound-vector))
                            (.on sound "end" #(.play (nth chord-sound-vector (+ i 1))))))
                        chord-sound-vector))
    (println progression)
    (.play (first chord-sound-vector))))

#_(let 
  (.on A "end" #(.play D))
  (.on D "end" #(.play E))
  (.play A))

(defn PlayChordProgression
  []
  (let [current-progression (r/cursor state [:current-progression])
        reveal (r/cursor state [:reveal])]
    [:div
     [:div [:h1 "Last Played Progression: " (cond (nil? @current-progression)
                                                  nil
                                                  @reveal
                                                  (clojure.string/join "" (map #(-> % symbol str) @current-progression))
                                                  :else
                                                  [:button.ui.button {:on-click #(reset! reveal true)} "Reveal"])]]
     [:br]
     [:button.ui.button.positive.basic {:on-click (fn [e]
                                                    (reset! reveal false)
                                                    (reset! current-progression (rand-nth chord-progressions))
                                                    (play-chord-progression  @current-progression))
               :style {:font-size "1em"}}
      "Play New Progression"]
     (when-not (nil? @current-progression)
       [:button.ui.button.primary.basic {:on-click (fn [e]
                                                     (play-chord-progression  @current-progression))
                                         :style {:font-size "1em"}}
        [:i.redo.icon] "Replay"])]))

#_(defn ShowProgression
  []
  (let [
]
))

(r/render [:div
           [:div.ui.masthead.segment
            [:div.ui.container
             [:div.ui.header
              [:a {:href "/"} "Musicality"]]]]
           [:div.ui.container.main
            ;;[ShowProgression]
            [PlayChordProgression]]] (.getElementById js/document "app"))
