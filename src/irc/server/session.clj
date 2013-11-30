(ns irc.server.session (:use [irc.core]))

(defn- nick-in-use? [server nick] false)
(defn- valid-auth? [server username password] true)

(defn- state-get
  [state k]
  (k (:value state)))

(defn- state-update
  [state new-state update]
  (let [state (update-in state [:value] merge update)
        state (assoc state :state new-state)]
    state))

(defn- state-join
  [state name]
  (update-in state [:value :rooms] conj name))

(defn- fail
  [state message]
  (assoc state :state :fail, :reason message))

(defn- join-channel
  [server state command]
  (let [{ channels :channels } command]
    (map channels (fn [[ name key] channel]
      (if (server-valid-channel-pass name key)
        (do
          (server-join (state-get :username) name key)
          (state-join state name))
        nil)))))

(defn- handle-user
  [server state command]
  (let [{username :username } command
         password (state-get :password)]
    (if (valid-auth? server username password)
      (state-update state :registered command)
      (fail state "Invalid password"))))

(defn- handle-password
  [server state { password :password }]
  (state-update state :pass { :password password }))

(defn- handle-nick
  [server state { nick :nick }]
  (if (nick-in-use? server nick)
    (fail state "Nick already in use")
    (state-update state :nick { :nick nick })))

(def initial-state { :state :start :value {} })

(defn registered?
  [state]
  (= :registered (:state state)))

(defn failure?
  [state]
  (= :fail (:state state)))

(defn error-msg
  [state]
  (if (failure? state)
    (-> state :state :reason)
    nil))

;; :start -> :pass -> :nick -> :user -> :registered
(defmulti next-state (fn [_ state message] [(:state state) (:command message)]))

(defmethod next-state [:start "PASS"][server state command](handle-password server state command)) 
(defmethod next-state [:pass "PASS"] [server state command] (handle-password server state command))
(defmethod next-state [:pass "NICK"] [server state command] (handle-nick server state command))
(defmethod next-state [:nick "NICK"] [server state command] (handle-nick server state command))
(defmethod next-state [:nick "USER"] [server state command] (handle-user server state command))
(defmethod next-state [:registered "JOIN"] [server state command] (handle-user server state command))
(defmethod next-state :default        [server state command] (fail state "Invalid message"))
