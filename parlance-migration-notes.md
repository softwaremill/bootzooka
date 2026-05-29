# Magnum → parlance migration notes

Scratch note for the multi-step migration on branch `migrate-magnum-to-parlance`.
Produced by the dependency-swap task; consumed by later code-migration tasks.

## Dependency coordinates (confirmed)

- **sbt:** `"ma.chinespirit" %% "parlance" % "0.1.0"` (replaces `"com.augustnagro" %% "magnum" % "1.3.1"`)
- Resolved artifact: `ma/chinespirit/parlance_3/0.1.0/parlance_3-0.1.0.jar` from Maven Central (`sbt backend/update` succeeds).
- **Scala 3.8.3:** parlance publishes a single Scala-3 artifact `parlance_3` (built with 3.8.2 per its `build.sbt`; no `crossScalaVersions`). Scala 3 is binary/TASTy-forward-compatible, so the `_3` artifact resolves and compiles under this project's 3.8.3. There is no separate 3.8.3 cross-build — the `_3` artifact is the cross-build.
- Source/package: `ma.chinespirit.parlance` (GitHub `lbialy/parlance`, group domain `chinespirit.ma`). Other modules exist but are **not** needed: `parlance-pg`, `parlance-migrate`.

## Logging (build.sbt review done)

parlance's `SqlLogger` logs via **`java.lang.System.Logger`** (platform logging), **not** JUL — same mechanism Magnum used. It is routed to SLF4J by `slf4j-jdk-platform-logging` (build.sbt line ~52), **not** by `jul-to-slf4j` (line ~50). The old line-50 comment claiming Magnum "uses JUL" was inaccurate; updated so line 50 mentions only OTEL and line 52 notes parlance.

## IMPORTANT: parlance is NOT a drop-in Magnum fork

