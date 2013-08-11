DW Data Profiling
=================

Run Web Application
-------------------

```
$ mvn jetty:run
```

Run Builder Job
---------------

```
$ mvn compile exec:java -Dexec.mainClass=com.anjuke.dw.data_profiling.job.Builder
```
