(ns musicality.synthesizer)

;; https://pages.mtu.edu/~suits/NoteFreqCalcs.html
(def f0 440)

(def notes [:C :C# :D :D# :E :F :F# :G :G# :A :A# :B])

(defn f
  "Given n steps above A4, calculate the frequency of that note"
  [n]
  (* f0 (js/Math.pow (js/Math.pow 2 (/ 1 12)) n)))

(defn freq-note
  "Given a note (keyword) and an octave n, calculate the frequency of that note"
  [octave note]
  (f (+ (- (.indexOf notes note)
           (.indexOf notes :A))
        (* (- octave 4) 12))))

;; https://developer.mozilla.org/en-US/docs/Web/API/Web_Audio_API/Simple_synth
;; not working yet
#_(def master-gain-node (.. (js/AudioContext.)
                          (createGain)
                          ))
