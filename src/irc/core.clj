(ns irc.core
  (:require [instaparse.core :as insta])
  (:use [clojure.string :only (join)]))

(def grammar-file (clojure.java.io/resource "grammar.txt"))
(def parser (insta/parser (slurp grammar-file)))

(def command-info {
  "PASS"    [:password]
  "NICK"    [:nick]
  "USER"    [:username :hostname :servername :realname]
  "SERVER"  [:servername :hopcount :info]
  "JOIN"    [:channels :keys]
  "PART"    [:channels]
  "QUIT"    [:message]
  "MODE"    [:channel :mod :limit :user :mask]
  "TOPIC"   [:channel :topic]
  "NAMES"   []
  "LIST"    []
  "INVITE"  []
  "SQUIT"   []
  "KICK"    []
  "OPER"    [:user :password]
  "VERSION" []
  "STATS"   []
  "LINKS"   []
  "TIME"    []
  "CONNECT" []
  "TRACE"   [:server]
  "ADMIN"   []
  "INFO"    []
})

(defn- do-parse [x]
  (let [left (parser x)]
    (if (insta/failure? left)
      nil
      (loop [left left
             msg { :params nil :command nil :prefix nil }]
        (if (empty? left)
          msg
          (let [section (first left)
                kind (first section)
                value (if (or (= kind :command) (= kind :prefix)) (first (rest section)) (rest section))]
            (recur (rest left) (assoc msg kind value))))))))

(defn- parse->message
  [parse]
  (if (nil? parse)
    nil
    (let [command-name (:command parse)
          params (:params parse)
          msg (merge parse (zipmap (command-info command-name) params))]
      (dissoc msg :params))))

(defn- params->string
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

(defn parse [text] (parse->message (do-parse text)))
