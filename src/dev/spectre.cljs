(ns dev.spectre
  (:require
   [com.rpl.specter :as s :refer [select setval ALL]]
   [rum.core :as rum]
   [cljs.core.async :as async]
   [clojure.string]
   [vlad.core :as vlad]
   [dev.validation :refer [condition numeric-string phone-number-mask]]
   [cljs.test])
  (:require-macros
   [cljs.test :refer [is testing]]
   [cljs.core.async.macros :refer [go-loop]]
   [devcards.core :as dc :refer [defcard deftest]]))

(enable-console-print!)


(def masks {[:form :age] (numeric-string)
            [:form :phone] phone-number-mask})




(defonce state (atom {:_counter 0
                      :heading "The heading"
                      :form {:name "Jack" :age "" :phone ""}}))
(defonce bus
  (let [ch (async/chan)]
    (go-loop []
             (let [[_ path v] (async/<! ch)
                   valid (vlad/valid? (or (masks path) vlad/valid) v)]
               (println [_ path v])
               (swap! state (fn [state]
                              (if valid
                                (setval path v state)
                                (update state :_counter inc)))))
             (recur))
    ch))


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
   (text state [:form :age]  bus {:label "Age"})
   (text state [:form :phone]  bus {:label "Phone"})])

(defcard ui (fn [s _] (ui @s bus)) state)


(defcard state state)
