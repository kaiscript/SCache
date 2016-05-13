package com.kaiscript.cache;

import com.kaiscript.cache.core.Cache;
import com.kaiscript.cache.core.CacheFactory;

/**
 * Hello world!
 *
 */
public class App 
{
    public static void main( String[] args ) throws Exception{
    	
    	Cache<String, Object> objectCache = CacheFactory.createCache("objects");
    	objectCache.setMaxLifeTime(1000);
    	objectCache.put("11", "22");
    	System.out.println("app "+objectCache.get("11"));
    	System.out.println("------");
    	System.out.println("app "+objectCache.get("12"));
    	System.out.println("------");
    	System.out.println(objectCache.get("11"));
    	
    }
}
