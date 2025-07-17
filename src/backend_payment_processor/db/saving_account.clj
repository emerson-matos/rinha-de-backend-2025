(ns backend-payment-processor.db.saving-account
  (:require
   [backend-payment-processor.protocols.storage-client :as storage-client]))

(defn add-account! [account storage]
  (storage-client/put! storage
                       #(assoc % (:id account) account)))

(defn remove-account! [account-id storage]
  (storage-client/put! storage #(dissoc % account-id)))

(defn accounts [storage]
  (storage-client/read-all storage))

(defn account [account-id storage]
  (get (storage-client/read-all storage) account-id))
