(ns clj-di.test)

(defmacro with-fresh-dependencies
  "Test helper for running tests with clear dependencies."
  [& body]
  `(let [old# @clj-di.core/dependencies]
     (reset! clj-di.core/dependencies {})
     ~@body
     (reset! clj-di.core/dependencies old#)))
