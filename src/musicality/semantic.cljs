(ns musicality.semantic
  (:require [cljsjs.semantic-ui-react]
            [reagent.core :as r]))

(def semantic-ui js/semanticUIReact)

;; from https://gist.github.com/TimoKramer/7e93758afb81dcad985fafccc613153a
(defn component
  "Get a component from sematic-ui-react:
    (component \"Button\")
    (component \"Menu\" \"Item\")"
  [k & ks]
  (r/adapt-react-class
   (if (seq ks)
     (apply goog.object/getValueByKeys semantic-ui k ks)
     (goog.object/get semantic-ui k))))


(def Dropdown (component "Dropdown"))
