(ns clj-duedil.core
  (:use
   clj-duedil.impl))

(defmacro url-only
  "API calls in the body will only return the URL which would be called"
  [& forms]
  `(url-only* (fn [] ~@forms)))

(defn make-client-context
  [api-base api-key]
  (->client-context api-base api-key))

(defmacro with-client-context
  [cc & forms]
  `(with-client-context* ~cc (fn [] ~@forms)))

(defn pages
  "a lazy seq of pages : iterates with next-page
   - f : a function to be called for the first page of results"
  ([f]
     (->> (f)
          (iterate (fn [r] (next-page r)))
          (take-while identity)))
  ([client-context f]
     (->> (f)
          (iterate (fn [r] (next-page client-context r)))
          (take-while identity))))
