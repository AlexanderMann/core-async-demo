(ns core-async-demo.crdt)


(defprotocol CommutativeDT
  (get-details [this] "Return parameters about as a map")
  (to-str [this] "Return a string representation")
  (idempotent-update [this x] "Perform an update which is idempotent to the underlying data-structure"))

(defn- i-update
  [a position v]
  (let [current-v (get a position)]
    (if (or (nil? current-v)
            (> v current-v))
      (assoc a position v)
      a)))

(defrecord CommutativeOutput
  [l]
  CommutativeDT
  (get-details [_]
    {:n (count @l)})
  (to-str [_]
    (str @l))
  (idempotent-update [this {:keys [position v]}]
    (swap! l i-update position v)
    (get-details this)))

(defn create-crdt-output
  "This is the public mechanism for creating a CommutativeOutput defrecord"
  [n]
  (->CommutativeOutput (atom (vec (repeat n nil)))))
