(def shared
  '[
    [org.clojure/tools.logging "0.2.6"]
    [org.clojure/tools.macro "0.1.2"]
    [org.clojure/core.incubator "0.1.2"]
    [org.clojure/data.json "0.2.1"]
    [clj-http "0.6.4"]
    ])

(defproject clj-duedil "0.4.4"
  :description "clj-duedil : a clojure library for the duedil api"

  :url "http://github.com/trampoline/clj-duedil"

  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}

  :min-lein-version "2.0.0"
  :repositories {"central" {:url "http://repo1.maven.org/maven2"}
                 "clojars" {:url "https://clojars.org/repo/"}}


  :plugins [[lein-midje "3.0-RC1"]
            [codox "0.6.4"]]

  :codox {:include [clj-duedil.core clj-duedil.v2-api clj-duedil.open-api]}

  :aliases {"all" ["with-profile" "dev:1.4,dev:1.5"]}
  :dependencies ~(conj shared '[org.clojure/clojure "1.5.1"])

  :profiles {:all {:dependencies ~shared}
             :dev {:dependencies [[midje "1.5-RC1"]
                                  [log4j/log4j "1.2.17"]]}
             :production {}
             :1.4 {:dependencies [[org.clojure/clojure "1.4.0"]]}
             :1.5 {:dependencies [[org.clojure/clojure "1.5.1"]]}}
  )
