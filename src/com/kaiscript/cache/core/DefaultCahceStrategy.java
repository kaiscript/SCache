package com.kaiscript.cache.core;
/**
*@author kaiscript
*@date 2016年5月8日 下午6:28:14
*/
public class DefaultCahceStrategy implements CacheStrategy {

	public Cache createCache(String name) {
		long cacheSize = CacheFactory.getDefaultMaxCacheSize();
		long cacheLeftTime = CacheFactory.getDefaultMaxLifetime();
		return new DefaultCache(name,cacheSize,cacheLeftTime);
	}

	public void destoryCache(Cache cache) {
		
	}
	
}
