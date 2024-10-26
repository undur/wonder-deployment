# Changelog

We're currently going through changes fast so changes are merely listed by date. This will change once I get around to do proper versioning.

## 2024-10-26


* Upgraded Monitor's look to something a little more modern. The new look is based on the [tabler](https://www.tabler.io/) template, which itself is based on bootstrap 5.3.
* Converted all components to inline bindings.
* Made Monitor logins persistent (well, at least for the lifetime of the session).
* Moved sessionID from URL to cookie (to enable the persistent logins).
* Made those darn links into the actual monitored applications a little more explicit about where they point. I've been pressing those things accidentally for 25 years. Links on application names and instance IDs now point to their corresponding configuration pages. As They Should.