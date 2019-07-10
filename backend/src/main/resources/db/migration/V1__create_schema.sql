-- USERS
CREATE TABLE "users"
(
  "id"              TEXT        NOT NULL,
  "login"           TEXT        NOT NULL,
  "login_lowercase" TEXT        NOT NULL,
  "email_lowercase" TEXT        NOT NULL,
  "password"        TEXT        NOT NULL,
  "created_on"      TIMESTAMPTZ NOT NULL
);
ALTER TABLE "users"
  ADD CONSTRAINT "users_id" PRIMARY KEY ("id");
CREATE UNIQUE INDEX "users_login_lowercase" ON "users" ("login_lowercase");
CREATE UNIQUE INDEX "users_email_lowercase" ON "users" ("email_lowercase");

-- API KEYS
CREATE TABLE "api_keys"
(
  "id"          TEXT        NOT NULL,
  "user_id"     TEXT        NOT NULL,
  "created_on"  TIMESTAMPTZ NOT NULL,
  "valid_until" TIMESTAMPTZ NOT NULL
);
ALTER TABLE "api_keys"
  ADD CONSTRAINT "api_keys_id" PRIMARY KEY ("id");
ALTER TABLE "api_keys"
  ADD CONSTRAINT "api_keys_user_id_fk" FOREIGN KEY ("user_id") REFERENCES "users" ("id") ON DELETE CASCADE ON UPDATE CASCADE;

-- PASSWORD RESET CODES
CREATE TABLE "password_reset_codes"
(
  "id"          TEXT        NOT NULL,
  "user_id"     TEXT        NOT NULL,
  "valid_until" TIMESTAMPTZ NOT NULL
);
ALTER TABLE "password_reset_codes"
  ADD CONSTRAINT "password_reset_codes_id" PRIMARY KEY ("id");
ALTER TABLE "password_reset_codes"
  ADD CONSTRAINT "password_reset_codes_user_fk"
    FOREIGN KEY ("user_id") REFERENCES "users" ("id") ON DELETE CASCADE ON UPDATE CASCADE;

-- EMAILS
CREATE TABLE "scheduled_emails"
(
  "id"        TEXT NOT NULL,
  "recipient" TEXT NOT NULL,
  "subject"   TEXT NOT NULL,
  "content"   TEXT NOT NULL
);
ALTER TABLE "scheduled_emails"
  ADD CONSTRAINT "scheduled_emails_id" PRIMARY KEY ("id");
