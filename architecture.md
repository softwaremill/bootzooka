---
layout: default
title:  "High-level architecture"
---

Bootzooka is structured how many modern web applications are done these days.

The backend server exposes a JSON API which can be consumed by any client you want. In case of Bootzooka this client
is a single-page browser application built with AngularJS. Such approach allows better scaling and independent
development the server and client parts. This separation is mirrored in how Bootzooka projects are structured.

There are several sub-projects (directories) containing server-side code and one for client-side application. They are
completely unrelated in terms of code and dependencies. `ui` directory contains the browser part (JavaScript,
CSS, HTML) and `backend` contains the backend application. Additionaly, `dist` is an utility module
for building a "fat jar" distribution.
