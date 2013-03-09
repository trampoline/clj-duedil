(ns clj-duedil.core
  (:require
   [clojure.tools.macro :as macro]
   [clojure.data.json :as json]
   [clj-http.client :as http]
   [clj-duedil.util :as util])
  (:use
   clojure.core.incubator
   clojure.core.strint))

(def ^:dynamic *url-only* false)

(defn url-only*
  [f]
  (with-bindings {#'*url-only* true}
    (f)))

(defmacro url-only
  "API calls in the body will only return the URL which would be called"
  [& forms]
  `(url-only* (fn [] ~@forms)))

(defprotocol ApiContext
  "duedil API methods"
  (call [this resource opts]))

(defrecord client-context [api-base api-key]

  ApiContext
  (call [this resource opts]
    (let [url (util/api-url api-base api-key resource opts)]
      (if-not *url-only*
        (-?> url
             http/get
             :body
             (json/read-str :key-fn keyword))
        url))))

(defn make-client-context
  [api-base api-key]
  (->client-context api-base api-key))

(defmacro def-api-fn
  "def a function which will call an API method
   - name : the function name
   - param-or-params : the parameters to be substituted into the resource url
   - resource-pattern : the resource url patten. keys will be substituted with values from the function arglist
   - opt-defs : vector of option defs. each may be a key or a [key default-value|processor-fn] pair"
  [fname & macro-args]
  (let [[dname [param-or-params resource-pattern opt-defs]] (macro/name-with-attributes fname macro-args)]

    (let [param-keywords (map keyword (->> [param-or-params] flatten (filter identity)))
          param-symbols (->> param-keywords (map name) (map symbol))
          param-map (->> (map vector param-keywords param-symbols)
                         (into {}))]
      `(defn ~dname
         [client# ~@param-symbols & {:as opts#}]
         (call client#
               (util/expand-resource-pattern ~resource-pattern ~param-map)
               (util/check-opts ~opt-defs opts#))))))
