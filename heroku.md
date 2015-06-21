---
layout: default
title:  "Heroku deployment"
---

Bootzooka-based applications can be easily deployed to [Heroku](https://www.heroku.com).

First, you need to create a Heroku account and install the [toolbelt](https://toolbelt.heroku.com).
Once this is done, login to heroku from the command line with `heroku login` while in the application's main directory.

From there you can create a new application, e.g.:

````
heroku create myappname
````

You now have a new application, which should be also visible in Heroku's web console.
Using a file-system based H2 database on a non-dev environment isn't probably a good choice, so you can add a free, entry-level Postgres database with

````
heroku addons:create heroku-postgresql:hobby-dev
````

Bootzooka already includes the Postgres driver and properly recognizes the `DATABASE_URL` environment variable that is set by Postgres Heroku.

Now you can deploy your app. Bootzooka includes an sbt task which will build the fat-jar and upload it:

````
sbt dist/deployToHeroku
````

After that's done you can visit your application's URL. If anything goes wrong, `heroku logs` will show you your application's output.

## Email

You can configure email either by [providing SMTP server details](config.html), or by using a service provided by Heroku, such as [SendGrid](https://sendgrid.com). In the latter case, you'll need to modify the code a bit to add a new `EmailService`:

# in `build.sbt`, add `"com.sendgrid" % "sendgrid-java" % "2.2.2"` or newer to the `libraryDependencies` of the `backend` project.
# if the `SENDGRID_USERNAME` and `SENDGRID_PASSWORD` environmental variables are present, instantiate the `SendGridEmailService` in `Beans` instead of the smtp one
# implement the `SendGridEmailService`, extending `EmailService`, using SendGrid's API. 