(defproject dev "0.1.0-SNAPSHOT"
  :description "A playground for clojurescript based UIs"
  :url "http://roycetownsend.com/"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}

  :dependencies [[org.clojure/clojure "1.7.0"]
                 [org.clojure/clojurescript "1.7.122"]
                 [devcards "0.2.0-1"]
                 [sablono "0.3.4"]
                 [com.rpl/specter "0.7.1"]
                 [rum "0.4.1"]
                 [vlad "3.3.0"]]

  :plugins [[lein-cljsbuild "1.1.0"]
            [lein-figwheel "0.4.0"]]

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
