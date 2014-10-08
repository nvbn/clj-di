# clj-di

Dependency injection library for clojure and clojurescript

## Installation

Add the following to your `project.clj`:

```clojure
[clj-di "0.1.0"]
```

## Usage

`clj-di.test/register` &ndash;  register dependency:

```clojure
(register! :dependency-name dependency)
```

`clj-di.test/get-dep` &ndash; get dependency:

```clojure
(get-dep :dependency-name)
```

`clj-di.test/let-deps` &ndash; macro for getting dependencies in let-like form:

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
