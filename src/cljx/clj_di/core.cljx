(ns clj-di.core)

(def dependencies (atom {}))

(defn register!
  "Register dependecy."
  {:doc/format :markdown}
  [key dep]
  (swap! dependencies assoc key dep))

(defn get-dep
  "Get dependency by protocol."
  [key]
  (key @dependencies))

(defmacro let-deps
  "Something like let but for deps."
  [deps & body]
  (let [names (vec (take-nth 2 deps))
        keys (vec (take-nth 2 (rest deps)))]
    `(let [~names (map clj-di.core/get-dep ~keys)]
       ~@body)))
