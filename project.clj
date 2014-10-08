(defproject clj-di "0.1.0-SNAPSHOT"
            :description "Dependency injection library for clojure and clojurescript"
            :url "https://github.com/nvbn/clj-di"
            :license {:name "Eclipse Public License"
                      :url "http://www.eclipse.org/legal/epl-v10.html"}
            :dependencies [[org.clojure/clojure "1.6.0"]
                           [org.clojure/clojurescript "0.0-2356"]]
            :plugins [[lein-cljsbuild "1.0.3"]
                      [com.keminglabs/cljx "0.4.0"]
                      [com.cemerick/clojurescript.test "0.3.1"]]
            :profiles {:uberjar {:hooks [cljx.hooks
                                         leiningen.cljsbuild]}
                       :dev {:plugins [[lein-cljsbuild "1.0.3"]
                                       [com.keminglabs/cljx "0.3.2"]
                                       [com.cemerick/clojurescript.test "0.3.1"]]
                             :cljx {:builds [{:source-paths ["src/cljx"]
                                              :output-path "target/generated-clj"
                                              :rules :clj}
                                             {:source-paths ["test/cljx"]
                                              :output-path "target/generated-clj-test"
                                              :rules :clj}
                                             {:source-paths ["src/cljs"]
                                              :output-path "target/generated-cljs"
                                              :rules :cljs}
                                             {:source-paths ["test/cljx"]
                                              :output-path "target/generated-cljs-test"
                                              :rules :cljs}]}
                             :cljsbuild {:builds [{:source-paths ["target/generated-cljs"
                                                                  "target/generated-cljs-test"]
                                                   :compiler {:output-to "target/cljs-test.js"
                                                              :optimizations :whitespace
                                                              :pretty-print true}}]
                                         :test-commands {"test" ["phantomjs" :runner
                                                                 "target/cljs-test.js"]}}}}
            :test-paths ["target/generated-clj-test"]
            :source-paths ["target/generated-clj" "target/generated-cljs" "src/cljs"])
