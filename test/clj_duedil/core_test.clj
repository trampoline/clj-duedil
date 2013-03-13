(ns clj-duedil.core-test
  (:use midje.sweet
        clj-duedil.core)
  (:require
   [clojure.data.json :as json]
   [clj-http.client :as http]
   [clj-duedil.impl :as impl]))

(fact
  impl/*url-only* false
  (url-only impl/*url-only*) => true)

(def c (make-client-context "http://api.duedil.com/foo" "blahblah"))

(fact
  (:api-base c) => "http://api.duedil.com/foo"
  (:api-key c) => "blahblah")

(fact
  impl/*default-client-context* => nil

  (with-client-context c
    impl/*default-client-context* => c))

(impl/def-api-fn foos [:company_id] "/company/:company_id.json" [[:fields "get_all"]])

(fact
  (url-only
   (foos c 1000) => "http://api.duedil.com/foo/company/1000.json?api_key=blahblah&fields=get_all"
   (foos c 1010 :fields "foo,bar") => "http://api.duedil.com/foo/company/1010.json?api_key=blahblah&fields=foo,bar"))


(fact

  (with-client-context c
    (foos 1000)) => {:response "boo"}

    (provided
      (http/get "http://api.duedil.com/foo/company/1000.json?api_key=blahblah&fields=get_all") =>
      {:body (json/write-str {:response "boo"})}))

(fact

  (foos 1000) => (throws RuntimeException #"must set \*default-client-context\*"))

(fact
  (pages c (fn [] {:response {:pagination "http://api.duedil.com/foo?offset=5"}})) =>
  '({:response {:pagination "http://api.duedil.com/foo?offset=5"}}
    {:response {:pagination "http://api.duedil.com/foo?offset=10"}}
    {:response {:pagination "http://api.duedil.com/foo?last_result=1"}})

  (provided
    (http/get "http://api.duedil.com/foo?offset=5&api_key=blahblah") =>
    {:body (json/write-str {:response {:pagination "http://api.duedil.com/foo?offset=10"}})}

    (http/get "http://api.duedil.com/foo?offset=10&api_key=blahblah") =>
    {:body (json/write-str {:response {:pagination "http://api.duedil.com/foo?last_result=1"}})}
    )

  )
