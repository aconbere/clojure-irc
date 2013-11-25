(ns irc.core-test
  (:require [clojure.test :refer :all]
            [irc.core :refer :all]))

(def provide-test-message-parsing [
  ["PASS secretpasswordhere" '([:command "PASS"] [:param "secretpasswordhere"])]
  ["NICK WiZ" '([:command "NICK"] [:param "WiZ" ])]
  [":WiZ NICK Kilroy" '([:prefix [:nick "WiZ"]] [:command "NICK"] [:param "Kilroy"])]
  ["USER guest tolmoon tolsun :Ronnie Reagan" '([:command "USER"] [:param "guest"] [:param "tolmoon"] [:param "tolsun"] [:param "Ronnie Reagan"])]
  [":testnick USER guest tolmoon tolsun :Ronnie Reagan" '([:prefix [:nick "testnick"]] [:command "USER"] [:param "guest"] [:param "tolmoon"] [:param "tolsun"] [:param "Ronnie Reagan"])]
  ["SERVER test.oulu.fi 1 :[tolsun.oulu.fi] Experimental server" '([:command "SERVER"] [:param "test.oulu.fi"] [:param "1"] [:param "[tolsun.oulu.fi] Experimental server"])]
  [":tolsun.oulu.fi SERVER csd.bu.edu 5 :BU Central Server" '([:prefix [:servername "tolsun.oulu.fi"]] [:command "SERVER"] [:param "csd.bu.edu"] [:param "5"] [:param "BU Central Server"])]
  ["OPER foo bar" '([:command "OPER"] [:param "foo"] [:param "bar"])]
  ["QUIT :Gone to have lunch" '([:command "QUIT"] [:param "Gone to have lunch"])]
  ["SQUIT tolsun.oulu.fi :Bad Link ?" '([:command "SQUIT"] [:param "tolsun.oulu.fi"] [:param "Bad Link ?"])]
  [":Trillian SQUIT cm22.eng.umd.edu :Server out of control" '([:prefix [:nick "Trillian"]] [:command "SQUIT"] [:param "cm22.eng.umd.edu"] [:param "Server out of control"])]
  ["JOIN #foobar" '([:command "JOIN"] [:param "#foobar"])]
  ["JOIN &foo fubar" '([:command "JOIN"] [:param "&foo"] [:param "fubar"])]
  ["JOIN #foo,&bar fubar" '([:command "JOIN"] [:param "#foo,&bar"] [:param "fubar"])]
  ["JOIN #foo,#bar fubar,foobar" '([:command "JOIN"] [:param "#foo,#bar"] [:param "fubar,foobar"])]
  ["JOIN #foo,#bar" '([:command "JOIN"] [:param "#foo,#bar"])]
  [":WiZ JOIN #Twilight_zone" '([:prefix [:nick "WiZ"]] [:command "JOIN"] [:param "#Twilight_zone"])]
  ["PART #twilight_zone" '([:command "PART"] [:param "#twilight_zone"])]
  ["PART #oz-ops,&group5" '([:command "PART"] [:param "#oz-ops,&group5"])]
  ["MODE #Finnish +im" '([:command "MODE"] [:param "#Finnish"] [:param "+im"])]
  ["MODE #Finnish +o Kilroy" '([:command "MODE"] [:param "#Finnish"] [:param "+o"] [:param "Kilroy"])]
  ["MODE #Finnish +v WiZ" '([:command "MODE"] [:param "#Finnish"] [:param "+v"] [:param "WiZ"])]
  ["MODE #Fins -s" '([:command "MODE"] [:param "#Fins"] [:param "-s"])]
  ["MODE #42 +k oulu" '([:command "MODE"] [:param "#42"] [:param "+k"] [:param "oulu"])]
  ["MODE #eu-opers +l 10" '([:command "MODE"] [:param "#eu-opers"] [:param "+l"] [:param "10"])]
  ["MODE &oulu +b" '([:command "MODE"] [:param "&oulu"] [:param "+b"])]
  ["MODE &oulu +b *!*@*" '([:command "MODE"] [:param "&oulu"] [:param "+b"] [:param "*!*@*"])]
  ["MODE &oulu +b *!*@*.edu" '([:command "MODE"] [:param "&oulu"] [:param "+b"] [:param "*!*@*.edu"])]
  [":MODE WiZ -w" '([:prefix [:nick "MODE"]] [:command "WiZ"] [:param "-w"])]
  [":Angel MODE Angel +i" '([:prefix [:nick "Angel"]] [:command "MODE"] [:param "Angel"] [:param "+i"])]
  ["MODE WiZ -o" '([:command "MODE"] [:param "WiZ"] [:param "-o"])]
  [":WiZ TOPIC #test :New topic" '([:prefix [:nick "WiZ"]] [:command "TOPIC"] [:param "#test"] [:param "New topic"])]
  ["TOPIC #test :another topic" '([:command "TOPIC"] [:param "#test"] [:param "another topic"])]
  ["TOPIC #test" '([:command "TOPIC"] [:param "#test"])]
  ["NAMES #twilight_zone,#42" '([:command "NAMES"] [:param "#twilight_zone,#42"])]
  ["NAMES" '([:command "NAMES"])]
  ["LIST" '([:command "LIST"])]
  ["LIST #twilight_zone,#42" '([:command "LIST"] [:param "#twilight_zone,#42"])]
  [":Angel INVITE WiZ #Dust" '([:prefix [:nick "Angel"]] [:command "INVITE"] [:param "WiZ"] [:param "#Dust"])]
  ["INVITE WiZ #Twilight_Zone" '([:command "INVITE"] [:param "WiZ"] [:param "#Twilight_Zone"])]
  ["KICK &Melbourne Matthew" '([:command "KICK"] [:param "&Melbourne"] [:param "Matthew"])]
  ["KICK #Finnish John :Speaking English" '([:command "KICK"] [:param "#Finnish"] [:param "John"] [:param "Speaking English"])]
  [":WiZ KICK #Finnish John" '([:prefix [:nick "WiZ"]] [:command "KICK"] [:param "#Finnish"] [:param "John"])]
  [":WiZ VERSION *.se" '([:prefix [:nick "WiZ"]] [:command "VERSION"] [:param "*.se"])]
  ["VERSION tolsun.oulu.fi" '([:command "VERSION"] [:param "tolsun.oulu.fi"])]
  [":WiZ STATS c eff.org" '([:prefix [:nick "WiZ"]] [:command "STATS"] [:param "c"] [:param "eff.org"])]
  ["STATS m" '([:command "STATS"] [:param "m"])]
  ["LINKS *.au" '([:command "LINKS"] [:param "*.au"])]
  [":WiZ LINKS *.bu.edu *.edu" '([:prefix [:nick "WiZ"]] [:command "LINKS"] [:param "*.bu.edu"] [:param "*.edu"])]
  ["TIME tolsun.oulu.fi" '([:command "TIME"] [:param "tolsun.oulu.fi"])]
  ["Angel TIME *.au" '([:command "Angel"] [:param "TIME"] [:param "*.au"])]
  ["CONNECT tolsun.oulu.fi" '([:command "CONNECT"] [:param "tolsun.oulu.fi"])]
  [":WiZ CONNECT eff.org 6667 csd.bu.edu" '([:prefix [:nick "WiZ"]] [:command "CONNECT"] [:param "eff.org"] [:param "6667"] [:param "csd.bu.edu"])]
  ["TRACE *.oulu.fi" '([:command "TRACE"] [:param "*.oulu.fi"])]
  [":WiZ TRACE AngelDust" '([:prefix [:nick "WiZ"]] [:command "TRACE"] [:param "AngelDust"])]
  ["ADMIN tolsun.oulu.fi" '([:command "ADMIN"] [:param "tolsun.oulu.fi"])]
  [":WiZ ADMIN *.edu" '([:prefix [:nick "WiZ"]] [:command "ADMIN"] [:param "*.edu"])]
  ["KILL David (csd.bu.edu <- tolsun.oulu.fi)" '([:command "KILL"] [:param "David"] [:param "(csd.bu.edu"] [:param "<-"] [:param "tolsun.oulu.fi)"])]
])

(deftest test-message-parsing
  (doseq [[input expected] provide-test-message-parsing]
    (is (= expected (raw-parse input)))))

(def provide-test-message->string
  [
   [(message "PRIVMSG" '( "aconbere" "what's up?" )) "PRIVMSG aconbere :what's up?"]
   [(message "WiZ" "ADMIN" '("*.edu")) ":WiZ ADMIN :*.edu" ]
  ]
)

(deftest test-message->string
  (doseq [[input expected] provide-test-message->string]
    (is (= expected (message->string input)))))

