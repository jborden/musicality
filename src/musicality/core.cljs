(ns musicality.core
  (:require [cljsjs.howler]
            [musicality.audio :as audio]
            [musicality.controls :as controls]
            [musicality.semantic :refer [Dropdown]]
            [reagent.core :as r])
  (:require-macros [reagent.interop :refer [$]]))

(enable-console-print!)

;; define your app data so that it doesn't get over-written on reload

(def state (r/atom {:current-progression nil
                    :reveal false
                    :key-state {}
                    :playing? false
                    :chord-sound-vector nil}))

(defn on-js-reload []
  ;; optionally touch your app-state to force rerendering depending on
  ;; your application
  ;; (swap! app-state update-in [:__figwheel_counter] inc)
)

(def chord-progressions [[:A :D :E :D]
                         [:A :E :D :E]
                         [:A :D :A :E]
                         [:D :A :E :A]])

(def chord-map #_{:A "audio/A.m4a"
                :D "audio/D.m4a"
                  :E "audio/E.m4a"}
  {:A "audio/A.wav"
   :D "audio/D.wav"
   :E "audio/E.wav"
   :Am "audio/Am_Acoustic.wav"
   :Dm "audio/Dm_Acoustic.wav"
   :Em "audio/Em_Acoustic.wav"
   :C "audio/C_Acoustic.wav"
   :G "audio/G_Acoustic.wav"})

(def chord-sound-map (apply merge (map #(hash-map (key %) (audio/Audio (val %))) chord-map)))

(defn play-chord-progression
  "Play progression. playing? is an atom containing a boolean to determine if the chord
  should actually be play"
  [progression]
  (let [chord-sound-vector (r/cursor state [:chord-sound-vector])]
    (doall (map #($ % stop) @chord-sound-vector))
    (reset! chord-sound-vector (mapv (fn [chord]
                                       (audio/Audio (chord chord-map)))
                                     progression))
    (doall (do (map-indexed (fn [i sound]
                              (when (< (+ i 1) (count @chord-sound-vector))
                                ($ sound setOnEnded #($ (nth @chord-sound-vector (+ i 1)) play))))
                            @chord-sound-vector)))
    ($ (first @chord-sound-vector) play)))

(defn ChordPalette
  []
  (let [palette (r/cursor state [:palette])
        default-chords (keys chord-map)
        process-palette (fn [palette]
                          (map #(hash-map :key %
                                          :text (-> % symbol str)
                                          :value %) palette))]
    (when-not @palette
      (reset! palette default-chords))
    [Dropdown {:placeholder "Chords"
               :fluid true
               :multiple true
               :search true
               :selection true
               :options (process-palette default-chords)
               :value @palette
               :on-change (fn [event data]
                            (reset! palette (into [] (->> ($ data :value) js->clj (map keyword)))))}]))

(defn PlayChordProgression
  []
  (let [current-progression (r/cursor state [:current-progression])
        reveal (r/cursor state [:reveal])
        palette (r/cursor state [:palette])
        key-state (r/cursor state [:key-state])
        reveal-fn #(reset! reveal true)
        playing? (r/cursor state [:playing?])
        test-n-fn (fn [e] (.log js/console "n"))
        play-new-progression (fn []
                               (.log js/console "n")
                               (reset! reveal false)
                               (reset! current-progression
                                       (into [] (take 4 (repeatedly #(rand-nth @palette)))))
                               (play-chord-progression @current-progression))
        play-progression (fn []
                           (play-chord-progression  @current-progression))
        key-up-fn (fn [e]
                    (let [keycode (or ($ e :keycode)
                                      ($ e :which))]
                      ;;(.log js/console "keycode: " (str keycode))
                      (condp = (get controls/key-definitions (str keycode))
                        :n (play-new-progression)
                        :r (play-progression)
                        :v (reveal-fn)
                        nil)))]
    (do (js/removeEventListener "keyup" key-up-fn true)
        (js/addEventListener "keyup" key-up-fn true))
    (r/create-class
     {:reagent-render
      (fn [args]
        [:div
         [:div [:h1 "Last Played Progression: " (cond (nil? @current-progression)
                                                      nil
                                                      @reveal
                                                      (clojure.string/join " " (map #(-> % symbol str) @current-progression))
                                                      :else
                                                      [:button.ui.button {:on-click reveal-fn} "Reveal"])]]
         [:br]
         [ChordPalette]
         [:br]
         [:button.ui.button.positive.basic {:on-click play-new-progression
                                            :style {:font-size "1em"}}
          "Play New Progression"]
         [:button.ui.button.positive.basic {:on-click #(.play (:Em chord-sound-map))
                                            :style {:font-size "1em"}}
          "play-chord"]
         (when-not (nil? @current-progression)
           [:button.ui.button.primary.basic {:on-click play-progression
                                             :style {:font-size "1em"}}
            [:i.redo.icon] "Replay"])])})))

#_(defn ShowProgression
  []
  (let [
]
))

(defn ChordProgression []
  (let [key-state (r/cursor state [:key-state])]
    (controls/initialize-key-listeners! key-state)
    [:div
     [:div.ui.masthead.segment
      [:div.ui.container
       [:div.ui.header
        [:a {:href "/"} "Musicality"]]]]
     [:div.ui.container.main
      ;;[ShowProgression]
      [PlayChordProgression]]]))

(r/render [ChordProgression] (.getElementById js/document "app"))
