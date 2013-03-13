(ns clj-duedil.core
  (:use
   clj-duedil.impl))

(defmacro url-only
  "API calls in the body will only return the URL which would be called"
  [& forms]
  `(url-only* (fn [] ~@forms)))

(defn make-client-context
  "make a client-context
   - api-base : the base url for the version of the Duedil API
   - api-key : the API key corresponding to the version of the API in api-base"
  [api-base api-key]
  (->client-context api-base api-key))

(defmacro with-client-context
  "specify a default client-context for all API calls in the body
   - client-context : the default client-context"
  [client-context & forms]
  `(with-client-context* ~client-context (fn [] ~@forms)))

(defn collect-pages
  "given a sequence of API result pages, collect the results from each page into one list.
   returns page-seq if page-seq is not sequential?"
  [page-seq & [max-pages]]
  (if (sequential? page-seq)
    (let [pages (if max-pages (take max-pages page-seq) page-seq)]
      (->> pages
           (map :data)
           (apply concat)))
    page-seq))
