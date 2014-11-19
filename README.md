# clj-di [![Build Status](https://travis-ci.org/nvbn/clj-di.svg)](https://travis-ci.org/nvbn/clj-di)

Dependency injection and utility belt for testing for Clojure and ClojureScript.

[API reference.](http://nvbn.github.io/clj-di/)

## Installation

Add the following to your `project.clj`:

```clojure
[clj-di "0.5.0"]
```

## Usage

Before we start, we need to import library. In Clojure:

```clojure
(ns clj-di.example
  (:require [clj-di.core :refer [register! get-dep let-deps defprotocol*]]))
```

In ClojureScript:

```clojure
(ns clj-di.example
  (:require-macros [clj-di.core :refer [let-deps defprotocol*]])
  (:require [clj-di.core :refer [register! get-dep]]))
```

### Registering dependencies

For registering dependencies you can use `register!` function, example:

```clojure
(register! :cache (atom {})
           :logger (get-logger))
```

You should call it only on application initialization.
For example: in ring `:init` handler, in `-main`.

### Receiving dependencies

For receiving single dependency you can use `get-dep` function, example:

```clojure
(get-dep :cache)
```

If you need more than one dependencies it's better to use `let-deps` macro:

```clojure
(let-deps [cache :cache
           log :logger]
  (log :info (:last-id @cache)))
```

### Create complex dependency or add ability to mock whole module

For example, you need http client as a dependency,
first way &ndash; register `http-get`, `http-post`, `http-put` dependencies:

```clojure
(register! :http-get http/get
           :http-post http/post
           :http-put http/put)
(let-deps [http-get :http-get
           http-post :http-post]
  (http-get "some-url")
  (http-post "some-url" :some-data))
```

But it's ugly. Or you can create http protocol and http type and use it as a dependency:

```clojure
(defprotocol http
  (GET [_ url])
  (POST [_ url data])
  (PUT [_ url data]))

(deftype http-impl
  (GET [_ url] (http/get url))
  (POST [_ url data] (http/post url data))
  (PUT [_ url data] (http/put url data))

(defn make-http [] (http-impl.))

(register! :http (make-http))

(let-deps [http :http]
  (GET http "some-url")
  (POST http "some-url" :some-data))
```

Less ugly, but you need to pass `http` to `GET` and `POST`.
So for simplifying this process you can use `defprotocol*` macro:

```clojure
(defprotocol* http
  (GET [_ url])
  (POST [_ url data])
  (PUT [_ url data]))

(deftype http-impl
  (GET [_ url] (http/get url))
  (POST [_ url data] (http/post url data))
  (PUT [_ url data] (http/put url data))

(defn make-http [] (http-impl.))

(register! :http (make-http))

(GET* "some-url")
(POST* "some-url" :some-data)
```

It creates function (`method-name*`) for each method in which they automatically receive
dependency, which name equals to protocol name. For example, `GET*` works like:

```clojure
(defn GET*
  [url]
  (GET (get-dep :http) url))
```

### Usage in tests

In tests you need to import test utils. In Clojure:

```clojure
(ns clj-di.example-test
  (:require [clj-di.test :refer [with-fresh-dependencies with-registered]]))
```

In ClojureScript:

```clojure
(ns clj-di.example-test
  (:require-macros [clj-di.test :refer [with-fresh-dependencies with-registered with-reset]]))
```

For clearing dependencies before and after each test you can use `with-fresh-dependencies` macro:

```clojure
(use-fixtures :each (fn [f] (with-fresh-dependencies (f))))
```

For registering dependencies in tests you can use `with-registered` macro:

```clojure
(deftest test-write-to-cache
  (with-registered [:cache (atom {})]
    (write-to-cache :test "test")
    (is (= @(get-dep :cache) {:test "test"}))))
```

For redefining variables you can use clojure `with-redefs`,
but it works incorrectly in ClojureScript inside `go` block,
so in this situation you can use `with-reset` macro:

```clojure
(deftest ^:async test-model-get
  (go (with-reset [model/fetch (fn [& _] [:a :b :c])]
        (is (= (model/get :a) :a))
        (done))))
```

## Contributing

`clj-di` written using `cljx`, so for compiling you need to run:

```bash
lein cljx auto  # watch for changes and compile
lein cljx  # compile once
```

For running tests:

```bash
lein test
lein cljsbuild test
```
