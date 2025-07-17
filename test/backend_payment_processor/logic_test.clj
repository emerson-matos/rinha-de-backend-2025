(ns backend-payment-processor.logic-test
  (:require
   [clojure.test :refer [deftest is]]
   [backend-payment-processor.logic :as subject]))

(deftest new-account-test
  (is (= true
         (subject/foo))))
