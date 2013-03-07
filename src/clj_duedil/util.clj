(ns clj-duedil.util
  (:use
   clojure.core.strint)
  (:require
   [clojure.string :as str]
   [clojure.set :as set]))

(defn strip-last-slash
  [s]
  (str/replace-first s #"/$" ""))

(defn strip-first-slash
  [s]
  (str/replace-first s #"^/" ""))

(defn join-url-components
  "join a path to a base-url, accounting for any trailing / in base-url"
  [& components]
  (reduce (fn [base component]
            (str (strip-last-slash base) "/" (strip-first-slash component)))
          components))

(defn underscore
  "replace - with _ to convert clojure keywords to http param names"
  [s]
  (str/replace s #"-" "_"))

(defn flatten-params
  "convert a map into a params-string"
  [m]
  (->> m
       (map (fn [[k v]] (<< "~(-> k name underscore)=~(str v)")))
       (str/join "&")
       (str "?")
       (#(if (> (count %) 1) % ""))))

(defn expand-resource-pattern
  "expand a resource pattern with parameter values
   - pattern: a string of the form \"/foo/:key/bar\"
   - params: a map of the form {:key 100}
   the examples would result in : \"/foo/100/bar\""
  [pattern params]
  (let [param-keys (keys params)]
    (reduce (fn [resource param-key]
              (let [param-key-patt (re-pattern (str param-key))]
                (if (= 1 (count (re-seq param-key-patt resource)))
                  (str/replace-first resource
                                     param-key-patt
                                     (str (params param-key)))
                  (throw (RuntimeException. (<< "pattern: ~{pattern} does not contain key: ~{param-key}"))))))
            pattern
            param-keys)))

(defn check-opts
  "check options and apply default values
   - valid-opt-keys: a seq of option keys. each key may be just they key, or a pair of [key default-value]
   - opts: the options map to check. keys must be present in valid-opt-keys. default values, where they exist, will be added for missing keys"

  [valid-opt-keys opts]
  (let [opt-defaults (reduce (fn [od opt-default]
                               (let [[opt default] (flatten [opt-default])]
                                 (assoc od opt default)))
                             {}
                             valid-opt-keys)
        defaults (->> opt-defaults
                      (filter (fn [[k v]] v))
                      (into {}))
        valid-keys (keys opt-defaults)
        invalid-keys (vec (set/difference (-> opts keys set) (-> opt-defaults keys set)))]

    (if (not-empty invalid-keys)
      (throw (RuntimeException. (<< "unknown option keys: ~{invalid-keys}"))))

    (merge defaults opts)))
