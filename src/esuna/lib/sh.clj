(ns esuna.lib.sh
  (:require [babashka.process :as p]
            [clojure.string :as str]))

(defn run [& args]
  (let [{:keys [out err exit]} (apply p/sh args)]
    (when-not (zero? exit)
      (throw (ex-info (str/trim err) {:babashka/exit exit})))
    (str/trim out)))

(defn run-with-result [& args]
  (let [{:keys [out err exit]} (apply p/sh args)
        result (if (zero? exit) out err)]
    {:status exit :result (str/trim result)}))
