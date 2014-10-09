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
