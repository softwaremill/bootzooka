![Bootzooka](https://github.com/softwaremill/bootzooka/raw/master/banner.png)

[See the docs](http://softwaremill.github.io/bootzooka/) for more information.

[![ CI ](https://github.com/softwaremill/bootzooka/workflows/Bootzooka%20CI/badge.svg)](https://github.com/softwaremill/bootzooka/actions?query=workflow%3A%22Bootzooka+CI%22)

## Quick start

### Using Docker compose

The fastest way to experiment with Bootzooka is using the provided Docker compose setup. It starts three images: 
Bootzooka itself (either locally built or downloaded), PostgreSQL server and Graphana LGTM for observability.

### Backend: PostgreSQL & API

To run Bootzooka's backend locally, you'll still need a running instance of PostgreSQL with a `bootzooka` database. 
You can spin up one easily using docker:

```sh
# use "bootzooka" as a password
docker run --name bootzooka-postgres -p 5432:5432 -e POSTGRES_PASSWORD=bootzooka -e POSTGRES_DB=bootzooka -d postgres
```

Then, you can start the backend:

```sh
export SQL_PASSWORD=bootzooka
./backend-start.sh
```

### Frontend: Yarn & webapp

You will need the [yarn package manager](https://yarnpkg.com) to run the UI. Install it using your package manager or:

```sh
curl -o- -L https://yarnpkg.com/install.sh | bash
```

Then, you can start the frontend:

```sh
./frontend-start.sh
```

And open `http://localhost:3000`.

## Commercial Support

We offer commercial support for Bootzooka and related technologies, as well as development services. [Contact us](https://softwaremill.com) to learn more about our offer!

## Copyright

Copyright (C) 2013-2024 SoftwareMill [https://softwaremill.com](https://softwaremill.com).
