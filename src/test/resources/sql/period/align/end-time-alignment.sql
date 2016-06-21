SELECT entity, date_format(PERIOD(5 minute)), COUNT(value)
  FROM 'sql-period-align-metric'
WHERE datetime >= '2016-06-03T09:20:00.123Z' AND datetime < '2016-06-03T09:50:00.321Z'
  AND entity = 'sql-period-align-entity'
GROUP BY entity, PERIOD(5 minute, NONE, END_TIME)