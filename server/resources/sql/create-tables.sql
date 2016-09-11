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

CREATE TABLE arbiter_event (
  id         SERIAL PRIMARY KEY NOT NULL,
  type       TEXT               NOT NULL,
  data       TEXT               NOT NULL,
  created_at TIMESTAMP          NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE scenario_event (
  id          SERIAL PRIMARY KEY NOT NULL,
  scenario_id SERIAL             NOT NULL,
  type        TEXT               NOT NULL,
  data        TEXT               NOT NULL,
  created_at  TIMESTAMP          NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE chat_command (
  id         SERIAL PRIMARY KEY NOT NULL,
  user_id    INT REFERENCES user_registration (id),
  command    TEXT               NOT NULL,
  created_at TIMESTAMP          NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE instance_event (
  id           SERIAL PRIMARY KEY NOT NULL,
  instance_key TEXT               NOT NULL,
  type         TEXT               NOT NULL,
  data         TEXT               NOT NULL,
  created_at   TIMESTAMP          NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE achievement_event (
  id              SERIAL PRIMARY KEY NOT NULL,
  user_id         INT                NOT NULL REFERENCES user_registration (id),
  achievement_key TEXT               NOT NULL,
  type            TEXT               NOT NULL,
  created_at      TIMESTAMP          NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE friendship (
  id         SERIAL PRIMARY KEY NOT NULL,
  user_id1   INT                NOT NULL REFERENCES user_registration (id),
  user_id2   INT                NOT NULL REFERENCES user_registration (id),
  created_at TIMESTAMP          NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE analytics_event (
  id         SERIAL PRIMARY KEY NOT NULL,
  user_id    INT                NOT NULL REFERENCES user_registration (id),
  type       TEXT               NOT NULL,
  created_at TIMESTAMP          NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE analytics_event_data (
  id         SERIAL PRIMARY KEY NOT NULL,
  user_id    INT                NOT NULL REFERENCES user_registration (id),
  type       TEXT               NOT NULL,
  data       TEXT               NOT NULL,
  created_at TIMESTAMP          NOT NULL DEFAULT CURRENT_TIMESTAMP
);