(ns clj-duedil.util-test
  (:use midje.sweet
        clj-duedil.util))


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
  (flatten-params {:company-id 100}) => "?company_id=100")

(fact
  (expand-resource-pattern "/foo/:bar.json" {:bar 10}) => "/foo/10.json"
  (expand-resource-pattern "/foo/:company-id/:person-id.json" {:company-id 100 :person-id 22}) => "/foo/100/22.json"
  (expand-resource-pattern "/foo/:bar.json" {:bar 10 :company-id 20}) => (throws RuntimeException #"does not contain key: :company-id"))


(fact
  (check-opts [:foo] {:foo "boo"}) => {:foo "boo"}
  (check-opts [[:foo 100]] {:foo 10}) => {:foo 10}
  (check-opts [[:foo 100] :bar] {:bar 20}) => {:foo 100 :bar 20}
  (check-opts [[:foo 100] :bar :baz] {:bar 20}) => {:foo 100 :bar 20}
  (check-opts [:foo] {:foo 10 :bar 20}) => (throws RuntimeException #"unknown option keys: \[:bar\]"))
