(ns clj-di.core)

(def dependencies (atom []))

#+clj (defmacro with-fresh-dependencies
        [& body]
        `(let [old# @clj-di.core/dependencies]
           (reset! clj-di.core/dependencies [])
           ~@body
           (reset! clj-di.core/dependencies old#)))

(defn register!
  "Register dependecy."
  [dep]
  (when-not (some #{dep} @dependencies)
    (swap! dependencies conj dep)))

(defn forget!
  "Forget about dependency."
  [dep]
  (swap! dependencies #(remove #{dep} %)))

(defn dependency
  "Get dependency by protocol."
  [proto]
  (->> (reverse @dependencies)
       (filter #(satisfies? proto %))
       first))
