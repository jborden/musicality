(ns musicality.audio
  (:require-macros [reagent.interop :refer [$! $]]))

;;https://stackoverflow.com/questions/11506180/web-audio-api-resume-from-pause
(defn AudioRaw [url]
  (let [context (js/AudioContext.)
        ]
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
                                          ($ js/console log "onBufferError" e)))))
                       ($ send)))))))

(defn Audio [url]
  (let [audio-source (AudioRaw url)]
    (doto audio-source
      ($ loadUrl))
    audio-source))
