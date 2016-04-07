(ns core-async-demo.core
  (:require [clojure.core.async :as async]
            [clojure.string :as s])
  (:gen-class))

(defn sleepy-print
  "Waits, prints, flushes so that display more closely represents code.

  NOTE: Without the flush, you would only see all of the output when the application exits."
  [previous-complete? locking-channel is-complete? word]
  (Thread/sleep 1000)
  (async/<!! previous-complete?)
  (async/>!! locking-channel (.getName (Thread/currentThread)))
  (print word)
  (flush)
  (let [lock-value (async/<!! locking-channel)]
    (when (not= lock-value (.getName (Thread/currentThread)))
      (throw (Exception. (format "Expected lock-value of: %s got: %s"
                                 (.getName (Thread/currentThread))
                                 lock-value)))))
  (async/close! is-complete?)
  (.getName (Thread/currentThread)))

(defn build-locking-thread
  "sleepy-print sometimes gets out of order without locking mechanisms like callbacks and channles."
  [previous-complete? locking-channel input]
  (let [is-complete? (async/chan)]
    [is-complete?
     (async/thread (sleepy-print previous-complete?
                                 locking-channel
                                 is-complete?
                                 input))]))

(defn parallelize-print
  "sleepy-print is slow so to run it faster parallelize-print runs it on multiple cores,
  thus distributing the workload."
  [input]
  (let [words (s/split input #" ")
        locking-channel (async/chan 1)
        dummy-thread (async/chan)
        dummy-starting-thread (async/chan)
        thread (loop [thread dummy-thread
                      words words
                      previous-complete? dummy-starting-thread]
                 (if (empty? words)
                   thread
                   (let [[is-complete? new-thread] (build-locking-thread previous-complete?
                                                                         locking-channel
                                                                         (-> words
                                                                             first
                                                                             (str " ")))]
                     (recur (async/merge [thread
                                          new-thread])
                            (rest words)
                            is-complete?))))]
    (async/close! dummy-starting-thread)
    (async/close! dummy-thread)
    (async/<!! (async/into [] thread))))

(defn -main [& _]
  (time
    (let [input "Asynchronous programming is hard, but should it be?"
          print-threads (parallelize-print input)]
      (println)
      (println print-threads)
      (println "All done!"))))
