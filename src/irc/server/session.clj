(ns irc.server.session (:use [irc.core]))

(defn- nick-in-use? [server nick] false)
(defn- valid-auth? [server username password] false)

(defn- state-get
  [state k]
  (k (:value state)))

(defn- state-update
  [state new-state update]
  (assoc state :state new-state)
  (update-in state [:value] merge update))

(defn- fail
  [state message]
  (assoc state :state :fail :reason message))

(def initial-state { :state :start :value {} })

(defmulti next-state (fn [_ state message] [(:state state) (:command message)]))

(defmethod next-state [:start "PASS"]
  [server state { password :password }]
  (state-update state :start { :password password }))

(defmethod next-state [:start "NICK"]
  [server state { nick :nick }]
  (if (nick-in-use? server nick)
    (fail state "Nick already in use")
    (do
      (state-update state :nick { :nick nick }))))

(defmethod next-state [:nick "USER"]
  [server state command]
  (let [{ username :username } command
        password (state-get :password)]
    (if (valid-auth? server username password)
      (state-update state :user command)
      (fail state "Invalid password"))))

(defmethod next-state :default
  [server state command]
  state)
