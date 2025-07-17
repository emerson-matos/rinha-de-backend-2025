(ns backend-payment-processor.logic-test
  (:require
   [clojure.test :refer [deftest is]]
   [matcher-combinators.test :refer [match?]]
   [backend-payment-processor.logic :as subject]))

(deftest new-account-test
  (is (match? {:customer-id {},
               :id uuid?,
               :name {}}
         (subject/new-account {} {}))))
