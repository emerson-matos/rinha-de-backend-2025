(ns backend-payment-processor.aux.servlet-helpers
  (:require
   [backend-payment-processor.components.debug-logger :as debug-logger]
   [backend-payment-processor.http.serialization :as serialization]
   [io.pedestal.http :as bootstrap]
   [io.pedestal.test :refer [response-for]]
   [state-flow.api :as flow]))

(def default-headers
  {"Content-Type"    "application/edn ;charset=utf-8,*/*"
   "Accept-Encoding" "gzip, deflate"})

(defn output-stream->data [output]
  (if (string? output)
    (serialization/read-edn output)
    output))

(defmacro with-debug-error-logging
  "Prepare debug logger to store, in a thread local manner, the last error stored"
  [& forms]
  `(binding [debug-logger/*latest-error* nil] (do ~@forms)))

(defn req!
  ([method uri]
   (req! method uri nil))
  ([method uri body]
   (flow/flow "servlet request"
     [servlet (flow/get-state (comp :servlet :system))]
     (with-debug-error-logging
     ;; Raw pedestal response, without content negotiation or serialization support
     (-> servlet
         :instance
         ::bootstrap/service-fn
         (response-for method uri
                       :body (when body
                               (serialization/write-edn body))
                       :headers default-headers)
         (update :body #(try (output-stream->data %)
                             (catch Exception _ %)))
         flow/return)))))

(def GET  (partial req! :get))
(def POST (partial req! :post))
