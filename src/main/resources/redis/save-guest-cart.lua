local current = redis.call('HGET', KEYS[1], 'version')
if ARGV[1] == '-1' then
  if current then return 0 end
else
  if not current or current ~= ARGV[1] then return 0 end
end
redis.call('HSET', KEYS[1], 'version', ARGV[2], 'payload', ARGV[3])
redis.call('EXPIRE', KEYS[1], ARGV[4])
return 1