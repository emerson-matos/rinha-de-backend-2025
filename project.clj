(defproject backend-payment-processor "0.0.1-SNAPSHOT"
  :description "Rinha de backend 2025 em clojure"
  :url "https://github.com/emerson-matos/rinha-de-backend-2025"
  :license {:name "Apache License, Version 2.0"}

  :plugins [[com.github.clojure-lsp/lein-clojure-lsp "2.0.9"]
            [lein-ancient "1.0.0-RC3"]]

  :dependencies [[org.clojure/clojure "1.12.1"]
                 [io.pedestal/pedestal.service "0.7.2"]
                 [io.pedestal/pedestal.jetty "0.7.2"]
                 [io.pedestal/pedestal.error "0.7.2"]

                 [com.github.seancorfield/next.jdbc "1.3.909"]
                 [org.postgresql/postgresql "42.7.2"]
                 [com.zaxxer/HikariCP "5.1.0"]
;; Time handling (for ISO timestamps)
[clojure.java-time "1.4.2"]

;; Configuration
[aero "1.1.6"] ; or [cprop "0.1.20"]

;; Async coordination (for fallback logic)
[org.clojure/core.async "1.6.681"]

                 [http-kit "2.8.0"]
                 [com.stuartsierra/component "1.1.0"]
                 [prismatic/schema "1.4.1"]
                 [cheshire "6.0.0"]
                 [io.aviso/pretty "1.4.4"]

                 [ch.qos.logback/logback-classic "1.5.18" :exclusions [org.slf4j/slf4j-api]]
                 [org.slf4j/jul-to-slf4j "2.0.17"]
                 [org.slf4j/jcl-over-slf4j "2.0.17"]
                 [org.slf4j/log4j-over-slf4j "2.0.17"]]

  :min-lein-version "2.4.2"

  :resource-paths ["config", "resources"]

  :aliases {"clean-ns" ["clojure-lsp" "clean-ns" "--dry"]          ;; check if namespaces are clean
            "format" ["clojure-lsp" "format" "--dry"]              ;; check if namespaces are formatted
            "diagnostics" ["clojure-lsp" "diagnostics"]            ;; check if project has any diagnostics (clj-kondo findings)
            "lint" ["do" ["clean-ns"] ["format"] ["diagnostics"]]  ;; check all above

            "clean-ns-fix" ["clojure-lsp" "clean-ns"]              ;; Fix namespaces not clean
            "format-fix" ["clojure-lsp" "format"]                  ;; Fix namespaces not formatted
            "lint-fix" ["do" ["clean-ns-fix"] ["format-fix"]]

            "dev" ["with-profile" "+dev" "repl"]
            "unit" ["with-profile" "+unit" "test"]
            "integration" ["with-profile" "+integration" "test"]}
  :profiles {:integration {:test-paths ^:replace ["test/integration"]}
             :unit {:test-paths ^:replace ["test/unit"]}
             :dev {:aliases {"run-dev" ["trampoline" "run" "-m" "backend-payment-processor.server/run-dev"]}
                   :repl-options {:init-ns user}
                   :source-paths ["dev"]
                   :dependencies [[nubank/state-flow "5.20.1"]
                                  [nubank/matcher-combinators "3.9.1"]
                                  [org.clojure/tools.namespace "1.5.0"]
                                  [org.clojure/java.classpath "1.1.0"]
                                  [criterium "0.4.6"]]}
             :uberjar {:aot :all
                       :main backend-payment-processor.server
                       :jvm-opts ["-Dclojure.compiler.direct-linking=true"]}}

  :test-paths ["test/unit" "test/integration"]

  :global-vars {*warn-on-reflection* true}
  :main ^{:skip-aot true} backend-payment-processor.server)
