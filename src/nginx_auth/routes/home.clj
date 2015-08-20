(ns nginx-auth.routes.home
  (:require [nginx-auth.layout :as layout]
            [compojure.core :refer [defroutes GET POST]]
            [ring.util.response :refer [response redirect content-type]]
            [ring.util.http-response :refer [ok]]
            [environ.core :refer [env]]
            [clj-http.client :as client]
            [taoensso.timbre :as timbre]))

(defn home-page [request]
  (layout/render
    "home.html" {:target   (get-in request [:headers "x-target"])
                 :identity (get-in request [:session :identity])}))

(defn private-page [request]
  (println "private page requested: identity is " (get-in request [:session :identity]))
  (layout/render "private.html" {:identity (get-in request [:session :identity])}))

(defn logout []
  (-> (redirect "/")
      (assoc :session {})))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Authentication                                   ;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn authenticate-using-http-basic-auth [url username password]
  (timbre/error (str "authenticating using " url))
  (try (if (client/head url {:basic-auth [username password]}) true)
       (catch Exception _ nil)))

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
        next-url (get-in request [:form-params "target"] "/private")
        session (:session request)]
    (if (authenticate-using-http-basic-auth (env :basic-auth-url) username password)
      (do
        (let [updated-session (assoc session :identity (keyword username))]
          (-> (redirect next-url)
              (assoc :session updated-session))))
      (home-page {:headers {"x-target" next-url}}))))

; unrestricted access
(defroutes base-routes
           (GET "/" request (home-page request))
           (POST "/" request (login-authenticate request))
           (GET "/logout" [] (logout)))

; restricted access
(defroutes home-routes
           (GET "/auth-check" request (private-page request)))

