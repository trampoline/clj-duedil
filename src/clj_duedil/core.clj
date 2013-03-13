(ns clj-duedil.core
  (:use
   clj-duedil.impl))

(defn url-only*
  [f]
  (with-bindings {#'*url-only* true}
    (f)))

(defmacro url-only
  "API calls in the body will only return the URL which would be called"
  [& forms]
  `(url-only* (fn [] ~@forms)))

(defn make-client-context
  [api-base api-key]
  (->client-context api-base api-key))

(defn with-client-context*
  [cc f]
  (if-not (instance? clj_duedil.impl.ClientContext cc)
    (throw (RuntimeException. "cc must be a client-context")))
  (with-bindings {#'*default-client-context* cc}
    (f)))

(defmacro with-client-context
  [cc & forms]
  `(with-client-context* ~cc (fn [] ~@forms)))

(defn next-page
  ([client-context api-result]
     (call-next-page client-context api-result))
  ([api-result]
     (if (nil? *default-client-context*)
       (throw (RuntimeException. "must set *default-client-context* if not explicitly passing a client-context")))
     (call-next-page *default-client-context* api-result)))

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
