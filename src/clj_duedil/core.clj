(ns clj-duedil.core
  (:require
   [clojure.string :as str]
   [clojure.tools.macro :as macro]
   [clj-duedil.util :as util])
  (:use
   clojure.core.strint))


(def duedil-v2-sandbox "http://api.duedil.com/sandbox/v2")
(def duedil-v2 "http://api.duedil.com/v2")

(defprotocol DuedilApi
  "duedil API methods"

  (call [this resource params]))

(defrecord client [api-base api-key ]

  DuedilApi
  (call [this resource opts]
    (let [url (str (apply util/join-url-components api-base (flatten [resource]))
                   (util/flatten-params (merge opts {:api-key api-key})))]
      url)))

(defmacro def-api-method
  [fname & macro-args]
  (let [[dname [param-or-params resource-pattern valid-opt-keys]] (macro/name-with-attributes fname macro-args)]

    (let [params (map keyword (->> [param-or-params] flatten (filter identity)))
          param-names (->> params (map name) (map symbol))
          param-map (->> (map vector params param-names)
                         flatten
                         (apply hash-map))]
      `(defn ~dname
         [client# ~@param-names opts#]
         (call client#
               (util/expand-resource-pattern ~resource-pattern ~param-map)
               (util/check-opts ~valid-opt-keys opts#))))))


(def-api-method get-company :company-id "company/:company-id.json" [[:fields "get_all"] :traversal])
