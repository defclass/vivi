(ns vivi.core
  (:require [org.httpkit.client :as client]
            [environ-plus.core :refer [env]]
            [clojure.string :as str]
            [clojure.walk :as walk]
            [cheshire.core :refer [parse-string]]))

(defonce +default-config+
  {:url "http://127.0.0.1:8086"
   :db "mydb"})

(alter-var-root #'env merge +default-config+)

(defn set-config [m]
  (alter-var-root #'env merge m))

(defn write-url []
  (str (:url env)  "/write?db=" (:db env)))

(defn query-url []
  (str (:url env) "/query"))

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
                     {:query-params {"db" (:db env) "q" sql}})
        :body (parse-string true)
        :results first)))
