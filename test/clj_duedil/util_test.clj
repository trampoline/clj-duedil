(ns clj-duedil.util-test
  (:use midje.sweet
        clj-duedil.util)
  (:require [clojure.string :as str]
            [clojure.data.json :as json]
            [clj-http.client :as http]
            [clojure.tools.logging :as log]))


(fact
  (strip-last-slash "foo/") => "foo"
  (strip-last-slash "foo") => "foo")

(fact
  (strip-first-slash "/foo") => "foo"
  (strip-first-slash "foo") => "foo")

(fact
  (join-url-components "foo" "/bar/" "baz") => "foo/bar/baz")

(fact
  (flatten-params {}) => ""
  (flatten-params {:foo "bar"}) => "?foo=bar"
  (flatten-params {:foo "bar" :baz "barbar"}) => "?foo=bar&baz=barbar"
  (flatten-params {:company_id 100}) => "?company_id=100")

(fact
  (expand-resource-pattern "/foo/:bar.json" {:bar 10}) => "/foo/10.json"
  (expand-resource-pattern "/foo/:company_id/:person_id.json" {:company_id 100 :person_id 22}) => "/foo/100/22.json"
  (expand-resource-pattern "/foo/:bar.json" {:bar 10 :company_id 20}) => (throws RuntimeException #"must contain key: :company_id"))

(fact
  (opt-keys [:foo]) => [:foo]
  (opt-keys [:foo [:bar 10]]) => [:foo :bar]
  (opt-keys [:foo [:bar (fn [v] v)] [:baz 10]]) => [:foo :bar :baz])


(fact
  (check-opts [:foo] {:foo "boo"}) => {:foo "boo"}
  (check-opts [[:foo 100]] {:foo 10}) => {:foo 10}
  (check-opts [[:foo 100] :bar] {:bar 20}) => {:foo 100 :bar 20}
  (check-opts [[:foo 100] :bar :baz] {:bar 20}) => {:foo 100 :bar 20}
  (check-opts [:foo] {:foo 10 :bar 20}) => (throws RuntimeException #"unknown option keys: \[:bar\]")
  (check-opts [[:foo str/upper-case]] {:foo "bar"}) => {:foo "BAR"})

(fact
  (encode-traversals {:get :directorships}) => "%7B%22get%22%3A%22directorships%22%7D"
  (encode-traversals [{:get :directorships} {:get "directors"}]) => "%5B%7B%22get%22%3A%22directorships%22%7D%2C%7B%22get%22%3A%22directors%22%7D%5D")

(fact
  (api-url "http://api.duedil.com/open" "blahblah" "company/1234" {:foo 10 :bar "boo"}) =>
  "http://api.duedil.com/open/company/1234?api_key=blahblah&foo=10&bar=boo")

(fact
  (next-page-url "blarghle" {:response {:pagination "http://api.duedil.com/blah?offset=50"}}) =>
  "http://api.duedil.com/blah?offset=50&api_key=blarghle"

  (next-page-url "blarghle" {:response {:pagination "http://api.duedil.com/blah?offset=50&last_result=1"}}) => nil
  (next-page-url "blarghle" {:response {:pagination "http://api.duedil.com/blah?last_result=1&offset=50"}}) => nil
  (next-page-url "blarghle" {:response {:pagination "http://api.duedil.com/blah?limit=10&last_result=1&offset=50"}}) => nil)


(fact
  (api-call "http://api.duedil.com/foo") => {:response {:data {:foo 100}}}

  (provided
    (log/log* anything :info nil "GET: http://api.duedil.com/foo") => true
    (http/get "http://api.duedil.com/foo") => {:body (json/write-str {:response {:data {:foo 100}}})}))

(fact
  (api-call nil) => nil

  (provided
    (log/log* anything :info nil "finished") => true))
