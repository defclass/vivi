# Vivi

A Clojure client for InfluxDB.

## Installation

Vivi is distributed via Clojars. Add the following to your dependencies in project.clj:

[![Clojars Project](https://img.shields.io/clojars/v/defclass/vivi.svg)](https://clojars.org/defclass/vivi)

## CHANGELOG

[click me](https://github.com/defclass/vivi/blob/master/CHANGELOG.md)

## Usage

### Require in your app
`(require '[vivi.core :as v])`

### Influxdb Spec

```clojure
 (def db-spec {:host "http://127.0.0.1:8086"
               :db "database-name"})
 ```



### Write data to influxdb

Write a record: 

```clojure
 (v/write db-spec
    {:measurement "cpu"
     :tags {:tag-1 "tag-1" :tag-2 "tag-2"}
     :fields {:value1 1 :value2 2}})
;=>
;{:opts {:body "cpu,tag-1=tag-1,tag-2=tag-2 value1=1,value2=2 ",
;        :method :post,
;        :url "http://127.0.0.1:8086/write?db=database-name"},
; :body nil,
; :headers {:content-encoding "gzip",
;           :date "Thu, 27 Oct 2016 02:00:39 GMT",
;           :request-id "28db69b2-9be9-11e6-8007-000000000000",
;           :x-influxdb-version "0.12.2"},
; :status 204}

```

Write records:

```clojure
 (v/write db-spec [{:measurement "cpu"
                   :tags {:tag-1 "tag-1" :tag-2 "tag-2"}
                   :fields {:value1 1 :value2 2}}
                   {:measurement "cpu"
                    :tags {:tag-1 "tag-1" :tag-2 "tag-2"}
                    :fields {:value1 3 :value2 4}}])
;=>
;{:opts {:body "cpu,tag-1=tag-1,tag-2=tag-2 value1=1,value2=2  \ncpu,tag-1=tag-1,tag-2=tag-2 value1=3,value2=4 ",
;        :method :post,
;        :url "http://127.0.0.1:8086/write?db=database-name"},
; :body nil,
; :headers {:content-encoding "gzip",
;           :date "Thu, 27 Oct 2016 02:02:33 GMT",
;           :request-id "6c9ba85f-9be9-11e6-8008-000000000000",
;           :x-influxdb-version "0.12.2"},
; :status 204}
```


### Query
```clojure
(v/query db-spec "select * from cpu")
;=>   
;{:opts {:query-params {:db "database-name", :q "select * from cpu"}, :method :get, :url "http://127.0.0.1:8086/query"},
; :body {:results [{:series [{:name "cpu",
;                             :columns ["time" "tag-1" "tag-2" "value1" "value2"],
;                             :values [["2016-10-27T02:00:39.65425519Z" "tag-1" "tag-2" 1 2]
;                                      ["2016-10-27T02:02:33.320677577Z" "tag-1" "tag-2" 3 4]]}]}]},
; :headers {:connection "close",
;           :content-encoding "gzip",
;           :content-length "153",
;           :content-type "application/json",
;           :date "Thu, 27 Oct 2016 02:03:28 GMT",
;           :request-id "8d383de2-9be9-11e6-8009-000000000000",
;           :x-influxdb-version "0.12.2"},
; :status 200}
```

## License

Copyright © 2016 Michael Wong

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
