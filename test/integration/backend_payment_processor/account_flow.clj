(ns backend-payment-processor.account-flow
  (:require
   [backend-payment-processor.components :as components]
   [backend-payment-processor.components.mock-http :as mock-http]
   [backend-payment-processor.aux.http-helpers :refer [GET POST]]
   [backend-payment-processor.protocols.storage-client :as storage-client]
   [matcher-combinators.matchers :refer [equals]]))


#_(defflow create-account!
  (flow "Create account by hitting /accounts/ endpoint.

   Since this endpoint gets customer info via an http request to another
   service, we mock the response from that service"
        [:let [customer-url (str "http://customer-service.com/customer/" customer-id)
               url          "/account/"
               resp-body    (mock-http/with-responses
                              [customer-url {:body {:customer-name "bob"}}]
                              (-> url
                                  (POST {:customer-id customer-id} 200)
                                  :body))]]
        (match?
         {:created-account resp-body
          :account-id      (-> resp-body :account :id)})))

#_(defn lookup-missing-account
  "Assert that no account is found for the current customer ID by hitting the
   /account/from-customer/:customer-id endpoint"
  [{:keys [customer-id] :as world}]
  (let [url       (str "/account/from-customer/" customer-id)
        resp-body (:body (GET url 400))]
    (assoc world :account-lookup resp-body)))

#_(flow "create savings account, look it up, and close it"
      init!

  ;; create a customer ID
      (fn [world]
        (assoc world :customer-id (java.util.UUID/randomUUID)))

  ;; try to find an account that corresponds to that customer ID
      lookup-missing-account

      (fact "There shouldn't be a savings account for Bob yet"
            (:account-lookup *world*) => {})

      create-account!

      (fact "There should now be a savings account for Bob"
            (:created-account *world*) => (match {:account (equals {:customer-id uuid?
                                                                    :id          uuid?
                                                                    :name        "bob"})})))
