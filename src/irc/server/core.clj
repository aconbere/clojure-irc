(ns irc.server.core
  (:require [instaparse.core :as insta])
  (:use [lamina core] [aleph tcp] [gloss core] [irc core]))

;; thoughts
;; 1. rooms are channels
;; 2. private messages are channels
;;
;;
;; USER
;; 
;; { :username :nick :password :client }
;;
;; CLIENT
;;
;; { :channel }
;;
;; ROOM
;;
;; { :name :users :channel }
;;
;; SERVER
;;
;; { :rooms { <name> <room> }
;;   :users [ <user> ]
;;   :clients [ <client> ]
;;   :sessions [ <username> <client> }

(defn make-server 
  ([] (make-server {} {} {} {}))
  [ rooms users sessions clients ]
   { :rooms rooms :users users :sessions session :clients clients })

(defn make-user
  [username nick password]
  { :username username :nick nick :password password})

(defn make-room
  [name channel]
  { :name name :channel channel})

(defn make-client [channel] { :channel channel})

(defn add-user
  [state user]
  (update-in state [:users] conj user))

(defn add-room
  [state room]
  (update-in state [(:name room)] assoc room))

(defn add-client
  [state client]
  (update-in state [:users] conj client))

(defn make-handler 
  (fn [ch client-info]
    (receive-all ch
      (fn [input] 
        (let [msg (parse input)]
          (when-not (insta/failure? msg)
            (enqueue ch (str "You said " msg))))))))

(defn start-server [port state]
  (start-tcp-server
    handler
    {:port port, :frame (string :utf-8 :delimiters ["\r\n"])}))

(defn -main [] (start-server 6667 (make-handler)))

