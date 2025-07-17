(ns backend-payment-processor.aux.init
  (:require
   [backend-payment-processor.components :as components]
   [schema.core :as s]
   [state-flow.api :as state-flow]))

(defn run-with-fn-validation
  [flow state]
  (s/with-fn-validation (state-flow/run flow state)))

(defn init! []
  (s/with-fn-validation
    {:system (components/create-and-start-system! :test-system)}))

(defmacro defflow [name & flows]
  `(state-flow/defflow ~name {:init  init!
                              :runner run-with-fn-validation}
     ~@flows))
