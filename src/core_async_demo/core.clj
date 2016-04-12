(ns core-async-demo.core
  (:require [clojure.string :as s]
            [clojure.core.async :as async])
  (:gen-class))

(defn sleepy-transform
  "Waits then updates printer."
  [word]
  (Thread/sleep 1000)
  (s/upper-case word))

(defn check-lock
  [locking-channel]
  (let [lock-value (async/<!! locking-channel)]
    (when (not= lock-value (.getName (Thread/currentThread)))
      (throw (Exception. (format "Expected lock-value of: %s got: %s"
                                 (.getName (Thread/currentThread))
                                 lock-value))))))

(defn locking-parallel-transform
  "An async wrapper for the sleepy-transform."
  [previous-complete? locking-channel output-channel word]
  (let [is-complete? (async/chan)]
    [is-complete?
     (async/thread
       (let [retval (sleepy-transform word)]
         (async/<!! previous-complete?)
         (async/>!! locking-channel (.getName (Thread/currentThread)))
         (async/>!! output-channel retval))
       (check-lock locking-channel)
       (async/close! is-complete?))]))

(defn coordinate-parallel
  "Coordinate the created threads so that we don't exit before daemon
  threads have completed."
  [input-seq]
  (let [locking-channel (async/chan 1)
        dummy-thread (async/chan)
        dummy-starting-thread (async/chan)
        output-channel (async/chan (count input-seq))
        thread (loop [thread dummy-thread
                      words input-seq
                      previous-complete? dummy-starting-thread]
                 (if (empty? words)
                   thread
                   (let [[is-complete? new-thread]
                         (locking-parallel-transform
                           previous-complete?
                           locking-channel
                           output-channel
                           (first words))]
                     (recur (async/merge [thread
                                          new-thread])
                            (rest words)
                            is-complete?))))]
    (async/close! dummy-starting-thread)
    (async/close! dummy-thread)
    (async/<!! (async/into [] thread))
    (async/close! output-channel)
    (async/<!! (async/into [] output-channel))))

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
