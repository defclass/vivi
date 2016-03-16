(ns vivi.core
  (:require [org.httpkit.client :as client]
            [environ-plus.core :refer [env]]
            [clojure.string :as str]
            [clojure.walk :as walk]
            [cheshire.core :refer [parse-string]]))

(defonce +default-config+ {:url "http://127.0.0.1:8086"})

(alter-var-root #'env merge {:influxdb +default-config+})

(defn set-config [m]
  (alter-var-root #'env merge {:influxdb m}))

(defn get-inf-cfg []
  (:influxdb env))

(defn write-url [db]
  (str (:url (get-inf-cfg))  "/write?db=" db))

(defn query-url []
  (str (:url (get-inf-cfg)) "/query"))

(defn- prepare-data [m]
  (->> (walk/stringify-keys m)
       (map #(str/join "=" %))
       (str/join ",")))

(defn- data->plain [{:keys [measurement fields values time]}]
  (->> (filter identity [measurement "," (prepare-data fields) " " (prepare-data values) " " time])
       (filter identity)
       (apply str)))

(defn write [db data]
  (assert (or (map? data) (vector? data)) "Write data need be map or vector.")
  (let [body (if (vector? data)
               (str/join " \n " (map data->plain data))
               (data->plain data))]
    (-> @(client/post (write-url db) {:body body})
        (select-keys [:body :status]))))

(defn query [db q]
  (let [url (query-url)]
    (-> @(client/get url
                     {:query-params {"db" db "q" q}})
        :body (parse-string true)
        :results)))
