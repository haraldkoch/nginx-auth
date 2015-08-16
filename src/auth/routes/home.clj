(ns auth.routes.home
  (:require [auth.layout :as layout]
            [compojure.core :refer [defroutes GET POST]]
            [ring.util.response :refer [response redirect content-type]]
            [ring.util.http-response :refer [ok]]
            [clojure.java.io :as io]))

(defn home-page []
  (layout/render
    "home.html" {:docs (-> "docs/docs.md" io/resource slurp)}))

(defn about-page []
  (layout/render "about.html"))

(defn login [request]
  (layout/render "login.html"))

(defn private-page []
  (layout/render "private.html"))

(defn logout [request]
  (-> (redirect "/login")
      (assoc :session {})))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Authentication                                   ;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(def authdata
  "Global var that stores valid users with their
   respective passwords."
  {:admin "secret"
   :test  "secret"})

;; Authentication Handler
;; Used to respond to POST requests to /login.

(defn login-authenticate
  "Check request username and password against authdata username and passwords.
  On successful authentication, set appropriate user into the session and
  redirect to the value of (:query-params (:next request)).
  On failed authentication, renders the login page."
  [request]
  (println request)
  (let [username (get-in request [:form-params "username"])
        password (get-in request [:form-params "password"])
        session (:session request)
        found-password (get authdata (keyword username))]
    (if (and found-password (= found-password password))
      (do (println "login successful")
          (let [next-url (get-in request [:headers :x-target] "/private")
                updated-session (assoc session :identity (keyword username))]
            (println updated-session)
            (-> (redirect next-url)
                (assoc :session updated-session))))
      (layout/error-page
        {:status  403
         :title   "login failed"
         :message "The username or password entered is incorrect"}))))

; unrestricted access
(defroutes base-routes
           (GET "/" [] (home-page))
           (GET "/about" [] (about-page))
           (GET "/login" request (login request))
           (POST "/login" request (login-authenticate request))
           (GET "/logout" [] logout))

; restricted access
(defroutes home-routes
           (GET "/private" [] (private-page)))

