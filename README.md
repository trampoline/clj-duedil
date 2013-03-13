# clj-duedil

[![Build Status](https://secure.travis-ci.org/trampoline/clj-duedil.png)](http://travis-ci.org/trampoline/clj-duedil)

A Clojure client for the duedil.com API

* Functions are defined for both the open API `clj-duedil.open-api` and the V2 API `clj-duedil.v2-api`
* A context carries the base-url for the API in use, and the API key. The context is created with `make-client-context`
* Function names are derived directly from [Duedil API documentation](http://developer.duedil.com/io-docs)
* Resource identifiers to be substituted into resource URLs are given as function arguments
* Parameters are given in a map argument destructured from after the resource identifiers in function calls.
  They are named as in the [Duedil API documentation](http://developer.duedil.com/io-docs). Keywords
  may be used for parameter names, though values should not use Keywords
* Keys in the response are decoded as Keywords

## Usage

Include the following dependency in your project.clj

    ["clj-duedil" "0.3.0"]

Then run

    lein deps

Which will download the specified version of `clj-duedil` from [Clojars](https://clojars.org/clj-duedil). Then :

    (require '[clj-duedil.core :as dd])
    (require '[clj-duedil.v2-api :as ddv2])

    (def cc (dd/make-client-context ddv2/v2-api-base "your-api-key"))

    ;; get company details
    (ddv2/get-company cc "03977902")

    ;; return the url which would be called
    (dd/url-only (ddv2/get-company cc "03977902"))

    ;; "http://api.duedil.com/v2/company/03977902.json?api_key=your-api-key&fields=get_all"

    ;; specify some fields and a traversal
    (dd/url-only (ddv2/get-company cc "03977902" :fields "status,accountsType" :traversal {:get "directorships"}))

    ;; "http://api.duedil.com/v2/company/03977902.json?api_key=your-api-key&traversal=%7B%22get%22%3A%22directorships%22%7D&fields=status,accountsType"

    ;; use with-client-context to avoid passing the client-context to api function
    (dd/with-client-context cc
      (ddv2/get-company "03977902")
      (ddv2/get-registered-address "03977902"))

    ;; methods which paginate results return a lazy-sequence of result-pages. each page
    ;; is a vector of results
    ;; page-size can be specified with :limit
    (ddv2/list-company-accounts cc "03977902" :limit 10)

### Available API functions

    clj-duedil.v2-api/get-company
    clj-duedil.v2-api/get-registered-address
    clj-duedil.v2-api/list-previous-company-names
    clj-duedil.v2-api/list-secondary-industries
    clj-duedil.v2-api/list-company-shareholdings
    clj-duedil.v2-api/list-company-bank-accounts
    clj-duedil.v2-api/list-company-accounts
    clj-duedil.v2-api/get-full-itemised-accounts
    clj-duedil.v2-api/list-company-documents
    clj-duedil.v2-api/get-director
    clj-duedil.v2-api/list-director-directorships
    clj-duedil.v2-api/list-credit-ratings
    clj-duedil.v2-api/list-credit-limits
    clj-duedil.v2-api/search-companies
    clj-duedil.v2-api/search-directors

    clj-duedil.open-api/company-search
    clj-duedil.open-api/united-kingdom-company-information
    clj-duedil.open-api/roi-company-information

## License

Copyright © 2013 Trampoline Systems Ltd

Distributed under the Eclipse Public License, the same as Clojure.
