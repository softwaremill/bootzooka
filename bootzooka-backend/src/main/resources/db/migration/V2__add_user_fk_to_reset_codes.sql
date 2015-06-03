ALTER TABLE "password_reset_codes" ADD CONSTRAINT "password_reset_codes_user_fk"
  FOREIGN KEY("user_id") REFERENCES "users"("id") ON DELETE CASCADE ON UPDATE CASCADE;