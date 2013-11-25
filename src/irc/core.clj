(ns irc.core
  (:require [instaparse.core :as insta])
  (:use [clojure.string :only (join)]))

(def grammar-file (clojure.java.io/resource "grammar.txt"))
(def parser (insta/parser (slurp grammar-file)))
(defn raw-parse [x] (parser x))

(defn parse [x]
  (loop [left (parser x)
         msg { :params () }]
    (if raw-parse
      (let [[kind value] (first raw-parse)]
        (case kind
          :command (assoc msg kind value)
          :prefix (assoc msg kind value)
          :params (update-in msg [kind] cons value)))
      msg) ))

(defn message
  ([command params] { :command command :params params })
  ([prefix command params] { :prefix prefix :command command :params params }))

(defn params->string
  [params]
  (let [params (reverse params)
        top (peek params)
        params (pop params)]
    (join " " (reverse (conj params (str ":" top))))))

(defn message->string
  [{ command :command params :params prefix :prefix }]
  (if prefix
    (str ":" (join " " [prefix command (params->string params)]))
    (str (join " " [command (params->string params)]))))
