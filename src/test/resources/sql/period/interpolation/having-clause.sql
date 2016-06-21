SELECT entity, date_format(PERIOD(1 minute)), COUNT(value)
  FROM 'sql-period-interpolation-metric'
WHERE datetime >= '2016-06-03T09:10:00.000Z' AND datetime < '2016-06-03T09:16:00.000Z'
  AND entity = 'sql-period-interpolation-entity'
GROUP BY entity, PERIOD(1 minute, VALUE 0) HAVING COUNT(value) > 2