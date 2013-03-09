(ns clj-duedil.v2-api
  (:use
   clj-duedil.util
   clj-duedil.core))

(def v2-sandbox-api-base "http://api.duedil.com/sandbox/v2")
(def v2-api-base "http://api.duedil.com/v2")

(def-api-fn get-company
  :company_id "company/:company_id.json" [[:fields "get_all"] [:traversal encode-traversals]])

(def-api-fn get-registered-address
  :company_id "company/:company_id/registered-address.json" [[:fields "get_all"] [:traversal encode-traversals]])

(def-api-fn list-previous-company-names
  :company_id "company/:company_id/previous-company-names.json" [[:fields "get_all"] [:traversal encode-traversals]])

(def-api-fn list-secondary-industries
  :company_id "company/:company_id/secondary-industries.json" [[:fields "get_all"] [:traversal encode-traversals]])

(def-api-fn list-company-shareholdings
  :company_id "company/:company_id/shareholdings.json" [[:fields "get_all"] [:traversal encode-traversals]])

(def-api-fn list-company-bank-accounts
  :company_id "company/:company_id/bank-accounts.json" [[:fields "get_all"] [:traversal encode-traversals]])

(def-api-fn list-company-accounts
  :company_id "company/:company_id/accounts.json" [[:fields "get_all"] [:traversal encode-traversals]])

(def-api-fn get-full-itemised-accounts
  [:company_id :accounts-id] "company/:company_id/accounts/:accounts-id.json" [[:fields "get_all"] [:traversal encode-traversals]])

(def-api-fn list-company-documents
  :company_id "company/:company_id/documents.json" [[:fields "get_all"] [:traversal encode-traversals]])

(def-api-fn get-director
  :director-id "director/:director-id.json" [[:fields "get_all"] [:traversal encode-traversals]])

(def-api-fn list-director-directorships
  :director-id "director/:director-id/directorships.json" [[:fields "get_all"] [:traversal encode-traversals]])

(def-api-fn list-credit-ratings
  :company_id "credit/company/:company_id/ratings.json" [[:fields "get_all"] [:traversal encode-traversals]])

(def-api-fn list-credit-limits
  :company_id "credit/company/:company_id/limits.json" [[:fields "get_all"] [:traversal encode-traversals]])

(def-api-fn search-companies
  [] "search/companies" [:query :url])

(def-api-fn search-directors
  [] "search/directors" [:query])
