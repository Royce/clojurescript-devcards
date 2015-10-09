(ns dev.spectre
  (:require
   [com.rpl.specter :as s :refer [select setval ALL]]
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

(defcard select (select path
                   {:form {:age "12"
                           :phone "95359513"}}
                   ))

(defcard set (setval (vec (concat [:_warning] path))
                   "Error message"
                   {:form {:age "12"
                           :phone "95359513"}}
                   ))

(defcard select (select (vec (concat [:_warning] path))
                   {:form {:age "12"
                           :phone "95359513"}}
                   ))
