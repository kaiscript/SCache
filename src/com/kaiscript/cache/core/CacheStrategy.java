package com.kaiscript.cache.core;
/**
*@author kaiscript
*@date 2016年5月8日 下午6:25:07
*/
public interface CacheStrategy {
	
	Cache createCache(String name);
	
	void destoryCache(Cache cache);
	
}
