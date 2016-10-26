(ns vivi.core
  (:require [org.httpkit.client :as client]
            [clojure.string :as str]
            [cheshire.core :refer [parse-string]]
            [vivi.schemas :refer :all]
            [schema.core :as s]))


(defn info-e! [msg & [m]]
  (let [m (if (nil? m) {} m)]
    (throw (ex-info msg m))))

(defn- check-string! [s]
  (if (not (string? s))
    (info-e! "s is not string.")
    (if (re-matches #"[0-9a-zA-Z_\- ()*,;.]+" s)
      s
      (info-e! (format "Invalid characters, only allow [0-9a-zA-Z_- ()*,;.] characters, but input: %s" s)))))

; db-spec format =>
; {:host "http://127.0.0.1:8086"
;  :db "db_name"}


;;; BOF write API.
; Write API
; key: Don't need quote.
; field value:
;   String need quote by ` " `.
;   Bool don't need quote.
;   Number don't need quote.
;
; tag value:
;   String don't need quote.



(s/defn ^:always-validate write-url :- s/Str [db-spec :- InfluxdbSpec]
        (format "%s/write?db=%s" (:host db-spec)   (:db db-spec)))

(defn- coerce-tag-value [v]
  (cond
    (keyword? v) (name v)
    (string? v) (do (check-string! v) v)
    (number? v) (str v)
    :else (info-e! "tag value should be keyword, number or string."
                   {:value v})))

(defn- coerce-field-value [v]
  (cond
    (string? v)
    (do (check-string! v)
        (format "\"%s\"" v))

    (= (class v) java.lang.Boolean) (str v)

    (number? v) v

    (keyword? v) (name v)

    :else
    (info-e! (format "Value should be number, bool or string. Have passed : %s ." (str (class v))))))

(defn- coerce-key [k]
  (assert (or (keyword? k) (string? k)) "key should be keyword or string.")
  (if (keyword? k)
    (name k)
    k))

(defn- prepare-data [m func]
  (let [join-fn (fn [[k v]]
                  (let [k (coerce-key k)
                        v (func v)]
                    (format "%s=%s" k v)))]
    (->> (map join-fn m)
         (str/join ","))))

(defn- prepare-tag [m]
  (prepare-data m coerce-tag-value))

(defn- prepare-field [m]
  (prepare-data m coerce-field-value))

(s/defn ^:always-validate data->plain :- s/Str
  [m :- Recorder]
  (let [{:keys [measurement tags fields time]} m]
    (->> [measurement "," (prepare-tag tags) " " (prepare-field fields) " " time]
         (filter identity)
         (apply str))))

(defn write [db-spec data]
  (assert (or (map? data) (vector? data)) "Write data need be map or vector.")
  (let [body (if (vector? data)
               (str/join " \n " (map data->plain data))
               (data->plain data))]
    (let [result (-> @(client/post (write-url db-spec) {:body body})
                (update-in [:body] #(parse-string % true)))]
      (if (<= 200 (:status result) 299)
        result
        (info-e! "Influxdb write error: " result)))))


;;; EOF write API.


;;; BOF query API.

; Query API
; key: Quoted by ` " `
; field value:
;   String quoted by ` ' `.
;   Bool don't need quote.
;   Number don't need quote.
;
; tag value:
;   String quoted by ` ' `.

(s/defn ^:always-validate query-url :- s/Str [db-spec :- InfluxdbSpec]
  (format "%s/query" (:host db-spec)))


(defn query [db-spec q]
  (let [url (query-url db-spec)
        result (-> @(client/get url {:query-params {:db (:db db-spec) :q q}})
                   (update-in [:body] #(parse-string % true)))]
    (if (<= 200 (:status result) 299)
      result
      (info-e! "Influxdb query error: " result))))

(defn create-db [db-spec db-name]
  (query db-spec (format "create database %s" (name db-name))))

(defn delete-db [db-spec db-name]
  (query db-spec (format "drop database %s" (name db-name))))

;;; EOF query API.