(defproject dev "0.1.0-SNAPSHOT"
  :description "A playground for clojurescript based UIs"
  :url "http://roycetownsend.com/"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}

  :dependencies [[org.clojure/clojure "1.7.0"]
                 [org.clojure/clojurescript "1.7.170"]
                 [devcards "0.2.1-2"]
                 [sablono "0.5.3"]
                 [cljsjs/react "0.14.3-0"]
                 [cljsjs/react-dom "0.14.3-1"]
                 [cljsjs/react-dom-server "0.14.3-0"]
                 [com.rpl/specter "0.7.1"]
                 [rum "0.6.0"]
                 [vlad "3.3.0"]
                 [org.omcljs/om "1.0.0-alpha29-SNAPSHOT"]
                 [datascript "0.13.1"]]

  :plugins [[lein-cljsbuild "1.1.1"]
            [lein-figwheel "0.5.0-2"]]

  :clean-targets ^{:protect false} ["resources/public/js/compiled"
                                    "target"]

  :source-paths ["src"]

  :cljsbuild {
              :builds [{:id "devcards"
                        :source-paths ["src"]
                        :figwheel { :devcards true } ;; <- note this
                        :compiler { :main       "dev.core"
                                    :asset-path "js/compiled/devcards_out"
                                    :output-to  "resources/public/js/compiled/dev_devcards.js"
                                    :output-dir "resources/public/js/compiled/devcards_out"
                                    :source-map-timestamp true }}
;;                        {:id "prod"
;;                         :source-paths ["src"]
;;                         :compiler {:main       "dev.core"
;;                                    :asset-path "js/compiled/out"
;;                                    :output-to  "resources/public/js/compiled/dev.js"
;;                                    :optimizations :advanced}}
                       ]}

  :figwheel { :css-dirs ["resources/public/css"] })
