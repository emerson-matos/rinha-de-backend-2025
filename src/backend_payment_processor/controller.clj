(ns backend-payment-processor.controller
  (:require
   [backend-payment-processor.db.saving-account :as db.saving-account]
   [backend-payment-processor.logic :as logic]
   [backend-payment-processor.protocols.http-client :as http-client]))

(defn customer->account [customer-id storage]
  (->> storage
       db.saving-account/accounts
       vals
       (filter #(= customer-id (:customer-id %)))
       first))

(defn delete-account! [account-id storage]
  (db.saving-account/remove-account! account-id storage))

(defn get-customer [customer-id http]
  (let [customer-url  (str "http://customer-service.com/customer/" customer-id)]
    (:body (http-client/req! http {:url    customer-url
                                   :method :get}))))

(defn create-account! [customer-id storage http]
  (let [customer (get-customer customer-id http)
        account  (logic/new-account customer-id (:customer-name customer))]
    (db.saving-account/add-account! account storage)
    account))

(defn get-account [account-id storage]
  (db.saving-account/account account-id storage))

