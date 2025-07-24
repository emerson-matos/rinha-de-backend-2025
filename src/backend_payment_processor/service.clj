(ns backend-payment-processor.service
  (:require
   [backend-payment-processor.adapters :as adapters]
   [backend-payment-processor.controller :as controller]
   [backend-payment-processor.interceptors.error-info :as error-info]
   [cheshire.core :as json]
   [clojure.core.async :refer [tap]]
   [clojure.string :as str]
   [io.pedestal.http :as http]
   [io.pedestal.http.body-params :as body-params]
   [io.pedestal.log :as log]
   [ring.util.response :as ring-resp]))

(defn home-page
  [request]
  (ring-resp/response {:message "Hello World!"}))

(defn create-account
  [{{:keys [customer-id]} :edn-params
    {:keys [http storage]} :components}]
  (let [account (controller/create-account! customer-id storage http)]
    (ring-resp/response {:account account})))

(defn customer->account
  [{{:keys [customer-id]} :path-params
    {:keys [storage]} :components}]
  (let [account (controller/customer->account (adapters/str->uuid customer-id) storage)]
    (if account
      (ring-resp/response {:account account})
      (ring-resp/status
       (ring-resp/response {})
       400))))

(defn get-account
  [{{:keys [account-id]} :path-params
    {:keys [storage]} :components}]
  (ring-resp/response
   (controller/get-account account-id storage)))

(defn delete-account
  [{{:keys [account-id]} :path-params
    {:keys [storage]} :components}]
  (ring-resp/response
   (controller/delete-account! (adapters/str->uuid account-id) storage)))

(defn fetch-health-check
  "Makes HTTP call to payment processor health endpoint"
  [processor-type base-urls]
  (let [response {} url (str (get base-urls processor-type) "/payments/service-health")]
    (try
      ;; (let [response @(http/get url {:timeout 3000})]) ; 3 second timeout
      (if (= 200 (:status response))
        (json/parse-string (:body response) true)
        (do
          (log/warn :desc (str "Health check failed for" processor-type "with status") :status (:status response))
          {:failing true :minResponseTime 5000})) ; Assume failing if non-200
      (catch Exception e
        (log/error :error e :desc "Health check error for" :type processor-type)
        {:failing true :minResponseTime 5000})))) ; Assume failing on exception

#_(defn get-cached-health-check
     "Gets health check result from cache or fetches fresh data"
     [processor-type redis-conn base-urls]
     (let [cache-key (str "health:" processor-type)
           cached (redis/get redis-conn cache-key)]
       (if cached
         (json/parse-string cached true)
         (let [health-data (fetch-health-check processor-type base-urls)
               ttl 5]                  ; 5 second cache as required by challenge
           (redis/setex redis-conn cache-key ttl (json/generate-string health-data))
           health-data))))

(defn choose-payment-processor
  "Decides which processor to use based on health checks"
  [redis-conn base-urls]
  (let [default-health {} #_(get-cached-health-check :default redis-conn base-urls)
        fallback-health {} #_(get-cached-health-check :fallback redis-conn base-urls)]
    (cond
      ;; If default is healthy, always use it (lower fees)
      (not (:failing default-health)) :default

      ;; If default is failing but fallback is healthy, use fallback
      (and (:failing default-health)
           (not (:failing fallback-health))) :fallback

      ;; Both failing - try default first (lower fees), fallback as backup
      :else :default)))

;; Cache health check for 5 seconds
(defn get-cached-health-check [processor-type redis-conn]
  (let [cache-key (str "health:" processor-type)
        cached {} #_(redis/get redis-conn cache-key)]
    (if cached
      (json/parse-string cached true)
      (let [health-data (fetch-health-check processor-type {})
            ttl 5] ; 5 second cache
        #_(redis/setex redis-conn cache-key ttl (json/generate-string health-data))
        health-data))))

(defn health
  [_]
  (ring-resp/response {:ok (get-cached-health-check :payment {})}))

;; (def atoms (atom []))
(defn payments
  [{body :json-params
    {:keys [http storage]} :components}]
  ;; (swap! atoms conj {:s body})
  ;; do something async!!!
  ;; maybe clojure async?
  ;; de fato processar?
  (ring-resp/status (ring-resp/response body) 202))

(defn payments-summary
  [{query :query-params
    {:keys [http storage]} :components}]
  ;; (swap! atoms conj {:s query})
  ;;TODO de fato pegar o sumary hehe
  (ring-resp/status (ring-resp/response query) 202))

(defn camel->kebab [s]
  ;; (swap! atoms conj {:s s})
  (-> s
      (str/replace #"" "$1-$2")
      str/lower-case))

(def common-interceptors
  [(body-params/body-params (body-params/default-parser-map :json-options {:bigdec true :key-fn (comp keyword camel->kebab)}))
   http/html-body
   error-info/log-error-during-debugging])

(def routes
  #{["/" :get (conj common-interceptors `home-page)]
    ["/payments" :post (conj common-interceptors `payments)]
    ["/payments-summary" :get (conj common-interceptors `payments-summary)]
    ["/ops/health" :get (conj common-interceptors `health)]
    ["/account/" :post (conj common-interceptors `create-account)]
    ["/account/from-customer/:customer-id/" :get (conj common-interceptors `customer->account)]
    ["/account/lookup/:account-id/" :get (conj common-interceptors `get-account)]
    ["/account/remove/:account-id/" :post (conj common-interceptors `delete-account)]})
