(ns core-async-demo.core
  (:require [clojure.string :as s])
  (:gen-class))

(defn sleepy-transform
  "Waits then updates printer."
  [word]
  (Thread/sleep 1000)
  (s/upper-case word))

(defn main
  []
  (time
    (let [input "Asynchronous programming is hard, but should it be?"
          result (map sleepy-transform (s/split input #" "))]
      (println)
      (println result)
      (println "All done!"))))

(defn -main [& _]
  (main))
