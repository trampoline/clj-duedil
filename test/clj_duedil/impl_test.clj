(ns clj-duedil.impl-test
  (:use midje.sweet
        clj-duedil.impl)
  (:require
   [clojure.data.json :as json]
   [clj-http.client :as http]))


(def cc (->client-context "http://api.duedil.com/foo" "blahblah"))

(fact
  (:api-base cc) => "http://api.duedil.com/foo"
  (:api-key cc) => "blahblah")

(fact
  (next-page cc {:response {:pagination "http://api.duedil.com/foo?offset=10"}}) =>
  {:response "boo"}

  (provided
    (http/get "http://api.duedil.com/foo?offset=10&api_key=blahblah") =>
    {:body (json/write-str {:response "boo"})}))

(fact
  (next-page cc {:response {:pagination "http://api.duedil.com/foo?offset=10&last_result=1"}}) =>
  nil)


(def-api-fn foos [:company_id] "/company/:company_id.json" [[:fields "get_all"]])

(fact
  (foos cc 1000) => {:response "boo"}

  (provided
    (http/get "http://api.duedil.com/foo/company/1000.json?api_key=blahblah&fields=get_all") =>
    {:body (json/write-str {:response "boo"})}))

(fact
  (pages cc {:response {:pagination "http://api.duedil.com/foo?offset=5" :data [{:id 1} {:id 2}]}}) =>
  '([{:id 1} {:id 2}]
      [{:id 3} {:id 4}]
        [{:id 5} {:id 6}])

  (provided
    (http/get "http://api.duedil.com/foo?offset=5&api_key=blahblah") =>
    {:body (json/write-str {:response {:pagination "http://api.duedil.com/foo?offset=10" :data [{:id 3} {:id 4}]}})}

    (http/get "http://api.duedil.com/foo?offset=10&api_key=blahblah") =>
    {:body (json/write-str {:response {:pagination "http://api.duedil.com/foo?last_result=1" :data [{:id 5} {:id 6}]}})}
    ))

(def-api-fn bars [:bar_id] "/bars/:bar_id.json" [[:fields "get_all"]] (fn [cc r] {:cc cc :bar r}))

(fact
  (bars cc 1000) => {:cc cc :bar {:response "booboo"}}

  (provided
    (http/get "http://api.duedil.com/foo/bars/1000.json?api_key=blahblah&fields=get_all") =>
    {:body (json/write-str {:response "booboo"})}))

(fact
  (unwrap-response cc {:response {:foo 10}}) => {:foo 10}

  (unwrap-response cc {:response {:foo 10} :traversals [:foo :bar :baz]}) => {:response {:foo 10} :traversals [:foo :bar :baz]}
  )
