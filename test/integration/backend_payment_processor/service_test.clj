(ns backend-payment-processor.service-test
  (:require
   [backend-payment-processor.aux.http-client-helpers :as aux.http-client-helpers]
   [backend-payment-processor.aux.init :refer [defflow]]
   [backend-payment-processor.aux.servlet-helpers :refer [GET POST]]
   [matcher-combinators.matchers :as m]
   [state-flow.api :refer [flow match?]]
   [clojure.pprint :as pprint]))

(defflow home-page-endpoint-flow
  (flow "hitting home page endpoint"
    (match?
     {:status 200
      :body {:message "Hello World!"}}
     (GET "/"))))

(defn create-account!
  "Create account by hitting /accounts/ endpoint.

   Since this endpoint gets customer info via an http request to another
   service, we mock the response from that service"
  [customer-id]
  (flow "create account flow"
    [:let [customer-url (str "http://customer-service.com/customer/" customer-id)]]
    (aux.http-client-helpers/add-responses {customer-url {:body {:customer-name "bob"}}})
    (POST "/account/" {:customer-id customer-id})))

(defn lookup-missing-account
  "Assert that no account is found for the current customer ID by hitting the
   /account/from-customer/:customer-id endpoint"
  [customer-id]
  (flow "request account"
    [:let [url (str "/account/from-customer/" customer-id)]]
    (GET url)))

(defflow account-flow
  (flow "create savings account, look it up, and close it"
    ;; try to find an account that corresponds to that customer ID
    [:let [customer-id (random-uuid)]]
    (flow "There shouldn't be a savings account for Bob yet"
      (match? {:status 400}
              (lookup-missing-account customer-id)))

    (flow "There should now be a savings account for Bob"
      (match? {:status 200
               :body  {:account (m/equals {:customer-id customer-id
                                           :id          uuid?
                                           :name        "bob"})}}
              (create-account! customer-id)))))

#_(defflow pessoa-endpoint-flow
    (flow "pessoa endpoint"
      [:let [raul-modelo {:nascimento "2021-01-01" :nome "raul" :apelido "corno"}]
       raul-criado (POST "/pessoas" raul-modelo)
       :let [raul-location (-> raul-criado :headers (get "Location"))
             raul-body (-> raul-criado :body)]]
      (flow "hitting list pessoas"
        (match? {:status 200
                 :body [raul-body]}
                (GET "/pessoas")))
      (flow "create a new person with success"
        (match?
         {:status 201
          :headers {"Location" (str "/pessoas/" (:id raul-body))}
          :body {:id uuid? :nome "raul" :apelido "corno" :nascimento "2021-01-01"}}
         raul-criado))
      (flow "should return 422 when invalid request"
        (match? {:status 422}
                (POST "/pessoas" raul-modelo)))
      (flow "should return 422 when invalid request"
        (match? {:status 400}
                (POST "/pessoas" {:nascimento "200-01-01"})))

      [_ (POST "/pessoas" {:nascimento "2021-01-01" :nome "raul" :apelido "corno"})]
      [:let [_ (pprint/pprint (-> raul-criado))]]
      (flow "hitting get by id"
        (match? {:status 200
                 :body [raul-body]}
                (GET raul-location)))
      #_(flow "hitting get by id"
          (match? {:status 200
                   :body  {:quantity 1}}
                  (GET (str "/contagem-pessoas"))))
      #_(POST "/pessoas" {:nascimento "2000-10-01" :nome "José Roberto" :apelido "josé" :stack ["C#" "Node" "Oracle"]})
      #_(POST "/pessoas" {:nascimento "1985-09-23" :nome "Ana Barbosa" :apelido "ana" :stack nil})
      #_(POST "/pessoas" {:nascimento "2021-01-12" :nome "ulra" :apelido "rnoco"})
      #_(flow "create a new person with success"
          (match?
           {:status 201
            :body {:id uuid? :nome "raul" :apelido "corno" :nascimento "2021-01-01"}}
           raul-criado))
      #_(flow "hitting get all filtering by termo"
          (match? {:status 200
                   :body  {:quantity 4}}
                  (GET (str "/pessoas?t=\"\""))))
      #_(flow "hitting get by id"
          (match? {:status 200
                   :body  {:quantity 4}}
                  (GET (str "/contagem-pessoas"))))))
