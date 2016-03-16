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

(defn- data->plain [{:keys [measurement tags fields time]}]
  (assert (and measurement tags fields) "measurement, tags, fields keys must in data.")
  (->> (filter identity [measurement "," (prepare-data tags) " " (prepare-data fields) " " time])
       (filter identity)
       (apply str)))

(defn write [db data]
  (assert (or (map? data) (vector? data)) "Write data need be map or vector.")
  (let [body (if (vector? data)
               (str/join " \n " (map data->plain data))
               (data->plain data))]
    (-> @(client/post (write-url db) {:body body})
        (select-keys [:status :body]))))

(defn query [data]
  (assert (and (map? data)
               (.contains (keys data) :q)) "Data is a map and must have key :q .")
  (let [url (query-url)]
    (-> @(client/get url {:query-params data})
        :body (parse-string true))))

(defn create-db [db]
  (query {:q (format "create database %s" (name db))}))

(defn delete-db [db]
  (query {:q (format "drop database %s" (name db))}))
