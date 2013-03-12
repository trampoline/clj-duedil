(ns clj-duedil.core-test
  (:use midje.sweet
        clj-duedil.core)
  (:require
   [clojure.data.json :as json]
   [clj-http.client :as http]))

(fact
  *url-only* false
  (url-only *url-only*) => true)

(let [c (make-client-context "http://api.duedil.com" "blahblah")]
  (fact
    (:api-base c) => "http://api.duedil.com"
    (:api-key c) => "blahblah"))

(let [c (make-client-context "http://api.duedil.com/foo" "blahblah")]
  (fact

    *default-client-context* => nil

    (with-client-context c
      *default-client-context* => c)))

(def-api-fn foos [:company_id] "/company/:company_id.json" [[:fields "get_all"]])

(let [c (make-client-context "http://api.duedil.com/foo" "blahblah")]
  (fact
    (url-only
     (foos c 1000) => "http://api.duedil.com/foo/company/1000.json?api_key=blahblah&fields=get_all"
     (foos c 1010 :fields "foo,bar") => "http://api.duedil.com/foo/company/1010.json?api_key=blahblah&fields=foo,bar")))

(let [c (make-client-context "http://api.duedil.com/foo" "blahblah")]
  (fact
    (foos c 1000) => {:response "boo"}

    (provided
      (http/get "http://api.duedil.com/foo/company/1000.json?api_key=blahblah&fields=get_all") =>
      {:body (json/write-str {:response "boo"})})))


(let [c (make-client-context "http://api.duedil.com/foo" "blahblah")]
  (fact

    (with-client-context c
      (foos 1000)) => {:response "boo"}

    (provided
      (http/get "http://api.duedil.com/foo/company/1000.json?api_key=blahblah&fields=get_all") =>
      {:body (json/write-str {:response "boo"})})))


(let [c (make-client-context "http://api.duedil.com/foo" "blahblah")]
  (fact

    (foos 1000) => (throws RuntimeException #"must set \*default-client-context\*")))
