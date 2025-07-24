(ns user
  (:require
   [backend-payment-processor.components :as components]
   [backend-payment-processor.components.system-utils :as system-utils]
   [clojure.repl :refer :all]))

(defn system []
  @system-utils/system)

(def init
  "Constructs the current development system."
  components/create-and-start-system!)

(def start
  "Starts the current development system."
  (partial components/ensure-system-up! :base-system))

(def stop
  "Shuts down and destroys the current development system."
  system-utils/stop-system!)

(defn go
  "Initializes the current development system and starts it running."
  []
  (init)
  (start))

(def db-uri "datomic:dev://0.0.0.0:4334/hello")

(comment
  (go)
  (stop)
  (require '[backend-payment-processor.service :as ser])
  ser/atom
  (d/create-database db-uri)
  (def conn (d/connect db-uri))
  @(d/transact conn [{:db/doc "Hello world"}]))
