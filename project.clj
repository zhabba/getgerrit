(defproject getgerrit "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "EPL-2.0 OR GPL-2.0-or-later WITH Classpath-exception-2.0"
            :url "https://www.eclipse.org/legal/epl-2.0/"}
  :dependencies [[org.clojure/clojure "1.9.0"]
                 [clj-http "3.9.1"]
                 [clj-jgit "0.8.10"]
                 [cheshire "5.8.1"]
                 [org.clojure/tools.cli "0.4.2"]]
  :repl-options {:init-ns getgerrit.core}
  :main getgerrit.core
  :profiles {:uberjar {:aot :all}})
