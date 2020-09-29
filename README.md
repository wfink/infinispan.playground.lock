Infinispan Clustered Counters
===============================

Author: Wolf-Dieter Fink
Level: Basics
Technologies: Infinispan, ClusteredLocks


What is it?
-----------

Examples how to use the ClusteredLocks feature in embedded mode with different configurations



Build and Run the examples
-------------------------
1. Type this command to build :

        mvn clean package

2. Use a simple clustered locks

   Use maven to start one or more clients for StrongCounter

         mvn exec:java

   Use the interactive shell to define, lock and release locks. To show the clustered feature you need to run the exec command in different shells
   to have more than one instance.

   In case of issues with the cluster discovery you might need to set the correct bind address like followed

         mvn exec:java -Djgroups.bind_addr=<your IP>
