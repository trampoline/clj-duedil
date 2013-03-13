(ns clj-duedil.impl
  (:require
   [clojure.tools.macro :as macro]
   [clj-duedil.util :as util]))

(def ^:dynamic *url-only* false)

(defprotocol ClientContext
  "duedil API methods"
  (call [this method opts]
    "call an API method with options")
  (call-next-page [this api-result]
    "given a result of call or call-next-page, fetch the next page of results, or nil"))

(def ^:dynamic *default-client-context* nil)

(defn client-context-arglist
  "split an arg-list with an optional client-context. returns
   [client-context arglist]"
  [args]
  (if (instance? clj_duedil.impl.ClientContext (first args))
    [(first args) (rest args)]

    (do
      (if-not *default-client-context*
        (throw (RuntimeException. "must set *default-client-context* if not explicitly passing a client-context")))

      [*default-client-context* args])))


(defrecord client-context [api-base api-key]
  ClientContext
  (call [this resource opts]
    (let [url (util/api-url api-base api-key resource opts)]
      (if-not *url-only*
        (util/api-call url)
        url)))

  (call-next-page [this api-result]
    (let [next-page-url (util/next-page-url api-key api-result)]
      (if-not *url-only*
        (util/api-call next-page-url)
        next-page-url))))

(defn parse-api-fn-args
  "parse the arg list of an api-fn call. returns
   [client-context param-values opts-map]"
  [param-count call-args]
  (let [[client-context param-opts] (client-context-arglist call-args)]
    [client-context
     (take param-count param-opts)
     (apply hash-map (drop param-count param-opts))]))

(defn api-fn*
  "implementation function for def-api-fn macro"
  [params resource-pattern opt-defs call-args]
  ;; (clojure.pprint/pprint (list 'api-fn* params resource-pattern opt-defs call-args))
  (let [[client-context param-values opts] (parse-api-fn-args (count params) call-args)
        param-map (->> (map vector params param-values) (into {}))]

    (call client-context
          (util/expand-resource-pattern resource-pattern param-map)
          (util/check-opts opt-defs opts))))

(defmacro def-api-fn
  "def a function which will call an API method
   - name : the function name
   - param-or-params : the parameters to be substituted into the resource url
   - resource-pattern : the resource url patten. keys will be substituted with values from the function arglist
   - opt-defs : vector of option defs. each may be a key or a [key default-value|processor-fn] pair"
  [fname & macro-args]
  (let [[dname [param-or-params resource-pattern opt-defs]] (macro/name-with-attributes fname macro-args)]

    (let [param-keywords (->> [param-or-params] flatten (filter identity) (map keyword) vec)
          param-symbols (->> param-keywords (map name) (map symbol) vec)
          opt-keys (->> (util/opt-keys opt-defs) (map name) (map symbol))
          arglists `(quote ([~'client-context? ~@param-symbols & {:keys [~@opt-keys]}]))
          dname-arglists (with-meta dname (merge (meta dname) {:arglists arglists}))]
      `(def ~dname-arglists
         (fn [& args#]
           (api-fn* ~param-keywords ~resource-pattern ~opt-defs args#))))))
