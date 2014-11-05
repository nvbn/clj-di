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
  (doseq [[key dep] (partition-all 2 key-dep-pairs)]
    (swap! dependencies update-in [key] #(conj % dep))))

(defn forget!
  "Forget about registered dependencies.

  Usage:

  ```clojure
  (forget! :logger :http)
  ```"
  [& keys]
  (doseq [key keys]
    (swap! dependencies update-in [key] rest)))

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

  Dependency should be registered with [[register!]] or [[with-registered]]."
  [key]
  (-> @dependencies key first))

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

  Dependencies should be registered with [[register!]] or [[with-registered]]."
  [deps & body]
  (let [names (vec (take-nth 2 deps))
        keys (vec (take-nth 2 (rest deps)))]
    `(let [~names (map clj-di.core/get-dep ~keys)]
       ~@body)))

(defmacro ^:no-doc call-protocol
  "Leverages differences in work with protocols with clojure and clojurescript."
  [fn-name name args]
  (if (boolean (:ns &env))
    `(~fn-name (clj-di.core/get-dep ~(keyword name)) ~@args)
    `(. (clj-di.core/get-dep ~(keyword name)) ~fn-name ~@args)))

(defn- ^:no-doc get-name-arities-pairs
  "Returns name-arities pairs from list of method definitions of protocol."
  [methods]
  (for [[method-name & arities] methods]
    [method-name (map rest arities)]))

(defn- ^:no-doc without-docstring
  "Returns body of `defprotocol` without docstring."
  [body]
  (if (string? (first body))
    (rest body)
    body))

(defn- ^:no-doc get-fn-bodies
  "Returns fn bodies for name and arities."
  [name fn-name arities]
  (for [arity arities]
    `(~(vec arity) (clj-di.core/call-protocol ~fn-name ~name ~arity))))

(defmacro defprotocol*
  "Defines protocol and creates proxy methods where protocol implementation
  received as a dependency.

  For defining dependency you should:

  ```clojure
  (defprotocol* http-client
    (get [_ url request])
    (post [_ url request]))
  ```

  After that you have `http-client` protocol and `get*` and `post*` proxy functions.

  Then you need to implement protocol:

  ```clojure
  (deftype HttpClient
    []
    http-client
    (get [_ url request] (http/get url request))
    (post [_ url request] (http/post url request)))
  ```

  And register dependency:

  ```clojure
  (register! :http-client (HttpClient.))
  ```

  And after that you can use proxy functions, like:

  ```clojure
  (get* \"http://nvbn.github.io/\" {})
  (post* \"http://nvbn.github.io/\" {:transit-params {:a 1 :b 2}})
  ```"
  [name & body]
  (apply vector `(defprotocol ~name ~@body)
         (for [[fn-name arities] (get-name-arities-pairs (without-docstring body))
               :let [fn-bodies (get-fn-bodies name fn-name arities)]]
           `(defn ~(symbol (str fn-name "*"))
              ~@fn-bodies))))

(defn ^:no-doc with-reset!-once
  "Utility function for temporary redefining single var."
  [[a-var a-val] body]
  `(let [prev-val# [~a-var]]
     (set! ~a-var ~a-val)
     (try (do ~@body)
          (catch js/Error e# (throw e#))
          (finally (set! ~a-var (first prev-val#))))))

(defmacro with-reset
  "**CLJS only**

  Temporarily redefines vars while executing the body.
  Works like `with-redefs` but can work inside go-block.

  Usage:

  ```clojure
  (with-reset [http/get (fn [url] {:body url})]
    ...)
  ```
  "
  [bindings & body]
  (let [wrapper-fn (->> (partition-all 2 bindings)
                        (map #(partial with-reset!-once %))
                        (apply comp))]
    (wrapper-fn body)))
