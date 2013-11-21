(ns irc.core
  (:require [instaparse.core :as insta])
  (:import [io.netty.channel ChannelInboundHandlerAdapter]
           [io.netty.buffer.ByteBuf]))

(def grammar-file (clojure.java.io/resource "grammar.txt"))
(def parser (insta/parser (slurp grammar-file)))
(defn parse [x] (parser x))

