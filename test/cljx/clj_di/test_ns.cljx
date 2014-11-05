(ns clj-di.test-ns)

(defn test-fn [] :initial)

(defn test-fn-2 [] (test-fn))
