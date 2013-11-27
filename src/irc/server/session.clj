(ns irc.server.session (:use [match]))

(defn make-user-session-state
  ([state password nick user]
   {:state state :password password :nick nick :user user})
  ([]
   (make-session-state :start nil nil nil)))

(defn next-state []
  (match state
   {:state :start :value value}
     (match msg
       ;; PASS, NICK and USER are some kind of super secret function that
       ;; knows how to pull these values out of their underlying maps / structs / records
       ;; One for each kind of message needs to be built
       (PASS password)
          (set-state :start { :password password })
       (NICK nickname)
        (if (nick-in-use server nick)
          (fail state "Nick already in use")
          (do
            (set-state :nick { :nick nickname })))
        (_) state)

   {:state :nick :value value}
     (match msg
       (USER username hostname servername realname)
           (if (and (requires-auth server) (valid-auth server username (:password value)) )
             (set-state :user {:username username})
             (fail state "Invalid password"))
       (_) state)))

(defn make-session
  ([connection] (make-session connection nil :))
  ([connection user state] { connection connection :user user :state state }))
