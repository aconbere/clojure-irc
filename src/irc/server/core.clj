(ns irc.server.core
  (:require [instaparse.core :as insta])
  (:use [lamina core] [aleph tcp] [gloss core] [irc core] [irc.server session]))

(defn handler [ch client-info]
  (let [session-state (ref initial-state)]
    (receive-all
      (->> ch (map* parse) (filter* identity))
      (fn [msg]
        (println msg)
        (if (registered? session-state)

          (let [n (next-state nil (deref session-state) msg)]
            (println n)
            (if (failure? n)
              (do
                (enqueue ch (error-msg n))
                (close ch)
              )
              (dosync (ref-set session-state n)))))))))

(defn start-server [port state]
  (start-tcp-server
    handler
    {:port port, :frame (string :utf-8 :delimiters ["\r\n"])}))

;; (defn -main [] (start-server 6667 handler))
