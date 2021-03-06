(ns clj-duedil.open-api
  (:use
   clj-duedil.impl
   clj-duedil.core))

(def open-api-base "http://api.duedil.com/open")

(def-api-fn company-search
  [] "search" [:q] :offset :limit pages)

(def-api-fn united-kingdom-company-information
  [:company_number] "uk/company/:company_number.json" [] unwrap-response)

(def-api-fn roi-company-information
  [:company_number] "roi/company/:company_number.json" [] unwrap-response)
