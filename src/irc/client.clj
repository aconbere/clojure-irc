(ns irc.client
  (:require [instaparse.core :as insta])
  (:use [lamina core] [aleph tcp] [gloss core] [irc core]))

(defn make-tcp-client [host, port]
  (wait-for-result
    (tcp-client
      {:host host
       :port port
       :frame (string :utf-8 :delimiters ["\r\n"])})))

(defn connect [client user nick pass]
  (enqueue client [ (messages.user user) (messages.nick nick) (messages.pass pass) ]))
