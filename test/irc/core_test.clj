(ns irc.core-test
  (:require [clojure.test :refer :all]
            [irc.core :refer :all]))

(def provide-test-message-parsing [
  ["PASS secretpasswordhere" '([:command "PASS"] [:params "secretpasswordhere"])]
  ["NICK WiZ" '([:command "NICK"] [:params "WiZ" ])]
  [":WiZ NICK Kilroy" '([:prefix "WiZ"] [:command "NICK"] [:params "Kilroy"])]
  ["USER guest tolmoon tolsun :Ronnie Reagan" '([:command "USER"] [:params "guest" "tolmoon" "tolsun" "Ronnie Reagan"])]
  [":testnick USER guest tolmoon tolsun :Ronnie Reagan" '([:prefix "testnick"] [:command "USER"] [:params "guest" "tolmoon" "tolsun" "Ronnie Reagan"])]
  ["SERVER test.oulu.fi 1 :[tolsun.oulu.fi] Experimental server" '([:command "SERVER"] [:params "test.oulu.fi" "1" "[tolsun.oulu.fi] Experimental server"])]
  [":tolsun.oulu.fi SERVER csd.bu.edu 5 :BU Central Server" '([:prefix "tolsun.oulu.fi"] [:command "SERVER"] [:params "csd.bu.edu" "5" "BU Central Server"])]
  ["OPER foo bar" '([:command "OPER"] [:params "foo" "bar"])]
  ["QUIT :Gone to have lunch" '([:command "QUIT"] [:params "Gone to have lunch"])]
  ["SQUIT tolsun.oulu.fi :Bad Link ?" '([:command "SQUIT"] [:params "tolsun.oulu.fi" "Bad Link ?"])]
  [":Trillian SQUIT cm22.eng.umd.edu :Server out of control" '([:prefix "Trillian"] [:command "SQUIT"] [:params "cm22.eng.umd.edu" "Server out of control"])]
  ["JOIN #foobar" '([:command "JOIN"] [:params "#foobar"])]
  ["JOIN &foo fubar" '([:command "JOIN"] [:params "&foo" "fubar"])]
  ["JOIN #foo,&bar fubar" '([:command "JOIN"] [:params "#foo,&bar" "fubar"])]
  ["JOIN #foo,#bar fubar,foobar" '([:command "JOIN"] [:params "#foo,#bar" "fubar,foobar"])]
  ["JOIN #foo,#bar" '([:command "JOIN"] [:params "#foo,#bar"])]
  [":WiZ JOIN #Twilight_zone" '([:prefix "WiZ"] [:command "JOIN"] [:params "#Twilight_zone"])]
  ["PART #twilight_zone" '([:command "PART"] [:params "#twilight_zone"])]
  ["PART #oz-ops,&group5" '([:command "PART"] [:params "#oz-ops,&group5"])]
  ["MODE #Finnish +im" '([:command "MODE"] [:params "#Finnish" "+im"])]
  ["MODE #Finnish +o Kilroy" '([:command "MODE"] [:params "#Finnish" "+o" "Kilroy"])]
  ["MODE #Finnish +v WiZ" '([:command "MODE"] [:params "#Finnish" "+v" "WiZ"])]
  ["MODE #Fins -s" '([:command "MODE"] [:params "#Fins" "-s"])]
  ["MODE #42 +k oulu" '([:command "MODE"] [:params "#42" "+k" "oulu"])]
  ["MODE #eu-opers +l 10" '([:command "MODE"] [:params "#eu-opers" "+l" "10"])]
  ["MODE &oulu +b" '([:command "MODE"] [:params "&oulu" "+b"])]
  ["MODE &oulu +b *!*@*" '([:command "MODE"] [:params "&oulu" "+b" "*!*@*"])]
  ["MODE &oulu +b *!*@*.edu" '([:command "MODE"] [:params "&oulu" "+b" "*!*@*.edu"])]
  [":MODE WiZ -w" '([:prefix "MODE"] [:command "WiZ"] [:params "-w"])]
  [":Angel MODE Angel +i" '([:prefix "Angel"] [:command "MODE"] [:params "Angel" "+i"])]
  ["MODE WiZ -o" '([:command "MODE"] [:params "WiZ" "-o"])]
  [":WiZ TOPIC #test :New topic" '([:prefix "WiZ"] [:command "TOPIC"] [:params "#test" "New topic"])]
  ["TOPIC #test :another topic" '([:command "TOPIC"] [:params "#test" "another topic"])]
  ["TOPIC #test" '([:command "TOPIC"] [:params "#test"])]
  ["NAMES #twilight_zone,#42" '([:command "NAMES"] [:params "#twilight_zone,#42"])]
  ["NAMES" '([:command "NAMES"] [:params])]
  ["LIST" '([:command "LIST"] [:params])]
  ["LIST #twilight_zone,#42" '([:command "LIST"] [:params "#twilight_zone,#42"])]
  [":Angel INVITE WiZ #Dust" '([:prefix "Angel"] [:command "INVITE"] [:params "WiZ" "#Dust"])]
  ["INVITE WiZ #Twilight_Zone" '([:command "INVITE"] [:params "WiZ" "#Twilight_Zone"])]
  ["KICK &Melbourne Matthew" '([:command "KICK"] [:params "&Melbourne" "Matthew"])]
  ["KICK #Finnish John :Speaking English" '([:command "KICK"] [:params "#Finnish" "John" "Speaking English"])]
  [":WiZ KICK #Finnish John" '([:prefix "WiZ"] [:command "KICK"] [:params "#Finnish" "John"])]
  [":WiZ VERSION *.se" '([:prefix "WiZ"] [:command "VERSION"] [:params "*.se"])]
  ["VERSION tolsun.oulu.fi" '([:command "VERSION"] [:params "tolsun.oulu.fi"])]
  [":WiZ STATS c eff.org" '([:prefix "WiZ"] [:command "STATS"] [:params "c" "eff.org"])]
  ["STATS m" '([:command "STATS"] [:params "m"])]
  ["LINKS *.au" '([:command "LINKS"] [:params "*.au"])]
  [":WiZ LINKS *.bu.edu *.edu" '([:prefix "WiZ"] [:command "LINKS"] [:params "*.bu.edu" "*.edu"])]
  ["TIME tolsun.oulu.fi" '([:command "TIME"] [:params "tolsun.oulu.fi"])]
  ["Angel TIME *.au" '([:command "Angel"] [:params "TIME" "*.au"])]
  ["CONNECT tolsun.oulu.fi" '([:command "CONNECT"] [:params "tolsun.oulu.fi"])]
  [":WiZ CONNECT eff.org 6667 csd.bu.edu" '([:prefix "WiZ"] [:command "CONNECT"] [:params "eff.org" "6667" "csd.bu.edu"])]
  ["TRACE *.oulu.fi" '([:command "TRACE"] [:params "*.oulu.fi"])]
  [":WiZ TRACE AngelDust" '([:prefix "WiZ"] [:command "TRACE"] [:params "AngelDust"])]
  ["ADMIN tolsun.oulu.fi" '([:command "ADMIN"] [:params "tolsun.oulu.fi"])]
  [":WiZ ADMIN *.edu" '([:prefix "WiZ"] [:command "ADMIN"] [:params "*.edu"])]
  ["KILL David (csd.bu.edu <- tolsun.oulu.fi)" '([:command "KILL"] [:params "David" "(csd.bu.edu" "<-" "tolsun.oulu.fi)"])]
])

(deftest test-message-parsing
  (doseq [[input expected] provide-test-message-parsing]
    (is (= expected (parser input)))))

(def provide-test-message->string
  [
   [{ :command "PRIVMSG" :params ["aconbere" "what's up?"]} "PRIVMSG aconbere :what's up?"]
   [{ :command "ADMIN" :params ["*.edu"] :prefix "WiZ" } ":WiZ ADMIN :*.edu" ]
])

(deftest test-message->string
  (doseq [[input expected] provide-test-message->string]
    (is (= expected (message->string input)))))

(def provide-test-parse [
  ["USER guest tolmoon tolsun :Ronnie Reagan" { :command "USER" :params '("guest" "tolmoon" "tolsun" "Ronnie Reagan") :prefix nil}]
  [":WiZ TRACE AngelDust" { :command "TRACE" :params '("AngelDust") :prefix "WiZ" }]
])

(deftest test-parse
  (doseq [[input expected] provide-test-parse]
    (is (= expected (parse input)))))
