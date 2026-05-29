package com.softwaremill.bootzooka.infrastructure

import ma.chinespirit.parlance.{DbTx, Postgres}

/** A transaction context for the application's single (Postgres) database. Use as a `(using Tx)` parameter on methods that must run within a
  * transaction; an instance is provided by [[DB.transact]] / [[DB.transactEither]]. Centralises the choice of database type so call sites
  * don't repeat `DbTx[Postgres]`.
  */
type Tx = DbTx[Postgres]
