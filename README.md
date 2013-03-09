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

    (require '[clj-duedil.core :as dd])
    (require '[clj-duedil.v2-api :as ddv2])

    (def c (dd/make-client-context ddv2/v2-api-base "your-api-key"))

    ;; get company details
    (ddv2/get-company c "03977902")

    ;; return the url which would be called
    (dd/url-only (ddv2/get-company c "03977902"))

    ;; "http://api.duedil.com/v2/company/03977902.json?api_key=your-api-key&fields=get_all"

    ;; specify some fields and a traversal
    (dd/url-only (ddv2/get-company c "03977902" :fields "status,accountsType" :traversal {:get "directorships"}))

    ;; "http://api.duedil.com/v2/company/03977902.json?api_key=your-api-key&traversal=%7B%22get%22%3A%22directorships%22%7D&fields=status,accountsType"

## License

Copyright © 2013 Trampoline Systems Ltd

Distributed under the Eclipse Public License, the same as Clojure.
