(ns dev.spectre
  (:require
   [com.rpl.specter :as s :refer [select setval ALL]]
   [rum.core :as rum]
   [cljs.core.async :as async]
   [clojure.string])
  (:require-macros
   [cljs.core.async.macros :refer [go-loop]]
   [devcards.core :as dc :refer [defcard deftest]]))

(enable-console-print!)


;; Todo introduce vlad

(defonce state (atom {:heading "The heading" :form {:name "Jack" :url "sz.com"}}))
(defonce bus
  (let [ch (async/chan)]
    (go-loop []
             (let [[_ path v] (async/<! ch)]
               (println [_ path v])
               (swap! state #(setval path v %)))
             (recur))
    ch))

(defcard state state)



(rum/defc text < rum/static [state path bus {:keys [label]}]
  (let [id (clojure.string/join path)]
    [:div
     [:label {:for id} label]
     [:input {:id id
              :value (select path state)
              :on-change
              (fn [e]
                (.preventDefault e)
                (async/put! bus [:setval path (.. e -target -value)]))}]]))

(rum/defc ui < rum/static [state bus]
  [:div
   [:h1 (state :heading)]
   (text state [:form :name] bus {:label "Name"})
   (text state [:form :url]  bus {:label "URL"})])

(defcard ui (fn [s _] (ui @s bus)) state)
