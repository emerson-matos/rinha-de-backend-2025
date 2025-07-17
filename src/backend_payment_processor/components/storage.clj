(ns backend-payment-processor.components.storage
  (:require
   [backend-payment-processor.protocols.storage-client :as storage-client]
   [com.stuartsierra.component :as component]))

(defrecord InMemoryStorage [storage]
  component/Lifecycle
  (start [this] this)
  (stop  [this]
    (reset! storage {})
    this)

  storage-client/StorageClient
  (read-all [_this] @storage)
  (put! [_this update-fn] (swap! storage update-fn))
  (clear-all! [_this] (reset! storage {})))

(defn new-in-memory []
  (->InMemoryStorage (atom {})))
