-- USERS
CREATE TABLE "users"(
    "id" UUID NOT NULL,
    "login" VARCHAR NOT NULL,
    "login_lowercase" VARCHAR NOT NULL,
    "email" VARCHAR NOT NULL NOT NULL,
    "password" VARCHAR NOT NULL NOT NULL,
    "salt" VARCHAR NOT NULL NOT NULL,
    "token" VARCHAR NOT NULL
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
