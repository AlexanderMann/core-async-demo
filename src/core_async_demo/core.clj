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
  (let [words (s/split input #" ")
        threads (for [word words]
                  (async/thread (sleepy-print (str word " "))))]
    (async/<!! (async/into [] (async/merge threads)))))

(defn -main [& _]
  (time
    (let [input "Asynchronous programming is hard, but should it be?"
          print-threads (parallelize-print input)]
      (println)
      (println print-threads)
      (println "All done!"))))
