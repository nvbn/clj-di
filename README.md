# clj-di [![Build Status](https://travis-ci.org/nvbn/clj-di.svg)](https://travis-ci.org/nvbn/clj-di)

Dependency injection for clojure and clojurescript.

[Documentation.](http://nvbn.github.io/clj-di/)

## Installation

Add the following to your `project.clj`:

```clojure
[clj-di "0.2.0"]
```

## Usage

Example usage:

```clojure
(ns clj-di.example
  (:require [clj-di.core :refer [register! get-dep let-deps with-registered def-dep]))
  
(def-dep printer  ; define dependency with def-dep
  (print-log [this log]))
  
(deftype PrinterImpl  ; implement complex dependency
  []
  printer
  (print-log [this log] (println @log))

(defn log-write
  [msg]
  (swap! (get-dep :log) conj msg)) ; get dependency with `get-dep`
    
(defn run
  []
  (register! :log (atom []))  ; register dependency
  (log-write "test")
  (let-deps [printer :printer  ; get dependency with `let-deps`
             log :log]
    (print-log* log)))  ; call proxy-method, equals to `(.print-log (get-dep :printer) log)`  
  
(with-registered [:printer (PrinterImpl.)]  ;register dependency with `with-registered`
  (run))
```

With clojurescript you should change imports to:

```clojure
(ns clj-di.example
  (:require-macros [clj-di.core :refer [let-deps with-registered def-dep]])
  (:require [clj-di.core :refer [register! get-dep]))
```
