(ns clj-di.test
  "Helpres for tests.")

(defmacro with-fresh-dependencies
  "Test helper for running tests without registered dependencies.

  Usage:

  ```clojure
  (with-fresh-dependencies ...)
  ```

  This helper very useful with `use-fixtures`, for running all tests
  without registered dependencies use:

  ```clojure
  (use-fixtures :each
    (fn [f] (with-fresh-dependencies (f))))
  ```"
  [& body]
  `(let [old# @clj-di.core/dependencies]
     (reset! clj-di.core/dependencies {})
     ~@body
     (reset! clj-di.core/dependencies old#)))

(defmacro with-reset
  "**CLJS only**

  binding => var-symbol temp-value-expr

  Temporarily redefines vars while executing the body.  The
  temp-value-exprs will be evaluated and each resulting value will
  replace in parallel the root value of its var.  After the body is
  executed, the root values of all the vars will be set back to their
  old values. Useful for mocking out functions during testing.

  Usage:

  ```clojure
  (with-reset [http/get (fn [url] :test)]
    (is (= (http/get :url) :test)))
  ```"
  [bindings & body]
  (let [names (take-nth 2 bindings)
        vals (take-nth 2 (drop 1 bindings))
        current-vals (map #(list 'identity %) names)
        tempnames (map (comp gensym name) names)
        binds (map vector names vals)
        resets (reverse (map vector names tempnames))
        bind-value (fn [[k v]] (list 'set! k v))]
    `(let [~@(interleave tempnames current-vals)]
       (try
         ~@(map bind-value binds)
         ~@body
         (finally
           ~@(map bind-value resets))))))

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
