(ns clj-di.core-test
  (:require #+clj [clojure.test :refer [deftest is use-fixtures]]
            #+cljs [cemerick.cljs.test :refer-macros [deftest is use-fixtures]]
            [clj-di.core :as di #+cljs :include-macros #+cljs true]))

(use-fixtures :each (fn [f] (di/with-fresh-dependencies (f))))

(defprotocol HttpProto
  (GET [this url])
  (POST [this url data]))

(def http1 (reify HttpProto
             (GET [_ url] (str "GET Http1 " url))
             (POST [_ url data] (str "POST Http1 " url " " data))))

(def http2 (reify HttpProto
             (GET [_ url] (str "GET Http2 " url))
             (POST [_ url data] (str "POST Http2 " url " " data))))

(deftest test-register!
  (di/register! http1)
  (di/register! http1)
  (is (= @di/dependencies [http1]))
  (di/register! http2)
  (is (= @di/dependencies [http1 http2])))

(deftest test-forget!
  (di/register! http1)
  (di/register! http2)
  (di/forget! http1)
  (is (= @di/dependencies [http2])))

(deftest test-dependency
  (di/register! http1)
  (= http1 (di/dependency HttpProto))
  (di/register! http2)
  (= http2 (di/dependency HttpProto))
  (di/forget! http2)
  (= http1 (di/dependency HttpProto)))
