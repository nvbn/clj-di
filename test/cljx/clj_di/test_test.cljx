(ns clj-di.test-test
  #+cljs (:require-macros [cljs.core.async.macros :refer [go]])
  (:require #+clj [clojure.test :refer [deftest is use-fixtures testing]]
            #+cljs [cemerick.cljs.test :refer-macros [deftest is use-fixtures testing done]]
            #+cljs [cljs.core.async :refer [<! timeout]]
            [clj-di.core :as di #+cljs :include-macros #+cljs true]
            [clj-di.test :as dt #+cljs :include-macros #+cljs true]
            [clj-di.test-ns :as t]))

(use-fixtures :each (fn [f] (dt/with-fresh-dependencies (f))))

(deftest test-with-registered
  (testing "Registered dependencies should be in dependencies atom"
    (dt/with-registered [:http t/http]
                        (is (= @di/dependencies {:http [t/http]}))))
  (testing "Registered dependencies should not be in dependencies atom outside of code block"
    (is (= @di/dependencies {:http []})))
  (testing "Even if exception thrown in code block"
    (try (dt/with-registered [:http t/http]
                             (throw #+clj (Exception.) #+cljs (js/Error.)))
         (catch #+clj Exception #+cljs js/Error e
                                                (is (= @di/dependencies {:http []}))))))

(deftest test-with-fresh-dependencies
  (di/register! :dep :impl-1)
  (dt/with-fresh-dependencies
    (is (not (di/get-dep :dep)))
    (di/register! :dep :impl-2))
  (is (= (di/get-dep :dep) :impl-1)))

#+cljs (deftest test-with-reset
         (testing "should not be mocked before"
           (is (= (t/test-fn) :initial))
           (is (= (t/test-fn-2) :initial)))
         (testing "should be mocked inside"
           (dt/with-reset [t/test-fn (constantly :mocked)]
                          (is (= (t/test-fn) :mocked))
                          (is (= (t/test-fn-2) :mocked))))
         (testing "should not be mocked after"
           (is (= (t/test-fn) :initial))
           (is (= (t/test-fn-2) :initial)))
         (testing "should propagate exceptions"
           (let [exc (js/Error. "Exception")]
             (try (dt/with-reset [t/test-fn (constantly :mocked)]
                                 (throw exc))
                  (catch js/Error e (is (= e exc))))))
         (testing "should not be mocked after fail"
           (is (= (t/test-fn) :initial))
           (is (= (t/test-fn-2) :initial))))

#+cljs (deftest ^:async test-with-reset-in-go-block
         (go (testing "should not be mocked before"
               (is (= (t/test-fn) :initial))
               (is (= (t/test-fn-2) :initial)))
             (<! (timeout 0))
             (testing "should be mocked inside"
               (dt/with-reset [t/test-fn (constantly :mocked)]
                              (is (= (t/test-fn) :mocked))
                              (is (= (t/test-fn-2) :mocked))))
             (<! (timeout 0))
             (testing "should not be mocked after"
               (is (= (t/test-fn) :initial))
               (is (= (t/test-fn-2) :initial)))
             (<! (timeout 0))
             (testing "should propagate exceptions"
               (let [exc (js/Error. "Exception")]
                 (try (dt/with-reset [t/test-fn (constantly :mocked)]
                                     (throw exc))
                      (catch js/Error e (is (= e exc))))))
             (<! (timeout 0))
             (testing "should not be mocked after fail"
               (is (= (t/test-fn) :initial))
               (is (= (t/test-fn-2) :initial)))
             (done)))
