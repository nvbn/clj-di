(ns clj-di.test-test
  #+cljs (:require-macros [cljs.core.async.macros :refer [go]])
  (:require #+cljs [cemerick.cljs.test :refer-macros [deftest is use-fixtures testing done]]
            #+cljs [cljs.core.async :refer [<! timeout]]
            [clj-di.test :as dt #+cljs :include-macros #+cljs true]
            [clj-di.test-ns :as t]))

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
