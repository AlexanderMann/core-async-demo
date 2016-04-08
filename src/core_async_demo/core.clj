(ns core-async-demo.core
  (:require [clojure.core.async :as async]
            [clojure.string :as s]
            [core-async-demo.crdt :as crdt])
  (:gen-class))

(defn sleepy-print
  "Waits then updates printer."
  [printer position word]
  (Thread/sleep 1000)
  (crdt/idempotent-update printer {:position position
                                   :v word})
  (.getName (Thread/currentThread)))

(defn parallelize-print
  "sleepy-print is slow so to run it faster parallelize-print runs it on multiple cores,
  thus distributing the workload."
  [input]
  (let [words (s/split input #" ")
        printer (crdt/create-print-crdt (count words))
        build-thread-fn (fn [idx word]
                          (async/thread
                            (sleepy-print printer idx (str word " "))))
        threads (map-indexed build-thread-fn words)
        thread-output (async/<!! (async/into [] (async/merge threads)))]
    (print (crdt/to-str printer))
    (flush)
    thread-output))

(defn -main [& _]
  (time
    (let [input "Asynchronous programming is hard, but should it be?"
          print-threads (parallelize-print input)]
      (println)
      (println print-threads)
      (println "All done!"))))
