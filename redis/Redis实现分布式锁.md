# Redis实现分布式锁

## 1、执行流程

> 流程图

<img src="https://tobing-markdown.oss-cn-shenzhen.aliyuncs.com/redis_distributed_lock.jpg"  />

> 存在问题

1、需要注意，加锁进程可以会在「加锁到释放锁」之间发生异常情况，这时候如果不做处理，将会一直锁住；为了解决这个问题，考虑为锁设置设置过期时间。

2、需要注意，「加锁到设置过期时间」之间可能也会发生异常，这时设置过期时间则无意义，因此要保证「加锁与设置过期时间」操作具有原子性，Redis提供了`SETNX`可以实现原子性。

3、需要注意，在执行完毕业务的过程中，可能花费的时间很长，当业务执行完毕，锁已经自动删除释放掉了，这时如果再去删除锁就有可能误删了别人的锁。

4、因此在删除锁的时候需要保证生产的是自己持有的锁，即设置的 value 是自己设置的，为了实现这个功能可以使用UUID来实现。

5、设置锁的时候，将UUID设置，删除的时候判断这个值是不是UUID，从而删除这个键值对。

6、但是又会存在一个问题，根据key查询value的时候总会有延时，当我们查到value时，value可能已经改变了，此时再根据key去删除，有可能会出现问题，因此我们还要保证，「判断与删除」具有原子性。

7、Redis 原始没有支持这一点，但是提供了一种解决办法，使用Lua脚本。

8、经过以上步骤可以实现较为可靠的分布式锁。

> 代码实现

```java
// 利用分布式锁，控制高并发下缓存查询问题
public Map<String, List<Catalog2Vo>> getCatalogJsonWithRedisLock() {
    // UUID用于确保删除的key是自己生成的
    String uuid = UUID.randomUUID().toString();
    ValueOperations<String, String> ops = stringRedisTemplate.opsForValue();
    // 原子性设置值和过期时间
    Boolean success = ops.setIfAbsent(CATALOG_JSON_LOCK, uuid, 500, TimeUnit.SECONDS);
    // 获取锁不成功，等待100ms，自旋
    while (!success) {
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        success = ops.setIfAbsent(CATALOG_JSON_LOCK, uuid, 500, TimeUnit.SECONDS);
    }
    // 获取锁成功，执行逻辑，并删除锁【只会有一个线程回去成功】
    String lock = ops.get(CATALOG_JSON_LOCK);
    // 查询数据库
    String getCatalogJson = ops.get("getCatalogJsonWithRedisLock");
    if (StringUtils.isEmpty(getCatalogJson)) {
        Map<String, List<Catalog2Vo>> catalogJsonFromDB = getCatalogJsonFromDB();
        String json = JSON.toJSONString(catalogJsonFromDB);
        stringRedisTemplate.opsForValue().set("getCatalogJsonWithRedisLock", json);
        return catalogJsonFromDB;
    }
    String script = "if redis.call(\"get\",KEYS[1]) == ARGV[1] \n" +
        "then\n" +
        "\treturn redis.call(\"del\",KEYS[1])\n" +
        "else\n" +
        "    return 0\n" +
        "end;";
    // 最后执行Lua脚本，原子性删除锁，成功返回1，失败返回0
    Long res = stringRedisTemplate.execute(new DefaultRedisScript<Long>(script, Long.class), Arrays.asList(CATALOG_JSON_LOCK), lock);
    return JSON.parseObject(getCatalogJson, new TypeReference<Map<String, List<Catalog2Vo>>>() {
    });
}
```