parlance 0.1.0 is a redesigned, Active-Record-inspired ORM (92 core source files vs Magnum's handful). The low-level names overlap, but several APIs the migration assumed differ. The code migration is **substantial**, not a package rename.

## API mapping

| Magnum (current) | parlance | Notes |
|---|---|---|
| `import com.augustnagro.magnum.*` | `import ma.chinespirit.parlance.*` | package rename |
| `DbCodec`, `.biMap(to, from)` | same | ✓ identical |
| `DbCodec.StringCodec` | `DbCodec.StringCodec` (also `DbCodec[String]`) | ✓ `StringCodec` is a given in `object DbCodec`; `DbCodec.StringCodec.biMap(...)` compiles as-is |
| `summon[DbCodec[OffsetDateTime]]` | same | ✓ `OffsetDateTimeCodec` given exists |
| custom `given DbCodec[Instant]` (in `infrastructure/Codecs.scala`) | **keep** | parlance ships `given InstantCodec: DbCodec[Instant]` in `object DbCodec`, but an *imported* given (via `import Codecs.given`) takes precedence over implicit/companion scope — **verified: no ambiguity**. (Corrects the earlier note that said to remove it.) |
| `Transactor(dataSource = ds, sqlLogger = ...)` | `Transactor[Postgres](Postgres, ds, SqlLogger.logSlowQueries(200.millis))` | **DB type is a required first arg** AND must be **pinned** as `[Postgres]` — `Transactor(Postgres, ...)` infers the singleton `Postgres.type`, which then mismatches `DbTx[Postgres]` everywhere. |
| `SqlLogger.logSlowQueries(200.millis)` | same | ✓ |
| top-level `transact(transactor)(f)` | **`transactor.transact(f)`** (instance method) | `def transact[T](f: DbTx[D] ?=> T): T`. Rolls back only on a thrown exception (then rethrows) — so the `LeftException` rollback-on-`Left` trick still works unchanged. |
| top-level `connect(ds)(f)` | **`transactor.connect(f)`** (instance method) | `connect` takes no DataSource — needs a `Transactor`; `def connect[T](f: DbCon[D] ?=> T): T`. DB.scala `testConnection(ds)` builds a throwaway `Transactor[Postgres](Postgres, ds)` to run the test query. |
| `(using DbTx)`, `DbTx ?=> T` | **`(using DbTx[Postgres])`, `DbTx[Postgres] ?=> T`** | `DbCon`/`DbTx` are parameterized by DB type. Touches *every* model/service/api file with a `DbTx` param (UserModel, UserService, ApiKeyModel, ApiKeyService, Auth, PasswordResetCodeModel, PasswordResetService, EmailModel, EmailService, the `*Api` files, `AuthTokenOps`). Read-only sites may use `DbCon[Postgres]`. |
| `sql"..."` interpolator | same | ✓ available via wildcard import; if selectively imported, confirm the `sql` name is importable |
| `.query[T].run()`, `.update.run()` | same | ✓ require a `DbCon`/`DbTx` in scope |
| `Frag` | same | ✓ exists |
| `Repo[EC, E, ID]` (no parens) | `Repo[EC, E, ID]()` | parlance `Repo` is an `open class` with default ctor args — needs `()` |
| `userRepo.insert(e)` | **`userRepo.rawInsert(e)`** (or `create`) | no `insert` method; `rawInsert(ec)` inserts, `create(ec): E` inserts & returns the entity |
| `findById`, `findAll`, `count`, `deleteById`, `deleteAllById` | same | ✓ (read ops on `ImmutableRepo[E, ID]`) |
| `TableInfo[EC, E, ID]` + `u.colName` + `sql"$u"` | `TableInfo[EC, E, ID]` | exists; exact column-ref / table-interpolation API **unverified — confirm during code migration** |
| `Spec[E].where(sql"...")` / `.limit(n)` | **`QueryBuilder.from[E].where(...).limit(n).run()`** | **`Spec` does not exist.** `.where` idiomatically takes a typed lambda (`_.field > x`); whether it accepts a raw `WhereFrag`/`sql"..."` is **unverified**. Affects `UserModel.findBy*` and `EmailModel.find(limit)`. |
| entity = plain `case class` + `@Table` | `case class ... derives EntityMeta` | **entities must add `derives EntityMeta`** (User, ApiKey, PasswordResetCode, ScheduledEmails). Creator types (if EC≠E) use `derives DbCodec`. |
| `@Table(PostgresDbType, SqlNameMapper.CamelToSnakeCase)` | **`@Table(SqlNameMapper.CamelToSnakeCase)`** | `@Table` takes only a `nameMapper`; the DB type moved to the `Transactor` type param |
| `PostgresDbType` | **`Postgres`** (`object Postgres extends Postgres`, in `DatabaseType.scala`) | used as the `Transactor` arg / `DbTx[Postgres]` type, no longer in `@Table` |
| `@SqlName("...")` | same | ✓ |
| `SqlNameMapper.CamelToSnakeCase` | same | ✓ |

## Open items for code-migration tasks

1. `Spec` → `QueryBuilder.from[E]`: verify whether `.where` accepts raw `sql`/`WhereFrag` or only typed lambdas; rewrite `UserModel.findByEmail/findByLogin/findByLoginOrEmail` and `EmailModel.find` accordingly.
2. `TableInfo` column-reference + `sql"$u"` table interpolation: confirm the API matches Magnum's usage in `UserModel`/`ApiKeyModel`.
3. Add `derives EntityMeta` to all entities; drop the DB-type arg from every `@Table`.
4. Parameterize all `DbTx`/`DbCon` usages with `[Postgres]`.
5. ~~Rework `DB.scala`~~ **DONE** (and `infrastructure/Magnum.scala` → `infrastructure/Codecs.scala`, with the four import sites updated).
6. ~~Remove the custom `given DbCodec[Instant]`~~ — keep it; `DbCodec.StringCodec` works as-is (see table).
7. `insert` → `rawInsert`; add `()` to `Repo[...]` constructions.
