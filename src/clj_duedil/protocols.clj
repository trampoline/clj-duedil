(ns clj-duedil.protocols
  "a separate namespace for protocols to avoid recompilation causing failed instance? calls during development")

(defprotocol ClientContext
  "duedil API methods"
  (call [this method opts]
    "call an API method with options")
  (call-next-page [this api-result]
    "given a result of call or call-next-page, fetch the next page of results, or nil"))
