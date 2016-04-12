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
  "An async wrapper for the sleepy-transform."
  [word]
  (async/thread
    (sleepy-transform word)))

(defn coordinate-parallel
  "Coordinate the created threads so that we don't exit before daemon
  threads have completed."
  [input-seq]
  (let [threads (map parallel-transform input-seq)
        merged (async/merge threads)]
    (async/<!! (async/into [] merged))))

(defn main
  []
  (time
    (let [input "Asynchronous programming is hard, but should it be?"
          result (coordinate-parallel (s/split input #" "))]
      (println)
      (println result)
      (println "All done!"))))

(defn -main [& _]
  (main))
