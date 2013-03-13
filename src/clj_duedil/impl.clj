(ns clj-duedil.impl
  (:use clj-duedil.protocols)
  (:require
   [clojure.tools.macro :as macro]
   [clj-duedil.util :as util]))

(def ^:dynamic *url-only* false)

(defn url-only*
  [f]
  (with-bindings {#'*url-only* true}
    (f)))

(def ^:dynamic *default-client-context* nil)

(defn with-client-context*
  [cc f]
  (if-not (instance? clj_duedil.protocols.ClientContext cc)
    (throw (RuntimeException. "cc must be a client-context")))
  (with-bindings {#'*default-client-context* cc}
    (f)))

(defn client-context-arglist
  "split an arg-list with an optional client-context. returns
   [client-context arglist]"
  [args]
  (if (instance? clj_duedil.protocols.ClientContext (first args))
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

(defn next-page
  ([client-context api-result]
     (call-next-page client-context api-result))
  ([api-result]
     (if (nil? *default-client-context*)
       (throw (RuntimeException. "must set *default-client-context* if not explicitly passing a client-context")))
     (call-next-page *default-client-context* api-result)))

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
  [params resource-pattern opt-defs result-processor-fn call-args]
  ;; (clojure.pprint/pprint (list 'api-fn* params resource-pattern opt-defs call-args))
  (let [[client-context param-values opts] (parse-api-fn-args (count params) call-args)
        param-map (->> (map vector params param-values) (into {}))
        api-result (call client-context
                         (util/expand-resource-pattern resource-pattern param-map)
                         (util/check-opts opt-defs opts))]
    (if result-processor-fn
      (result-processor-fn client-context api-result)
      api-result)))

(defmacro def-api-fn
  "def a function which will call an API method...
   the defined function calls api-fn* with the def'd metadata and the call args
   - name : the function name
   - param-or-params : the parameters to be substituted into the resource url
   - resource-pattern : the resource url patten. keys will be substituted with values from the function arglist
   - opt-defs : vector of option defs. each may be a key or a [key default-value|processor-fn] pair"
  [fname & macro-args]
  (let [[dname [param-or-params resource-pattern opt-defs & [result-processor-fn]]] (macro/name-with-attributes fname macro-args)]

    (let [param-keywords (->> [param-or-params] flatten (filter identity) (map keyword) vec)
          param-symbols (->> param-keywords (map name) (map symbol) vec)
          opt-keys (->> (util/opt-keys opt-defs) (map name) (map symbol))
          arglists `(quote ([~'client-context? ~@param-symbols & {:keys [~@opt-keys]}]))
          dname-arglists (with-meta dname (merge (meta dname) {:arglists arglists}))]
      `(def ~dname-arglists
         (fn [& args#]
           (api-fn* ~param-keywords ~resource-pattern ~opt-defs ~result-processor-fn args#))))))

(defn unwrap-page
  [page]
  (get-in page [:response :data]))

(defn pages
  "a lazy seq of pages : iterates with next-page
   - result-page : the first page of results"
  ([result-page]
     (->> result-page
          (iterate (fn [r] (next-page r)))
          (take-while identity)
          (map unwrap-page)))
  ([client-context result-page]
     (->> result-page
          (iterate (fn [r] (next-page client-context r)))
          (take-while identity)
          (map unwrap-page))))

(defn unwrap-response
  "remove the response wrapper from a result, unless there are traversals"
  [client-context result]
  (if result
    (if (:traversals result)
      result
      (:response result))))
