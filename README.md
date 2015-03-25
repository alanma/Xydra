**Xydra** is a **generic, embeddable application data model** that runs

  * seamlessly in **Java** (makes testing easy),
  * on **Google AppEngine** (GAE/J), and also via
  * **Google Web Toolkit** (GWT) as JavaScript in a browser.

![http://xydra.googlecode.com/svn/trunk/org.xydra.doc/src/test/resources/Xydra-Logo-80h.png](https://github.com/xamde/Xydra/blob/master/org.xydra.doc/src/test/resources/Xydra-Logo-80h.png?raw=true)
### Xydra Features ###
  * Persistence (currently: in-memory, in GAE data store)
  * Events (for all added, removed, or changed entites)
  * Commands (which can be serialized, too)
  * Transactions (over as many entities as you like)
  * Versioning (including a full change log)
  * Access Rights Management (with powerful resources hierarchies and nested user groups)
  * Synchronisation of state between client and server (in strict or relaxed mode)

### Who needs Xydra? ###
  * Web developers who build **rich web clients using GWT**,
  * Cloud application developers who use **Google AppEngine and need versioning of data** and/or access rights,
  * Social application developers who need to **sync several clients (in GWT or Java) on the same set of data**,
  * Or developers needing **all** of that.

### Get started ###
  * Get a Xydra5MinuteTutorial (see wiki)
  * Then check out the XydraBasics (see wiki).
  * Read ModuleOverview 

If you plan on using Xydra, send me a message, and I can tell you, if it is a good fit for your project or not.  

---

### Source code projects that can be used without Xydra ###
These projects have no dependency to any Xydra component. They are used _by_ Xydra, but they don't depend _on_ Xydra.

  * RestLess - Fast-loading, lightweight Java REST framework for AppEngine -- boots very fast
  * XydraLog - Unified logging for Java, AppEngine and GWT
  * GaeMyAdmin - Easy-to-install AppEngine version to manage your live app in the browser - doesn't offer much :-)
  * XydraIndex - Multi-level index structures (e.g. triple index) that run also in GWT
