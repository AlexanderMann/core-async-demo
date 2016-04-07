(ns core-async-demo.core
  (:require [clojure.core.async :as async]
            [clojure.string :as s])
  (:gen-class))

(defn sleepy-print
  "Waits, prints, flushes so that display more closely represents code.

  NOTE: Without the flush, you would only see all of the output when the application exits."
  [word]
  (Thread/sleep 1000)
  (print word)
  (flush)
  (.getName (Thread/currentThread)))

(defn parallelize-print
  "sleepy-print is slow so to run it faster parallelize-print runs it on multiple cores,
  thus distributing the workload."
  [input]
  (loop [words (s/split input #" ")]
    (async/thread
      (-> words
          first
          (str " ")
          sleepy-print))
    (when (second words)
      (recur (rest words)))))

(defn -main [& _]
  (let [input "Asynchronous programming is hard, but should it be?"]
    (time
      (parallelize-print input))
    (println "All done!")))
