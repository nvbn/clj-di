(ns clj-di.core-test
  #+cljs (:require-macros [cljs.core.async.macros :refer [go]])
  (:require #+clj [clojure.test :refer [deftest is use-fixtures testing]]
            #+cljs [cemerick.cljs.test :refer-macros [deftest is use-fixtures testing done]]
            #+cljs [cljs.core.async :refer [<! timeout]]
            [clj-di.core :as di #+cljs :include-macros #+cljs true]
            [clj-di.test :as dt #+cljs :include-macros #+cljs true]
            [clj-di.test-ns :as t]))

(use-fixtures :each (fn [f] (dt/with-fresh-dependencies (f))))

(deftest test-register!
  (testing "Registered dependency should be in dependencies atom"
    (di/register! :http t/http)
    (is (= @di/dependencies {:http [t/http]}))))

(deftest test-forget!
  (di/register! :http t/http)
  (testing "Forgeted dependency should not be in dependecies atom"
    (di/forget! :http)
    (is (= @di/dependencies {:http []}))))

(deftest test-get-dep
  (di/register! :http t/http)
  (testing "Registered dependency should be returned"
    (is (= t/http (di/get-dep :http)))))

(deftest test-let-deps
  (di/register! :http t/http)
  (testing "Registered dependency should be bound to lexical scope"
    (di/let-deps [http-dep :http]
                 (is (= t/http http-dep)))))

(deftest test-defprotocol*
  (testing "Registering dependency"
    (let [dep (t/make-logger-impl)]
      (dt/with-registered [:logger dep]
        (is (= dep (di/get-dep :logger))))))
  (testing "Calling proxy function"
    (dt/with-registered [:logger (t/make-logger-impl)
                         :logger2 (t/make-logger-iml2)]
      (is (= (t/info* "test message") "INFO: test message"))
      (is (= (t/info* "test message" "!!!") "INFO: test message !!!"))
      (is (= (t/info2* "test message") "INFO2: test message"))
      (is (= (t/info2* "test message" "!!!") "INFO2: test message !!!")))))
