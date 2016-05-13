package com.kaiscript.cache.core;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
*@author kaiscript
*@date 2016年5月8日 下午6:29:48
*/
public class CacheFactory {
	
	public static final long DEFAULT_MAX_CACHE_SIZE = 1024 * 256;
	
	public static final long DEFAULT_MAX_LIFETIME = 60 * 60 * 6;
	
	private static Map<String,Cache> caches = new ConcurrentHashMap<String, Cache>();
	
	private static CacheStrategy strategy = new DefaultCahceStrategy();
	
	public static long getDefaultMaxCacheSize() {
		return DEFAULT_MAX_CACHE_SIZE;
	}

	public static long getDefaultMaxLifetime() {
		return DEFAULT_MAX_LIFETIME;
	}
	
	public static synchronized <T extends Cache> T createCache(String name){
		T cache = (T) caches.get(name);
		if(cache!=null)
			return cache;
		
		cache = (T) strategy.createCache(name);
		caches.put(name, cache);
		
		return cache;
	}
	
}
