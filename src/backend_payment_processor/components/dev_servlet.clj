(ns backend-payment-processor.components.dev-servlet
  (:require
   [com.stuartsierra.component :as component]
   [io.pedestal.http :as bootstrap]))

(defrecord DevServlet [service]
  component/Lifecycle
  (start [this]
    (assoc this :instance (-> service
                              :runnable-service
                              ;; do not block thread that starts web server
                              (assoc ::bootstrap/join? false)
                              bootstrap/create-server
                              bootstrap/start)))
  (stop  [this]
    (bootstrap/stop (:instance this))
    (dissoc this :instance))

  Object
  (toString [_] "<DevServlet>"))

(defn new-servlet [] (map->DevServlet {}))

(defmethod print-method DevServlet [v ^java.io.Writer w]
  (.write w "<DevServlet>"))

(defn main [start-fn & _args]
  (start-fn {:mode :embedded})) ; lein run

(defn run-dev [start-fn & _args]
  ;; The entry-point for 'lein run-dev', 'lein with-profile +repl-start'
  (start-fn {:mode :embedded}))
