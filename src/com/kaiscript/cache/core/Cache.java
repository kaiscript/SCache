package com.kaiscript.cache.core;
/**
*@author kaiscript
*@date 2016年5月8日 下午6:03:54
*/
public interface Cache<K,V> extends java.util.Map<K,V>{
	
	String getName();
	
	void setName(String name);
	
	long getMaxCacheSize();
	
	void setMaxCacheSize(long maxCacheSize);
	
	void setMaxLifeTime(long maxLifetime);
	
	long getMaxLeftTime();
	
	long getCacheSize();
	
	long getCacheMisses();
	
	long getCacheHits();
}
