(ns clj-di.core
  "Functions and macros for using dependency injection.")

(def ^:no-doc dependencies (atom {}))

(defn register!
  "Register dependecies.

  Usage:

  ```clojure
  (register! :logger logger
             :http http-client)
  ```

  After that you can get dependency with [[get-dep]] or [[let-deps]]
  using dependency name (here - `:logger` and `:http`)."
  [& key-dep-pairs]
  (apply swap! dependencies assoc key-dep-pairs))

(defn forget!
  "Forget about registered dependencies.

  Usage:

  ```clojure
  (forget! :logger :http)
  ```"
  [& keys]
  (apply swap! dependencies dissoc keys))

(defmacro with-registered
  "Register dependencies, run code block and forget about dependencies.

  Usage:

  ```clojure
  (with-registered [:http http-client
                    :logger logger]
    ...)
  ```"
  [key-dep-pairs & body]
  `(do (apply clj-di.core/register! ~key-dep-pairs)
       (try (do ~@body)
            (finally (apply clj-di.core/forget! (take-nth 2 ~key-dep-pairs))))))

(defn get-dep
  "Get dependency by name.

  Usage:

  ```clojure
  (get-dep :http)
  ```

  Dependency should be registered with [[register!]]."
  [key]
  (key @dependencies))

(defmacro let-deps
  "Bind dependencies in lexical scope of code block.

  Usage:

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

  Dependencies should be registered with [[register!]]."
  [deps & body]
  (let [names (vec (take-nth 2 deps))
        keys (vec (take-nth 2 (rest deps)))]
    `(let [~names (map clj-di.core/get-dep ~keys)]
       ~@body)))
