# SCache
Java实现的简单LRU Cache，键为String，值为任意对象类型。可设置缓存的生存时间。容量超0.95时根据LRU策略清理缓存。供个人学习使用，还在完善中......所以如果你觉得代码是错的，其实你是对的

	Cache<String, Object> objectCache = CacheFactory.createCache("example");
	objectCache.setMaxCacheSize(2000);
	objectCache.setMaxLifeTime(1000);
    objectCache.put("11", "22");
    System.out.println("app "+objectCache.get("11"));
    System.out.println("app "+objectCache.get("12"));
    Thread.currentThread().sleep(2000);
    System.out.println(objectCache.get("11"));
    
输出：

	app 22
	app null
	null
