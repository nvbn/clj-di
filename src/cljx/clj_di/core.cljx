(ns clj-di.core
  "Functions and macros for using dependency injection.")

(def ^:no-doc dependencies (atom {}))

(defn register!
  "Register dependecy, usage:

  ```clojure
  (register :logger logger
            :http http-client)
  ```

  After that you can get dependency with [[get-dep]] or [[let-deps]]
  using dependency name (here - `:logger` and `:http`)."
  [& key-dep-pairs]
  (apply swap! dependencies assoc key-dep-pairs))

(defn get-dep
  "Get dependency by name, usage:

  ```clojure
  (get-dep :http)
  ```

  Dependency should be registered with [[register!]]."
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

  Dependencies should be registered with [[register!]].
  ```"
  [deps & body]
  (let [names (vec (take-nth 2 deps))
        keys (vec (take-nth 2 (rest deps)))]
    `(let [~names (map clj-di.core/get-dep ~keys)]
       ~@body)))
