SELECT
  a.data,
  count(*) AS count
FROM (SELECT DISTINCT
        user_id,
        data
      FROM analytics_event_data
      WHERE type = 'quest.complete') AS a
GROUP BY a.data
ORDER BY count