(defproject core-async-demo "0.1.0-SNAPSHOT"
  :description "This is a simple demo of asynchronous programming written for the purpose of a blog post."
  :url "https://github.com/AlexanderMann/core-async-demo"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :source-paths ["src"]
  :test-paths ["test"]
  :jvm-opts ["-Duser.timezone=GMT"]
  :main core-async-demo.core
  :dependencies [[org.clojure/clojure "1.8.0"]])
