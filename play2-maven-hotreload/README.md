# play2-maven-hotreload Example
Shows how to run Play 2 Framework on Maven with hotreload functionality.

One of Play's killer features is to reload source code changes on the fly.
This feature is only available out-of-the-box on SBT, this shows how to
achieve it on Maven as well.

Requires DCEVM full installed as an alternative JVM. Run with:

```
mvn -P hotreload compile play2:run
```

Then attach eclipse's remote debugger to localhost:8000. For more detailed instructions, see the [blog post tutorial](http://www.webdevelopersdiary.com/blog/play-2-framework-on-maven-with-auto-reloading-through-ide).