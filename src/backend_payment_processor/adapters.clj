(ns backend-payment-processor.adapters)

(defn str->uuid [id-str]
  (read-string (str "#uuid \"" id-str "\"")))

