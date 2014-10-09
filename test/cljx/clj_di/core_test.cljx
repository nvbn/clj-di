(ns clj-di.core-test
  (:require #+clj [clojure.test :refer [deftest is use-fixtures testing]]
            #+cljs [cemerick.cljs.test :refer-macros [deftest is use-fixtures testing]]
            [clj-di.core :as di #+cljs :include-macros #+cljs true]
            [clj-di.test :as dt #+cljs :include-macros #+cljs true]))

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
    (is (= @di/dependencies {:http http}))))

(deftest test-forget!
  (di/register! :http http)
  (testing "Forgeted dependency should not be in dependecies atom"
    (di/forget! :http)
    (is (= @di/dependencies {}))))

(deftest test-with-registered
  (testing "Registered dependencies should be in dependencies atom"
    (di/with-registered [:http http]
      (is (= @di/dependencies {:http http}))))
  (testing "Registered dependencies should not be in dependencies atom outside of code block"
    (is (= @di/dependencies {})))
  (testing "Even if exception thrown in code block"
    (try (di/with-registered [:http http]
           (throw #+clj (Exception.) #+cljs (js/Error.)))
         (catch #+clj Exception #+cljs js/Error e
                                (is (= @di/dependencies {}))))))

(deftest test-get-dep
  (di/register! :http http)
  (testing "Registered dependency should be returned"
    (is (= http (di/get-dep :http)))))

(deftest test-let-deps
  (di/register! :http http)
  (testing "Registered dependency should be bound to lexical scope"
    (di/let-deps [http-dep :http]
      (is (= http http-dep)))))
