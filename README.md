# clj-di [![Build Status](https://travis-ci.org/nvbn/clj-di.svg)](https://travis-ci.org/nvbn/clj-di)

Dependency injection for clojure and clojurescript

## Installation

Add the following to your `project.clj`:

[![Clojars Project](http://clojars.org/clj-di/latest-version.svg)](http://clojars.org/clj-di)

## Usage

`clj-di.core/register` &ndash;  register dependency:

```clojure
(register! :dependency-name dependency)
```

`clj-di.core/get-dep` &ndash; get dependency:

```clojure
(get-dep :dependency-name)
```

`clj-di.core/let-deps` &ndash; macro for getting dependencies in let-like form:

```clojure
(let-deps [http :http
           log :log]
  (-> (GET http "http://clojure.org")
      (info "received: ")))
```

For running tests without registered dependencies you can use `clj-di.test/with-fresh-dependencies`.

Example with clojure:

```clojure
(ns clj-di.example
  (:require [clj-di.core :refer [register! get-dep let-deps]))

(defn log-write
  [msg]
  (let-deps [log :log]  ; get dependency with `let-deps`
    (swap! log conj)))
    
(defn run
  []
  (register! :log (atom []))  ; register dependency
  (log-write "test")
  (println @(get-dep :log)))  ; get dependency with get-dep
  
(run)

```

Example with clojurescript:

```clojure
(ns clj-di.example
  (:require-macros [clj-di.core :refer [let-deps]])
  (:require [clj-di.core :refer [register! get-dep]))

(defn log-write
  [msg]
  (let-deps [log :log]  ; get dependency with `let-deps`
    (swap! log conj)))
    
(defn run
  []
  (register! :log (atom []))  ; register dependency
  (log-write "test")
  (println @(get-dep :log)))  ; get dependency with get-dep
  
(run)

```
