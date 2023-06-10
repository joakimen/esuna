(ns esuna.lib.aws.sqs
  (:require [com.grzm.awyeah.client.api :as aws]
            [clojure.walk :refer [keywordize-keys]]))

(def sqs (aws/client {:api :sqs}))

(defn invoke [m]
  (aws/invoke sqs m))

(defn- get-queue-attributes [queue-url]
  (-> (invoke {:op :GetQueueAttributes
               :request {:QueueUrl queue-url :AttributeNames ["All"]}})
      :Attributes
      keywordize-keys))

(defn- list-queues []
  (:QueueUrls (invoke {:op :ListQueues})))

(defn- list-dlq []
  (->> (list-queues)
       (filter #(re-matches #".*-dlq$" %))))

(defn- enrich-queue [queue-url]
  {:QueueUrl queue-url
   :Attributes (get-queue-attributes queue-url)})

(defn list-dead-letter-queues []
  (->> (list-dlq)
       (pmap enrich-queue)
       (doall)))

(comment

  (aws/validate-requests sqs true)
  (def queues (list-queues))
  (def dlq (list-dead-letter-queues))

  (-> (aws/ops sqs))
  (-> (aws/ops sqs) keys)
  (-> (aws/doc sqs :ListDeadLetterSourceQueues))
  (-> (aws/doc sqs :GetQueueAttributes))
;; 
  )
