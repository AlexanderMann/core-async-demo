(ns core-async-demo.core
  (:require [clojure.string :as s]
            [clojure.core.async :as async]
            [core-async-demo.crdt :as crdt])
  (:gen-class))

(defn sleepy-transform
  "Waits then updates printer."
  [word]
  (Thread/sleep 1000)
  (s/upper-case word))

(defn parallel-transform
  "An async wrapper for the sleepy-transform."
  [crdt-output position word]
  (async/thread
    (crdt/idempotent-update
      crdt-output
      {:position position
       :v (sleepy-transform word)})))

(defn coordinate-parallel
  "Coordinate the created threads so that we don't exit before daemon
  threads have completed."
  [input-seq]
  (let [crdt-output (crdt/create-crdt-output (count input-seq))
        thread (map-indexed (partial parallel-transform crdt-output) input-seq)]
    (async/<!! (async/into [] (async/merge thread)))
    (crdt/to-str crdt-output)))

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
