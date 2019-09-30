(ns musicality.audio
  (:require-macros [reagent.interop :refer [$! $]]))

;;https://stackoverflow.com/questions/11506180/web-audio-api-resume-from-pause
(defn AudioRaw [url]
  (let [context (js/AudioContext.)]
    (reify
      Object
      (ready [this]
        (.log js/console "buffer: " ($ this getBuffer))
        (.log js.console "ready context.state :" ($ context :state))
        (not (nil? ($ this getBuffer))))
      (setOnEnded [this fn]
        ($! this :on-ended fn))
      (setSourceNode [this]
        (let [source-node ($ context createBufferSource)]
          (doto source-node
            ($! :buffer nil)
            ($ connect ($ context :destination))
            ($! :buffer ($ this getBuffer)))
          ($! this :source-node source-node)))
      (internalPlay [this]
        (.log js/console "I am playing!")
        (.log js/console "internal play context.state :" ($ context :state ))
        ($ this setSourceNode)
        ($! ($ this getSourceNode) :onended ($ this :on-ended))
        ($ ($ this getSourceNode) start 0))
      (play [this]
        (if ($ this ready)
          ($ this internalPlay)
          ($! this :play-interval-id
              (js/setInterval (fn []
                                (.log js/console "I wasn't ready")
                               (if ($ this ready)
                                 (do
                                   (js/clearInterval ($ this :play-interval-id))
                                   ($ this internalPlay))))
                              100))))
      (getSourceNode [this]
        ($ this :source-node))
      (stop [this]
        (.log js/console "I stopped")
        (when-not (nil? ($ this getSourceNode))
          (.log js/console "I really stopped")
          ($! ($ this getSourceNode) :onended nil)
          ($ ($ this getSourceNode) stop 0)
          ($ ($ this getSourceNode) disconnect)
          ($! this :source-node nil)))
      (setBuffer [this buffer]
        ($! this :buffer buffer))
      (getBuffer [this]
        ($ this :buffer))
      (loadUrl [this]
           (let [request (js/XMLHttpRequest.)]
                     (doto request
                       ($ open "GET" url true)
                       ($! :responseType "arraybuffer")
                       ($! :onload (fn []
                                     ($ context decodeAudioData
                                        ($ request :response)
                                        ;; onBufferLoad
                                        (fn [b]
                                          ($ this setBuffer b))
                                        ;; onBufferError
                                        (fn [e]
                                          ($ js/console log url "onBufferError" e)))))
                       ($ send)))))))

(defn Audio [url]
  (let [audio-source (AudioRaw url)]
    (doto audio-source
      ($ loadUrl))
    audio-source))

(defn audio
  "Create an audio map object using url"
  [audio-url]
  (let [request (js/XMLHttpRequest.)
        buffer (atom nil)
        context (js/AudioContext.)]
    (doto request
      ($ open "GET" audio-url true)
      ($! :responseType "arraybuffer")
      ($! :onload (fn []
                    ($ context decodeAudioData
                       ($ request :response)
                       ;; onBufferLoad
                       (fn [b]
                         (reset! buffer b)
                         ;;($ buffer setBuffer b)
                         )
                       ;; onBufferError
                       (fn [e]
                         ($ js/console log audio-url "onBufferError" e)))))
      ($ send))
    {:buffer buffer
     :context context}))

(defn ready?
  [audio]
  (-> @(:buffer audio)
      nil?
      not))

(defn get-buffer
  [audio]
  @(:buffer audio))

(defn assoc-source-node
  "Assoc a AudioBufferSourceNode to audio"
  [audio]
  (let [context (:context audio)
        source-node ($ context createBufferSource)]
    (doto source-node
      ($! :buffer nil)
      ($ connect ($ context :destination))
      ($! :buffer (get-buffer audio)))
    (assoc audio :source-node
           source-node)))

(defn do-play
  "Internal version of playing audio, play should be used when actually playing the sound"
  [audio & [{:keys [on-ended]}]]
  (let [source-node (:source-node (assoc-source-node audio))]
    (when (fn? on-ended)
      ($! source-node :onended on-ended))
    ($ source-node start 0)))

(defn play-check-interval
  "Play when everything is ready to go"
  [audio & [opts]]
  (let [{:keys [interval-id]} opts]
    (if (ready? audio)
      (do
        (js/clearInterval interval-id)
        (do-play audio opts))
      (play-check-interval (js/setInterval )))))

(defn play
  "Play audio using optional map opts.
  opts is
  {:on-ended fn ; a callback fn to execute when audio is done playing}"
  [audio & [opts]]
  (let [interval-id (atom nil)
        ;; play-check-interval (fn []
        ;;                       (when (ready? audio)
        ;;                         (js/clearInterval @interval-id)
        ;;                         (do-play audio opts))
        ;;                       (when-not (ready? audio)
        ;;                         (reset! interval-id
        ;;                                 (js/setInterval (play-check-interval)
        ;;                                                 100))))
        ]
    #_(if (ready? audio)
      (do-play audio opts)
      (play-check-interval))))


(defn promise-foo []
  (let [
        promise (js/Promise.
                 (fn [resolve reject]
                   (js/setTimeout
                    (fn []
                      (resolve "foo"))
                    300)))]
    (doto promise
      ($ then (fn [value]
                value)))))
