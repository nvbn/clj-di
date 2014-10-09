(ns clj-di.core)

(def dependencies (atom {}))

(defn register!
  "Register dependecy, usage:

  ```clojure
  (register :logger logger
            :http http-client)
  ```
  "
  {:doc/format :markdown}
  [& key-dep-pairs]
  (apply swap! dependencies assoc key-dep-pairs))

(defn get-dep
  "Get dependency by name, usage:

  ```clojure
  (get-dep :http)
  ```"
  [key]
  (key @dependencies))

(defmacro let-deps
  "Get dependencies with `let`-like syntax:

  ```clojure
  (let-deps [http :http
             logger :logger]
    ...)
  ```

  It will work like:

  ```clojure
  (let [http (get-dep :http)
        logger (get-dep :logger)]
    ...)
  ```
  "
  [deps & body]
  (let [names (vec (take-nth 2 deps))
        keys (vec (take-nth 2 (rest deps)))]
    `(let [~names (map clj-di.core/get-dep ~keys)]
       ~@body)))
