(ns core-async-demo.core
  (:require [clojure.string :as s]
            [clojure.core.async :as async])
  (:gen-class))

(defn sleepy-transform
  "Waits then updates printer."
  [word]
  (Thread/sleep 1000)
  (s/upper-case word))

(defn parallel-transform
  "An asyc wrapper for the sleepy-transform."
  [result word]
  (async/thread
    (swap! result conj (sleepy-transform word))))

(defn main
  []
  (time
    (let [input "Asynchronous programming is hard, but should it be?"
          result (atom [])]
      (doseq [word (s/split input #" ")]
        (parallel-transform result word))
      (println)
      (println @result)
      (println "All done!"))))

(defn -main [& _]
  (main))
