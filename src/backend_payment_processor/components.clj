(ns backend-payment-processor.components
  (:refer-clojure :exclude [test])
  (:require
   [backend-payment-processor.components.debug-logger :as debug-logger]
   [backend-payment-processor.components.dev-servlet :as dev-servlet]
   [backend-payment-processor.components.dummy-config :as config]
   [backend-payment-processor.components.http :as http]
   [backend-payment-processor.components.http-kit :as http-kit]
   [backend-payment-processor.components.mock-http :as mock-http]
   [backend-payment-processor.components.mock-servlet :as mock-servlet]
   [backend-payment-processor.components.routes :as routes]
   [backend-payment-processor.components.service :as service]
   [backend-payment-processor.components.storage :as storage]
   [backend-payment-processor.components.system-utils :as system-utils]
   [backend-payment-processor.service :as backend-payment-processor.service]
   [com.stuartsierra.component :as component]
   [schema.core :as s]))

(def base-config-map {:environment :prod
                      :host        "0.0.0.0"
                      :dev-port    8080})

(def local-config-map {:environment :dev
                       :host        "localhost"
                       :dev-port    8080})

;; all the components that will be available in the pedestal http request map
(def web-app-deps
  [:config :routes :http :storage])

(defn base []
  (component/system-map
   :config    (config/new-config base-config-map)
   :http-impl (component/using (http-kit/new-http-client) [:config])
   :http      (component/using (http/new-http) [:config :http-impl])
   :storage   (storage/new-in-memory)
   :routes    (routes/new-routes #'backend-payment-processor.service/routes)
   :service   (component/using (service/new-service) web-app-deps)
   :servlet   (component/using (dev-servlet/new-servlet) [:service])))

(defn e2e []
  (s/set-fn-validation! true)
  (merge (base)
         (component/system-map
          :config (config/new-config local-config-map))))

(defn test-system []
  (merge (base)
         (component/system-map
          :config       (config/new-config local-config-map)
          :servlet      (component/using (mock-servlet/new-servlet) [:service])
          :debug-logger (debug-logger/new-debug-logger)
          :http         (component/using (mock-http/new-mock-http) [:config])
          :service      (component/using (service/new-service) (conj web-app-deps :debug-logger)))))

(def systems-map
  {:e2e-system   e2e
   :local-system e2e
   :test-system  test-system
   :base-system  base})

(defn create-and-start-system!
  ([] (create-and-start-system! :base-system))
  ([env]
   (system-utils/bootstrap! systems-map env)))

(defn ensure-system-up! [env]
  (or (deref system-utils/system)
      (create-and-start-system! env)))

(defn stop-system! [] (system-utils/stop-components!))
