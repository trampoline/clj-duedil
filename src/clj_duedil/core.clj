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
  [& body]
  `(url-only* (fn [] ~@body)))

(defprotocol ApiContext
  "duedil API methods"

  (call [this resource opts]))

(defrecord client [api-base api-key]

  ApiContext
  (call [this resource opts]
    (let [url (util/api-url api-base api-key resource opts)]
      (if-not *url-only*
        (-?> url
             http/get
             :body
             (json/read-json :key-fn keyword))
        url))))

(defmacro def-api-method
  [fname & macro-args]
  (let [[dname [param-or-params resource-pattern valid-opt-keys]] (macro/name-with-attributes fname macro-args)
        [dname-url [_ _ _]] (macro/name-with-attributes (symbol (<< "~{fname}-url")) macro-args)]

    (let [param-keywords (map keyword (->> [param-or-params] flatten (filter identity)))
          param-symbols (->> param-keywords (map name) (map symbol))
          param-map (->> (map vector param-keywords param-symbols)
                         (into {}))]
      `(defn ~dname
         [client# ~@param-symbols & {:as opts#}]
         (call client#
               (util/expand-resource-pattern ~resource-pattern ~param-map)
               (util/check-opts ~valid-opt-keys opts#))))))
