(ns musicality.core
  (:require [cljsjs.howler]
            [reagent.core :as r]))

(enable-console-print!)

(println "This text is printed from src/musicality/core.cljs. Go ahead and edit it and see reloading in action.")

;; define your app data so that it doesn't get over-written on reload

(def state (r/atom {:displayed-progression "None Yet!"
                    :current-progression "None Yet!"}))

(def foo "bar")
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
  (let [current-progression (r/cursor state [:current-progression])]
    [:button {:on-click (fn [e]
                          (reset! current-progression (rand-nth chord-progressions))
                          (play-chord-progression  @current-progression))}
     "Play Progression!"]))

(defn ShowProgression
  []
  (let [displayed-progression (r/cursor state [:displayed-progression])
        current-progression (r/cursor state [:current-progression])]
    [:div [:h1 "Last Played Progression"]
     [:h1 (clojure.string/join "" (map #(-> % symbol str) @displayed-progression))]
     [:button {:on-click (fn [e]
                           (reset! displayed-progression @current-progression))}
      "Display Last Played Progression"]]))

(r/render [:div
           [PlayChordProgression]
           [ShowProgression]] (.getElementById js/document "app"))
