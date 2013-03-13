(ns clj-duedil.impl-test
  (:use midje.sweet
        clj-duedil.impl)
  (:require
   [clojure.data.json :as json]
   [clj-http.client :as http]))

;.;. A clean boundary between useful abstractions and the grubby code that
;.;. touches the real world is always a good thing. -- Ron Jeffries
(let [cc (->client-context "http://api.duedil.com/foo" "blahblah")]

  (fact
    (:api-base cc) => "http://api.duedil.com/foo"
    (:api-key cc) => "blahblah")

  (def-api-fn foos [:company_id] "/company/:company_id.json" [[:fields "get_all"]])


  (fact
    (foos cc 1000) => {:response "boo"}

    (provided
      (http/get "http://api.duedil.com/foo/company/1000.json?api_key=blahblah&fields=get_all") =>
      {:body (json/write-str {:response "boo"})})))
