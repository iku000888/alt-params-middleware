(ns alt-params-middleware.core
  (:require [clojure.string :as str]
            [ring.util.codec :as codec :refer [assoc-conj]]
            [ring.util.request :as req])
  (:import java.net.URLDecoder
           org.apache.commons.codec.net.URLCodec))

;; for convenience. Perhaps it can land in ring.util.codec
;; but not really relavant to the point of this lib.
(defn commons-decoder [encoded encoding]
  (let [codec (URLCodec. encoding)
        decoded (.decode codec encoded)]
    decoded))

;; form-decode-str and form-decode Should be fixed in ring.util.codec
(defn form-decode-str
  "Decode the supplied www-form-urlencoded string using the specified encoding,
  or UTF-8 by default."
  [^String encoded & [encoding decoder]]
  (try
    (if decoder
      (decoder encoded encoding)
      (URLDecoder/decode encoded (or encoding "UTF-8")))
    (catch Exception _ nil)))

(defn form-decode
  "Decode the supplied www-form-urlencoded string using the specified encoding,
  or UTF-8 by default. If the encoded value is a string, a string is returned.
  If the encoded value is a map of parameters, a map is returned."
  [^String encoded & [encoding decoder]]
  (if-not (.contains encoded "=")
    (form-decode-str encoded encoding decoder)
    (reduce
     (fn [m param]
       (if-let [[k v] (str/split param #"=" 2)]
         (assoc-conj m (form-decode-str k encoding decoder) (form-decode-str v encoding decoder))
         m))
     {}
     (str/split encoded #"&"))))


;; Every fn below should be updated in ring.middleware.params
(defn- parse-params
  ([params encoding]
   (parse-params params encoding nil))
  ([params encoding decoder]
   (let [params (form-decode params encoding decoder)]
     (if (map? params) params {}))))

(defn assoc-query-params
  "Parse and assoc parameters from the query string with the request."
  {:added "1.3"}
  ([request encoding]
   (assoc-query-params request encoding nil))
  ([request encoding decoder]
   (merge-with merge request
               (if-let [query-string (:query-string request)]
                 (let [params (parse-params query-string encoding decoder)]
                   {:query-params params, :params params})
                 {:query-params {}, :params {}}))))

(defn assoc-form-params
  "Parse and assoc parameters from the request body with the request."
  {:added "1.2"}
  ([request encoding]
   (assoc-form-params request encoding nil))
  ([request encoding decoder]
   (merge-with merge request
               (if-let [body (and (req/urlencoded-form? request) (:body request))]
                 (let [params (parse-params (slurp body :encoding encoding) encoding)]
                   {:form-params params, :params params})
                 {:form-params {}, :params {}}))))

(defn params-request
  "Adds parameters from the query string and the request body to the request
  map. See: wrap-params."
  {:added "1.2"}
  ([request]
   (params-request request {}))
  ([request options]
   (let [encoding (or (:encoding options)
                      (req/character-encoding request)
                      "UTF-8")
         decoder (:decoder options)
         request  (if (:form-params request)
                    request
                    (assoc-form-params request encoding decoder))]
     (if (:query-params request)
       request
       (assoc-query-params request encoding decoder)))))

(defn wrap-params
  "Middleware to parse urlencoded parameters from the query string and form
  body (if the request is a url-encoded form). Adds the following keys to
  the request map:

  :query-params - a map of parameters from the query string
  :form-params  - a map of parameters from the body
  :params       - a merged map of all types of parameter

  Accepts the following options:

  :encoding - encoding to use for url-decoding. If not specified, uses
              the request character encoding, or \"UTF-8\" if no request
              character encoding is set.
  :decoder -  decoder fn to use for url-decoding that accepts the encoded
              string as the first argument and the encoding as the second.
              e.g. (fn [encoded encoding]
                      (do-stuff-to-get-decoded))"
  ([handler]
   (wrap-params handler {}))
  ([handler options]
   (fn
     ([request]
      (handler (params-request request options)))
     ([request respond raise]
      (handler (params-request request options) respond raise)))))
