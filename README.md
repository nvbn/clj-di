# clj-di [![Build Status](https://travis-ci.org/nvbn/clj-di.svg)](https://travis-ci.org/nvbn/clj-di)

Dependency injection for clojure and clojurescript.

[Documentation.](http://nvbn.github.io/clj-di/)

## Installation

Add the following to your `project.clj`:

[![Clojars Project](https://clojars.org/clj-di/latest-version.svg)](http://clojars.org/clj-di)

## Usage

Example with clojure:

```clojure
(ns clj-di.example
  (:require [clj-di.core :refer [register! get-dep let-deps with-registered]))

(defn log-write
  [msg]
  (swap! (get-dep :log) conj)) ; get dependency with `get-dep`
    
(defn run
  []
  (register! :log (atom []))  ; register dependency
  (log-write "test")
  (let-deps [printer :printer  ; get dependency with `let-deps`
             log :log]
    (printer @log)))  
  
(with-registered [:printer println]  ;register dependency with `with-registered`
  (run))

```

Example with clojurescript:

```clojure
(ns clj-di.example
  (:require-macros [clj-di.core :refer [let-deps with-registered]])
  (:require [clj-di.core :refer [register! get-dep]))

(defn log-write
  [msg]
  (swap! (get-dep :log) conj)) ; get dependency with `get-dep`
    
(defn run
  []
  (register! :log (atom []))  ; register dependency
  (log-write "test")
  (let-deps [printer :printer  ; get dependency with `let-deps`
             log :log]
    (printer @log)))  
  
(with-registered [:printer println]  ;register dependency with `with-registered`
  (run))

```
