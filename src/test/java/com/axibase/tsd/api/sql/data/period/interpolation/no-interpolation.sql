SELECT entity, date_format(PERIOD(5 minute)), COUNT(value)
  FROM cpu_busy
WHERE datetime >= '2016-06-03T09:20:00.000Z' AND datetime < '2016-06-03T09:50:00.000Z'
  AND entity = 'nurswgvml006'
GROUP BY entity, PERIOD(5 minute)
