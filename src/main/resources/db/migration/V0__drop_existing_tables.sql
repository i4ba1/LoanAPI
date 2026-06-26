-- Dev-only cleanup: remove tables previously created by ddl-auto=update.
-- Safe to keep in version history; DROP IF EXISTS is idempotent.
DROP TABLE IF EXISTS idempotency_keys;
DROP TABLE IF EXISTS loans;
