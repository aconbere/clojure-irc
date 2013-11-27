(ns irc.core
  (:require [instaparse.core :as insta])
  (:use [clojure.string :only (join)]))

(def grammar-file (clojure.java.io/resource "grammar.txt"))
(def parser (insta/parser (slurp grammar-file)))

(defn parse [x]
  (loop [left (parser x)
         msg { :params nil :command nil :prefix nil }]
    (if (empty? left)
      msg
      (let [section (first left)
            kind (first section)
            value (if (or (= kind :command) (= kind :prefix)) (first (rest section)) (rest section))]
        (recur (rest left) (assoc msg kind value))))))

(defn params->string
  "The big idea here is the last element of the params list should be prefixed
  with a : to allow spaces in subsequent string"
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

(defn params-map
  [command]
  (case command
    "PASS" [:password]
    "USER" [:username :hostname :servername :realname]))

(defn make-command
  [parsed-message]
  (merge parsed-message (zipmap (params-map (:command parsed-message) (:params parsed-message)))))
