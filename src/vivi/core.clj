(ns vivi.core
  (:require [org.httpkit.client :as client]
            [environ-plus.core :refer [env]]
            [clojure.string :as str]
            [clojure.walk :as walk]
            [cheshire.core :refer [parse-string]]))

(defonce +default-config+
  {:influxdb {:url "http://127.0.0.1:8086"
              :db "mydb"}})

(alter-var-root #'env merge +default-config+)

(defn set-config [m]
  (alter-var-root #'env merge m))

(defn get-inf-cfg []
  (:influxdb env))

(defn write-url []
  (str (:url (get-inf-cfg))  "/write?db=" (:db (get-inf-cfg))))

(defn query-url []
  (str (:url (get-inf-cfg)) "/query"))

(defn- prepare-data [m]
  (->> (walk/stringify-keys m)
       (map #(str/join "=" %))
       (str/join ",")))

(defn- data->plain [measurement fields values & [time]]
  (->> [measurement "," (prepare-data fields) " " (prepare-data values) " " time]
       (filter identity)
       (apply str)))

(defn write [measurement fields values & [time]]
  (-> @(client/post (write-url) {:body (data->plain measurement fields values time)})
      :status))

(defn query [sql]
  (let [url (query-url)]
    (-> @(client/get url
                     {:query-params {"db" (:db (get-inf-cfg)) "q" sql}})
        :body (parse-string true)
        :results first)))
