(ns clj-di.core-test
  (:require #+clj [clojure.test :refer [deftest is use-fixtures]]
            #+cljs [cemerick.cljs.test :refer-macros [deftest is use-fixtures]]
            [clj-di.core :as di #+cljs :include-macros #+cljs true]))

(use-fixtures :each (fn [f] (di/with-fresh-dependencies (f))))

(defprotocol HttpProto
  (GET [this url])
  (POST [this url data]))

(def http (reify HttpProto
             (GET [_ url] (str "GET Http1 " url))
             (POST [_ url data] (str "POST Http1 " url " " data))))

(deftest test-register!
  (di/register! :http http)
  (is (= @di/dependencies {:http http})))

(deftest test-get-dep
  (di/register! :http http)
  (is (= http (di/get-dep :http))))

(deftest test-let-deps
  (di/register! :http http)
  (di/let-deps [http-dep :http]
    (is (= http http-dep))))
