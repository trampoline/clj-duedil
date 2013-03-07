(ns clj-duedil.core
  (:require
   [clojure.string :as str]
   [clojure.tools.macro :as macro]
   [clojure.data.json :as json]
   [clj-http.client :as http]
   [clj-duedil.util :as util])
  (:use
   clojure.core.incubator
   clojure.core.strint))


(def duedil-v2-sandbox "http://api.duedil.com/sandbox/v2")
(def duedil-v2 "http://api.duedil.com/v2")

(defprotocol DuedilApi
  "duedil API methods"

  (url [this resource opts])
  (call [this resource opts]))

(defrecord client [api-base api-key ]

  DuedilApi
  (url [this resource opts]
    (str (apply util/join-url-components api-base (flatten [resource]))
         (util/flatten-params (merge opts {:api-key api-key}))))

  (call [this resource opts]
    (-?> (url this resource opts)
           (http/get {:as :json})
           :body)))

(defmacro def-api-method
  [fname & macro-args]
  (let [[dname [param-or-params resource-pattern valid-opt-keys]] (macro/name-with-attributes fname macro-args)
        [dname-url [_ _ _]] (macro/name-with-attributes (symbol (<< "~{fname}-url")) macro-args)]
    (let [params (map keyword (->> [param-or-params] flatten (filter identity)))
          param-names (->> params (map name) (map symbol))
          param-map (->> (map vector params param-names)
                         flatten
                         (apply hash-map))]
      `(defn ~dname
         [client# ~@param-names & {:as opts#}]
         (call client#
               (util/expand-resource-pattern ~resource-pattern ~param-map)
               (util/check-opts ~valid-opt-keys opts#)))

      `(defn ~dname-url
         [client# ~@param-names & {:as opts#}]
         (url client#
              (util/expand-resource-pattern ~resource-pattern ~param-map)
              (util/check-opts ~valid-opt-keys opts#))))))


(def-api-method get-company
  "(get-company <company-id-str> :fields <field-list-str> :traversal <traversal-maps>)"
  :company-id "company/:company-id.json" [[:fields "get_all"] [:traversal util/encode-traversals]])

(def-api-method get-registered-address
  "(get-registered-address <company-id-str> :fields <field-list-str> :traversal <traversal-maps>)"
  :company-id "company/:company-id/registered-address.json" [[:fields "get_all"] [:traversal util/encode-traversals]])

(def-api-method list-previous-company-names
  ""
  :company-id "company/:company-id/previous-company-names.json" [[:fields "get_all"] [:traversal util/encode-traversals]])

(def-api-method list-secondary-industries
  ""
  :company-id "company/:company-id/secondary-industries.json" [[:fields "get_all"] [:traversal util/encode-traversals]])

(def-api-method list-company-shareholdings
  ""
  :company-id "company/:company-id/shareholdings.json" [[:fields "get_all"] [:traversal util/encode-traversals]])

(def-api-method list-company-bank-accounts
  ""
  :company-id "company/:company-id/bank-accounts.json" [[:fields "get_all"] [:traversal util/encode-traversals]])

(def-api-method list-company-accounts
  ""
  :company-id "company/:company-id/accounts.json" [[:fields "get_all"] [:traversal util/encode-traversals]])

(def-api-method get-full-itemised-accounts
  ""
  [:company-id :accounts-id] "company/:company-id/accounts/:accounts-id.json" [[:fields "get_all"] [:traversal util/encode-traversals]])

(def-api-method list-company-documents
  ""
  :company-id "company/:company-id/documents.json" [[:fields "get_all"] [:traversal util/encode-traversals]])

(def-api-method get-director
  ""
  :director-id "director/:director-id.json" [[:fields "get_all"] [:traversal util/encode-traversals]])

(def-api-method list-director-directorships
  ""
  :director-id "director/:director-id/directorships.json" [[:fields "get_all"] [:traversal util/encode-traversals]])

(def-api-method list-credit-ratings
  ""
  :company-id "credit/company/:company-id/ratings.json" [[:fields "get_all"] [:traversal util/encode-traversals]])

(def-api-method list-credit-limits
  ""
  :company-id "credit/company/:company-id/limits.json" [[:fields "get_all"] [:traversal util/encode-traversals]])

(def-api-method search-companies
  ""
  [] "search/companies" [:query :url])

(def-api-method search-directors
  ""
  [] "search/directors" [:query])
