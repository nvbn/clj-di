(ns clj-di.core-test
  #+cljs (:require-macros [cljs.core.async.macros :refer [go]])
  (:require #+clj [clojure.test :refer [deftest is use-fixtures testing]]
            #+cljs [cemerick.cljs.test :refer-macros [deftest is use-fixtures testing done]]
            #+cljs [cljs.core.async :refer [<! timeout]]
            [clj-di.core :as di #+cljs :include-macros #+cljs true]
            [clj-di.test :as dt #+cljs :include-macros #+cljs true]
            [clj-di.test-ns :as t]))

(use-fixtures :each (fn [f] (dt/with-fresh-dependencies (f))))

(defprotocol HttpProto
  (GET [this url])
  (POST [this url data]))

(def http (reify HttpProto
            (GET [_ url] (str "GET Http1 " url))
            (POST [_ url data] (str "POST Http1 " url " " data))))

(deftest test-register!
  (testing "Registered dependency should be in dependencies atom"
    (di/register! :http http)
    (is (= @di/dependencies {:http [http]}))))

(deftest test-forget!
  (di/register! :http http)
  (testing "Forgeted dependency should not be in dependecies atom"
    (di/forget! :http)
    (is (= @di/dependencies {:http []}))))

(deftest test-with-registered
  (testing "Registered dependencies should be in dependencies atom"
    (di/with-registered [:http http]
      (is (= @di/dependencies {:http [http]}))))
  (testing "Registered dependencies should not be in dependencies atom outside of code block"
    (is (= @di/dependencies {:http []})))
  (testing "Even if exception thrown in code block"
    (try (di/with-registered [:http http]
           (throw #+clj (Exception.) #+cljs (js/Error.)))
         (catch #+clj Exception #+cljs js/Error e
                                                (is (= @di/dependencies {:http []}))))))

(deftest test-get-dep
  (di/register! :http http)
  (testing "Registered dependency should be returned"
    (is (= http (di/get-dep :http)))))

(deftest test-let-deps
  (di/register! :http http)
  (testing "Registered dependency should be bound to lexical scope"
    (di/let-deps [http-dep :http]
                 (is (= http http-dep)))))

(di/defprotocol* logger
  (info [_ msg] [_ msg msg2])
  (warn [_ msg]))

(di/defprotocol* logger2
  "Docstring"
  (info2 [_ msg] [_ msg msg2])
  (warn2 [_ msg]))

(deftype LoggerImpl
  []
  logger
  (info [_ msg] (str "INFO: " msg))
  (info [_ msg msg2] (str "INFO: " msg " " msg2)))

(defrecord LoggerImpl2
  []
  logger2
  (info2 [_ msg] (str "INFO2: " msg))
  (info2 [_ msg msg2] (str "INFO2: " msg " " msg2)))

(deftest test-defprotocol*
  (testing "Registering dependency"
    (let [dep (LoggerImpl.)]
      (di/with-registered [:logger dep]
        (is (= dep (di/get-dep :logger))))))
  (testing "Calling proxy function"
    (di/with-registered [:logger (LoggerImpl.)
                         :logger2 (LoggerImpl2.)]
      (is (= (info* "test message") "INFO: test message"))
      (is (= (info* "test message" "!!!") "INFO: test message !!!"))
      (is (= (info2* "test message") "INFO2: test message"))
      (is (= (info2* "test message" "!!!") "INFO2: test message !!!")))))

#+cljs (deftest test-with-reset
         (testing "should not be mocked before"
           (is (= (t/test-fn) :initial))
           (is (= (t/test-fn-2) :initial)))
         (testing "should be mocked inside"
           (di/with-reset [t/test-fn (constantly :mocked)]
             (is (= (t/test-fn) :mocked))
             (is (= (t/test-fn-2) :mocked))))
         (testing "should not be mocked after"
           (is (= (t/test-fn) :initial))
           (is (= (t/test-fn-2) :initial)))
         (testing "should propagate exceptions"
           (let [exc (js/Error. "Exception")]
             (try (di/with-reset [t/test-fn (constantly :mocked)]
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
               (di/with-reset [t/test-fn (constantly :mocked)]
                 (is (= (t/test-fn) :mocked))
                 (is (= (t/test-fn-2) :mocked))))
             (<! (timeout 0))
             (testing "should not be mocked after"
               (is (= (t/test-fn) :initial))
               (is (= (t/test-fn-2) :initial)))
             (<! (timeout 0))
             (testing "should propagate exceptions"
               (let [exc (js/Error. "Exception")]
                 (try (di/with-reset [t/test-fn (constantly :mocked)]
                        (throw exc))
                      (catch js/Error e (is (= e exc))))))
             (<! (timeout 0))
             (testing "should not be mocked after fail"
               (is (= (t/test-fn) :initial))
               (is (= (t/test-fn-2) :initial)))
             (done)))
