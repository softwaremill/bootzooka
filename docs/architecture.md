---
layout: default
title:  "High-level architecture"
---

Bootzooka uses different technologies for the frontend and backend parts, taking the best of both worlds and combining in a single build. Bootzooka is structured how many modern web applications are done these days.

The backend server, written using Scala, exposes a JSON API which can be consumed by any client you want. In case of Bootzooka this client is a single-page browser application built with React. Such an approach allows better scaling and independent development of the server and client parts. This separation is mirrored in how Bootzooka projects are structured.

There's one sub-project for backend code and one for client-side application. They are completely unrelated in terms of code and dependencies. `ui` directory contains the browser part (JavaScript, CSS, HTML) and `backend` contains the backend application.
