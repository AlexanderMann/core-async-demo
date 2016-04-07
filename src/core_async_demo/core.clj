(ns core-async-demo.core
  (:require [clojure.string :as s])
  (:gen-class))

(defn sleepy-print
  "Waits, prints, flushes so that display more closely represents code.

  NOTE: Without the flush, you would only see all of the output when the application exits."
  [word]
  (Thread/sleep 1000)
  (print word)
  (flush))

(defn -main [& _]
  (let [input "Asynchronous programming is hard, but should it be?"
        word-seq (s/split input #" ")]
    (time
      (doseq [word word-seq]
        (sleepy-print (str word " "))))
    (println "All done!")))
