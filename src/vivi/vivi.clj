(ns vivi.http
  (:require [org.httpkit.client :as client]
            [environ-plus.core :refer [env]]))

(def config {:url "http://localhost:8086"})
