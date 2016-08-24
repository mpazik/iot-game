CREATE TABLE user_registration (
  id         SERIAL PRIMARY KEY NOT NULL,
  nick       TEXT               NOT NULL,
  email      TEXT UNIQUE        NOT NULL,
  created_at TIMESTAMP          NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE user_password (
  id         SERIAL PRIMARY KEY NOT NULL,
  user_id    INT                NOT NULL REFERENCES user_registration (id),
  password   TEXT               NOT NULL,
  created_at TIMESTAMP          NOT NULL DEFAULT CURRENT_TIMESTAMP
);
