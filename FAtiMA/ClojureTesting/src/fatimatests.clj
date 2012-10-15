(ns fatimatests
  (:import (WorldTest)
           (FAtiMA Agent
                   AgentLauncher)))


;data/MindBlindness/
;MindBlindnessScenarios.xml
;MindBlindness
;Tim
(def ^:dynamic *params*
  {:reference {:world ["data/getthereference/"
                       "GetTheReferenceScenarios.xml"
                       "GetTheReferenceScenario"
                       ]
               :agents {:sally ["data/getthereference/"
                                "GetTheReferenceScenarios.xml"
                                "GetTheReferenceScenario"
                                "Sally"
                                ]
                        :molly ["data/getthereference/"
                                "GetTheReferenceScenarios.xml"
                                "GetTheReferenceScenario"
                                "Molly"
                                ]}}})

(def str-array (partial into-array String))

(defn run-obj
  [& keys]
  (cond
    (= (second keys) :world) (do
                               (let [prm (get-in *params* keys)]
                                 (println "Calling with: " prm)
                                 (WorldTest/main (str-array prm))))
                               
    (= (second keys) :agents) (do
                               (let [prm (get-in *params* keys)]
                                 (println "Calling with: " prm)
                                 (AgentLauncher/main (str-array prm))))
    
    :else (println "Error: Bad keys!")
    ))