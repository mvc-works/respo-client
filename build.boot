
(set-env!
 :asset-paths #{"assets"}
 :source-paths #{}
 :resource-paths #{"src"}
 :target-path "target/"

 :dev-dependencies '[]
 :dependencies '[[org.clojure/clojure "1.8.0"           :scope "provided"]
                 [org.clojure/clojurescript "1.7.228"   :scope "provided"]
                 [adzerk/boot-cljs "1.7.170-3"      :scope "test"]
                 [adzerk/boot-reload "0.4.6"        :scope "test"]
                 [mvc-works/boot-html-entry "0.1.1" :scope "test"]
                 [cirru/boot-cirru-sepal "0.1.1"    :scope "test"]
                 [binaryage/devtools "0.5.2"        :scope "test"]
                 [org.clojure/core.async "0.2.374"  :scope "test"]
                 [mvc-works/hsl "0.1.2"             :scope "test"]])

(require '[adzerk.boot-cljs :refer [cljs]]
         '[adzerk.boot-reload :refer [reload]]
         '[html-entry.core :refer [html-entry]]
         '[cirru-sepal.core :refer [cirru-sepal]])

(def +version+ "0.1.11")

(task-options!
  pom {:project     'mvc-works/respo-client
       :version     +version+
       :description "Client for Responsive DOM library"
       :url         "https://github.com/mvc-works/respo-client"
       :scm         {:url "https://github.com/mvc-works/respo-client"}
       :license     {"MIT" "http://opensource.org/licenses/mit-license.php"}})

(set-env! :repositories #(conj % ["clojars" {:url "https://clojars.org/repo/"}]))

(defn html-dsl [data]
  [:html
   [:head
    [:title "Respo Client"]
    [:link
     {:rel "stylesheet", :type "text/css", :href "style.css"}]
    [:link
     {:rel "icon", :type "image/png", :href "respo.png"}]
    [:style nil "body {margin: 0;}"]
    [:style
     nil
     "body * {box-sizing: border-box; scroll-behavior: smooth; }"]]
    [:script (if (= :build (:env data))
        "window._appConfig = {env: 'build'}"
        "window._appConfig = {env: 'dev'}")]
   [:body {:style "margin: 0;"}
    [:div#app] [:script {:src "main.js"}]]])

(deftask compile-cirru []
  (cirru-sepal :paths ["cirru-src"]))

(deftask dev []
  (comp
    (html-entry :dsl (html-dsl {:env :dev}) :html-name "index.html")
    (cirru-sepal :paths ["cirru-src"] :watch true)
    (watch)
    (reload :on-jsload 'respo-client.core/on-jsload)
    (cljs)
    (target)))

(deftask build-simple []
  (comp
    (compile-cirru)
    (cljs :optimizations :simple)
    (html-entry :dsl (html-dsl {:env :build}) :html-name "index.html")
    (target)))

(deftask build-advanced []
  (comp
    (compile-cirru)
    (cljs :optimizations :advanced :compiler-options {})
    (html-entry :dsl (html-dsl {:env :build}) :html-name "index.html")
    (target)))

(deftask rsync []
  (fn [next-task]
    (fn [fileset]
      (sh "rsync" "-r" "target/" "tiye:repo/mvc-works/respo-client" "--exclude" "main.out" "--delete")
      (next-task fileset))))

(deftask send-tiye []
  (comp
    (build-simple)
    (rsync)))

(deftask build []
  (comp
    (compile-cirru)
    (pom)
    (jar)
    (install)
    (target)))

(deftask deploy []
  (comp
    (build)
    (push :repo "clojars" :gpg-sign (not (.endsWith +version+ "-SNAPSHOT")))))
