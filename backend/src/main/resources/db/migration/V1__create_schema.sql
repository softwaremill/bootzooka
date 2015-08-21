-- USERS
CREATE TABLE "users"(
    "id" UUID NOT NULL,
    "login" VARCHAR NOT NULL,
    "login_lowercase" VARCHAR NOT NULL,
    "email" VARCHAR NOT NULL NOT NULL,
    "password" VARCHAR NOT NULL NOT NULL,
    "salt" VARCHAR NOT NULL NOT NULL,
    "created_on" TIMESTAMP NOT NULL
);
ALTER TABLE "users" ADD CONSTRAINT "users_id" PRIMARY KEY("id");

-- PASSWORD RESET CODES
CREATE TABLE "password_reset_codes"(
  "id" UUID NOT NULL,
  "code" VARCHAR NOT NULL,
  "user_id" UUID NOT NULL,
  "valid_to" TIMESTAMP NOT NULL
);
ALTER TABLE "password_reset_codes" ADD CONSTRAINT "password_reset_codes_id" PRIMARY KEY("id");
ALTER TABLE "password_reset_codes" ADD CONSTRAINT "password_reset_codes_user_fk"
  FOREIGN KEY("user_id") REFERENCES "users"("id") ON DELETE CASCADE ON UPDATE CASCADE;

-- REMEMBER ME TOKENS
CREATE TABLE "remember_me_tokens"(
  "id" UUID NOT NULL,
  "selector" VARCHAR NOT NULL,
  "token_hash" VARCHAR NOT NULL,
  "user_id" UUID NOT NULL,
  "valid_to" TIMESTAMP NOT NULL
);
ALTER TABLE "remember_me_tokens" ADD CONSTRAINT "remember_me_tokens_id" PRIMARY KEY("id");
ALTER TABLE "remember_me_tokens" ADD CONSTRAINT "remember_me_tokens_user_fk"
  FOREIGN KEY("user_id") REFERENCES "users"("id") ON DELETE CASCADE ON UPDATE CASCADE;
CREATE UNIQUE INDEX "remember_me_tokens_selector" ON "remember_me_tokens"("selector");