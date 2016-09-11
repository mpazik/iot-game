-- noinspection SqlResolveForFile
ALTER TABLE table1
  ALTER COLUMN col1 TYPE JSON USING col1 :: JSON;