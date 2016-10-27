(ns vivi.schemas
  (:require [schema.core :as s]))

(s/defschema InfluxdbSpec {:host s/Str
                           :db s/Str})

(s/defschema Recorder {:measurement s/Str
                       :fields {(s/either s/Keyword s/Str) (s/either s/Num s/Str s/Bool)}
                       (s/optional-key :tags) {(s/either s/Keyword s/Str) s/Str}
                       (s/optional-key :time) s/Num})

(s/defschema Tag {(s/either s/Keyword s/Str) s/Str})
(s/defschema Fields {(s/either s/Keyword s/Str) (s/either s/Num s/Str s/Bool)})

