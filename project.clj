(defproject ynab-telegram "0.1.0-SNAPSHOT"
  :description "YNAB notification and tagging bot for Telegram"
  :url "http://github.com/otann/ynab-telegram"
  :license {:name "EPL-2.0 OR GPL-2.0-or-later WITH Classpath-exception-2.0"
            :url "https://www.eclipse.org/legal/epl-2.0/"}
  :dependencies [[org.clojure/clojure "1.10.1"]
                 [morse "0.5.0-SNAPSHOT"]
                 [cheshire "5.10.2"]
                 [wrench "0.3.3"]]
  :main ^:skip-aot ynab-telegram.core
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all
                       :jvm-opts ["-Dclojure.compiler.direct-linking=true"]}})
