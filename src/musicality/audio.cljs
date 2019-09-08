(ns musicality.audio
  (:require-macros [reagent.interop :refer [$! $]]))

;;https://stackoverflow.com/questions/11506180/web-audio-api-resume-from-pause
(defn AudioRaw [url]
  (let [context (js/AudioContext.)]
    (reify
      Object
      (setOnEnded [this fn]
        ($! this :on-ended fn))
      (setSourceNode [this]
        (let [source-node ($ context createBufferSource)]
          (doto source-node
            ($ connect ($ context :destination))
            ($! :buffer ($ this getBuffer)))
          ($! this :source-node source-node)))
      (play [this]
        ($ this setSourceNode)
        ($! ($ this getSourceNode) :onended ($ this :on-ended))
        ($ ($ this getSourceNode) start 0))
      (stop [this]
        ($! ($ this getSourceNode) :onended nil)
        ($ ($ this getSourceNode) stop))
      (getSourceNode [this]
        ($ this :source-node))
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
