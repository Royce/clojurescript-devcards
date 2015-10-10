(ns dev.spectre
  (:require
   [com.rpl.specter :as s :refer [select setval transform putval]]
   [cljs.test])
  (:require-macros
   [cljs.test :refer [is testing]]
   [devcards.core :as dc :refer [defcard defcard-doc]]))

(enable-console-print!)

(def path [:form :age])
(defcard-doc "Path" (dc/mkdn-pprint-source path))

(def state {:form {:age "12"
                   :phone "95359513"}})
(defcard-doc "state" (dc/mkdn-pprint-source state))


(defcard set (setval path
                   "VALUE"
                   state
                   ))

(defcard select (select path state))

(defcard set (setval (vec (concat [:_warning] path))
                     "Error message"
                     state))

(defcard select (select
                 (vec (concat [:_warning] path))
                 state))

(defcard-doc "state" (dc/mkdn-pprint-source transform))

(defcard remove (transform [:_warning :form (putval :age)]
                        #(dissoc %2 %1)
                        {:_warning {:form {:age "Error message"}}
                         :form {:age "12"
                                :phone "95359513"}}
                   ))

(defcard remove (update-in
                 {:_warning {:form {:age "Error message"}}
                  :form {:age "12"
                         :phone "95359513"}}
                 [:_warning :form]
                 dissoc :age
                 ))
