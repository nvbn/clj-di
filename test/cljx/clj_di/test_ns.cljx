(ns clj-di.test-ns
  (:require [clj-di.core :as di #+cljs :include-macros #+cljs true]))

(defn test-fn [] :initial)

(defn test-fn-2 [] (test-fn))

(defprotocol HttpProto
  (GET [this url])
  (POST [this url data]))

(def http (reify HttpProto
            (GET [_ url] (str "GET Http1 " url))
            (POST [_ url data] (str "POST Http1 " url " " data))))

(di/defprotocol* logger
  (info [_ msg] [_ msg msg2])
  (warn [_ msg]))

(di/defprotocol* logger2
  "Docstring"
  (info2 [_ msg] [_ msg msg2])
  (warn2 [_ msg]))

(deftype logger-impl
  []
  logger
  (info [_ msg] (str "INFO: " msg))
  (info [_ msg msg2] (str "INFO: " msg " " msg2)))

(defrecord logger-impl2
  []
  logger2
  (info2 [_ msg] (str "INFO2: " msg))
  (info2 [_ msg msg2] (str "INFO2: " msg " " msg2)))

(defn make-logger-impl [] (logger-impl.))

(defn make-logger-iml2 [] (logger-impl2.))
