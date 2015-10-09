(ns dev.spectre_vlad_event_bus_global_state
  (:require
   [com.rpl.specter :as s :refer [select setval ALL]]
   [rum.core :as rum]
   [cljs.core.async :as async]
   [clojure.string]
   [vlad.core :as vlad]
   [dev.validation :refer [condition numeric-string phone-number-mask]]
   )
  (:require-macros
   [cljs.core.async.macros :refer [go-loop]]
   [devcards.core :as dc :refer [defcard]]))

(enable-console-print!)


(def masks {[:form :age] (numeric-string)
            [:form :phone] phone-number-mask})
;; (def required {[:form]})

(defn new-state [state [_ path value] {:keys [masks]}]
  (let [mask (condition (vlad/present) (masks path))
        mask-validation (vlad/validate (or mask vlad/valid) value)
        state (if (empty? mask-validation)
                (setval path value state)
                (setval [:warning]
                        (map vlad.core/english-translation mask-validation)
                        state))]
    state))

(defcard n (new-state {:form {:age "12" :phone "95359513"}}
                      [:setval [:form :age] "a"]
                      {:masks masks}))


(defonce state (atom {:_counter 0
                      :heading "The heading"
                      :form {:name "Jack" :age "" :phone ""}
                      :_warning {:form {:age "Too young"}}
                      :_error {}}))

(defonce bus
  (let [ch (async/chan)]
    (go-loop []
             (let [msg (async/<! ch)]
               (println msg)
               (swap! state #(new-state % msg {:masks masks})))
             (recur))
    ch))


(rum/defc text < rum/static [state path bus {:keys [label]}]
  (let [id (clojure.string/join path)
        error   (first (select path (state :error)))
        warning (first (select path (state :warning)))]
    [:div
     [:label {:for id} label]
     [:input {:id id
              :value (select path state)
              :on-change
              (fn [e]
                (.preventDefault e)
                (async/put! bus [:setval path (.. e -target -value)]))}]
     (if error error (if warning warning))]))

(rum/defc ui < rum/static [state bus]
  [:div
   [:h1 (state :heading)]
   (text state [:form :name] bus {:label "Name"})
   (text state [:form :age]  bus {:label "Age"})
   (text state [:form :phone]  bus {:label "Phone"})])

(defcard ui (fn [s _] (ui @s bus)) state)


(defcard state state)
