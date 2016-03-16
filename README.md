# Vivi

A Clojure client for InfluxDB.

## Installation

Vivi is distributed via Clojars. Add the following to your dependencies in project.clj:

`:dependencies [[defclass/vivi "0.1.0-SNAPSHOT"]]`

## Usage

### Require in your app
`(require '[vivi.core :as v])`

### Configure

` (v/set-config {:url "http://127.0.0.1:8086"}) `

### Write data to influxdb

Write a record: 

```clojure
 (v/write "mydb"
    {:measurement "cpu"
     :fields {:tag-1 "tag-1" :tag-2 "tag-2"}
     :values {:value1 1 :value2 2}})
 ;;=> {:body "",
       :status 204}
```

Write records:

```clojure
 (v/write "mydb" [{:measurement "cpu"
                   :fields {:tag-1 "tag-1" :tag-2 "tag-2"}
                   :values {:value1 1 :value2 2}}
                   {:measurement "cpu"
                    :fields {:tag-1 "tag-1" :tag-2 "tag-2"}
                    :values {:value1 3 :value2 4}}])
 ;;=> {:body "",
       :status 204}
```


### Query
```clojure
    (v/query "mydb" "select * from cpu"})
    ;; => {:series [{:name "cpu",
    ;;               :columns ["time" "tag-1" "tag-2" "value1" "value2"],
    ;;               :values [["2016-03-14T15:19:48.904649855Z" "tag-1" "tag-2" 1 2]
    ;;                        ["2016-03-14T15:20:12.932424503Z" "tag-1" "tag-2" 1 2]]}]}
```

## License

Copyright © 2016 Michael Wong

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
