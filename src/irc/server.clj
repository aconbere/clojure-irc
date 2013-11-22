(ns irc.server
  (:require [instaparse.core :as insta])
  (:use [lamina core] [aleph tcp] [gloss core] [irc core]))

(defn handler [ch client-info]
  (receive-all ch
    (fn [input] 
      (let [msg (parse input)]
        (when-not (insta/failure? msg)
          (enqueue ch (str "You said " msg)))))))

(defn start-server [port]
  (start-tcp-server
    handler
    {:port port, :frame (string :utf-8 :delimiters ["\r\n"])}))

(defn -main [] (start-server 6667))
