(ns esuna.lib.aws.sqs
  (:require [com.grzm.awyeah.client.api :as aws]
            [clojure.walk :refer [keywordize-keys]]))

(defn create-client []
  (aws/client {:api :sqs}))

(defn- get-queue-attributes [client queue-url]
  (-> (aws/invoke client {:op :GetQueueAttributes
                          :request {:QueueUrl queue-url :AttributeNames ["All"]}})
      :Attributes
      keywordize-keys))

(defn- list-queues [client]
  (:QueueUrls (aws/invoke client {:op :ListQueues})))

(defn- list-dlq [client]
  (->> (list-queues client)
       (filter #(re-matches #".*-dlq$" %))))

(defn- enrich-queue [client queue-url]
  {:QueueUrl queue-url
   :Attributes (get-queue-attributes client queue-url)})

(defn list-dead-letter-queues []
  (let [sqs (create-client)]
    (->> (list-dlq sqs)
         (pmap #(enrich-queue sqs %))
         (doall))))

(comment

  (def sqs (create-client))
  (aws/validate-requests sqs true)
  (def queues (list-queues sqs))
  (def dlq (list-dead-letter-queues))

  (-> (aws/ops sqs))
  (-> (aws/ops sqs) keys)
  (-> (aws/doc sqs :ListDeadLetterSourceQueues))
  (-> (aws/doc sqs :GetQueueAttributes))
;; 
  )
