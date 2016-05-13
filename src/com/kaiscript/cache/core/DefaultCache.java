package com.kaiscript.cache.core;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;

/**
*@author kaiscript
*@date 2016年5月8日 下午6:39:26
*/
public class DefaultCache<K,V> implements Cache<K,V> {
	
	protected Map<K,CacheObject<V>> map;
	
	protected LinkedList<K> lastAccessList;
	
	protected LinkedList<K> ageList;
	
	private String name;
	
	private long cacheSize;
	
	private long maxCacheSize;
	
	private long maxLifetime;
	
	private long cacheMisses;
	
	private long cacheHits;
	
	public DefaultCache(String name,long cacheSize,long maxLifetime) {
		this.name = name;
		this.maxCacheSize = cacheSize;
		this.maxLifetime = maxLifetime;
		map = new HashMap<K,CacheObject<V>>(100); //默认容量设100
		lastAccessList = new LinkedList<K>();
		ageList = new LinkedList<K>();
		
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public long getMaxCacheSize() {
		return maxCacheSize;
	}


	public void setMaxCacheSize(long maxCacheSize) {
		this.maxCacheSize = maxCacheSize;
	}

	public void setMaxLifeTime(long maxLifetime) {
		this.maxLifetime = maxLifetime;
	}
	
	public long getMaxLeftTime() {
		return maxLifetime;
	}

	public long getCacheSize() {
		return cacheSize;
	}

	public long getCacheMisses() {
		return cacheMisses;
	}

	public long getCacheHits() {
		return cacheHits;
	}

	public void clear() {
		map.clear();
		ageList.clear();
		lastAccessList.clear();
		
	}

	public boolean containsKey(Object key) {
		deleteExpiredEntries();//清除过期缓存再返回是否包含key
		return map.containsKey(key);
	}

	public boolean containsValue(Object value) {
		deleteExpiredEntries();
		Iterator i = values().iterator();
		if(value==null){
			return containsNullValue();
		}
		while(i.hasNext()){
			if(i.next().equals(value)){
				return true;
			}
		}
		
		return false;
	}
	
	/**
	 * 包含空值
	 * @return
	 */
	private boolean containsNullValue(){
		Iterator i = values().iterator();
		while(i.hasNext()){
			if(i.next()==null)
				return true;
		}
		return false;
	}
	
	public Set<Entry<K, V>> entrySet() {
		deleteExpiredEntries();
		synchronized (this) {
			final Map<K,V> result = new HashMap<K,V>();
			for(final Entry<K,CacheObject<V>> entry:map.entrySet()){
				result.put(entry.getKey(), entry.getValue().object);
				
			}
			return result.entrySet();
		}
	}

	public synchronized V get(Object key) {
		deleteExpiredEntries();
		CacheObject<V> cacheObject = map.get(key);
		if(cacheObject==null){
			cacheMisses++; //拿不到，miss次数增加
			return null;
				
		}
		cacheHits++;
		return cacheObject.object;
	}

	public boolean isEmpty() {
		deleteExpiredEntries();
		return map.isEmpty();
	}

	public Set<K> keySet() {
		deleteExpiredEntries();
		return map.keySet();
	}

	public synchronized V put(K key, V value) {
		V result = remove(key); //如果相同key，有旧的cacheObject则删除
		long timestamp = System.currentTimeMillis();//当前的时间错
		CacheObject<V> cacheObject = new CacheObject<V>(value,timestamp);
		map.put(key, cacheObject); //添加同样的key的cacheObject
		ageList.add(key);
		lastAccessList.add(key);
		cacheSize++;
		ensureCacheCapacity();
		return value;
	}

	/* 
	 * 把参数map m全部添加到 DefaultCache
	 */
	public void putAll(Map<? extends K, ? extends V> m) {
		for(Iterator<? extends K> i = m.keySet().iterator();i.hasNext();){
			K k = i.next();
			V v = m.get(k);
			put(k,v);
		}
	}

	public synchronized V remove(Object key) {
		CacheObject<V> cacheObject = map.get(key);
		if(cacheObject==null){
			return null;
		}
		map.remove(key);
		ageList.remove(key);
		lastAccessList.remove(key);
		cacheSize--;  //移除key，cache的size减1
		
		return cacheObject.object;
	}

	
	public int size() {
		return (int) cacheSize;
	}

	/**
	 * 值的集合 
	 */
	public Collection<V> values() {
		deleteExpiredEntries();
		return new DefaultCache.CacheObjectCollection(map.values());
	}	
	
	/**
	 * 当cache容量超过百分之95，清理LinkedList尾部
	 */
	private void ensureCacheCapacity() {
		int desiredSize = (int) (maxCacheSize * 0.95);
		if(getCacheSize()>desiredSize){
			deleteExpiredEntries();
			desiredSize = (int) (maxCacheSize * 0.9);
			if(getCacheSize()>desiredSize){
				deleteLastEntries();
				System.out.println("shrinked to 90%.");
			}
			
		}
		
	}

	/**
	 * 删除排在LinkedList尾部的元素
	 */
	private void deleteLastEntries() {
		K k = lastAccessList.getLast();
		int desireSize = (int) (maxCacheSize * 0.9);
		while(getCacheSize()>desireSize){
			remove(k);
			k = lastAccessList.removeLast();
		}
		
	}

	/**
	 * 删除过期的的元素
	 */
	private synchronized void deleteExpiredEntries() {
		if(maxLifetime<=0)
			return;
		K k = ageList.getLast();
		if(k==null)
			return;
		long expireTime = System.currentTimeMillis()-maxLifetime;
		CacheObject<V> cacheObject = map.get(k);
		while(expireTime>cacheObject.timestamp){ //过期
			remove(k);//移除k这个key
			if(ageList.size()==0)  //ageList为0 则跳出
				break;
		
			k = ageList.getLast(); //获取下个key k
			System.out.println(k);
			
			cacheObject = map.get(k);
			if(cacheObject==null)
				return;
		}
		
	}
	
	
	/**
	 * 缓存对象V，包含被缓存的具体对象、时间戳
	 * @author kaiscript
	 * @date 2016年5月8日 下午7:51:57 
	 * @param <V>
	 */
	private static class CacheObject<V>{
		
		V object; //被缓存的具体对象
		
		long timestamp;
		
		public CacheObject(V object,long timestamp){
			this.object = object;
			this.timestamp = timestamp;
		}
		
	}
	
	
	/**
	 * CacheObject的集合
	 * @author kaiscript
	 * @date 2016年5月12日 下午9:48:55 
	 * @param <V>
	 */
	private class CacheObjectCollection<V> implements Collection<V>{
		
		private List<CacheObject<V>> cacheObjects;
		
		private CacheObjectCollection(Collection<CacheObject<V>> cacheObjects){
			cacheObjects = new ArrayList<CacheObject<V>>(cacheObjects);
		}
		
		public boolean contains(Object o) {
			Iterator it = iterator();
			while(it.hasNext()){
				if(o.equals(it.next()))
					return true;
			}
			return false;
		}

		public boolean containsAll(Collection<?> c) {
			Iterator it = c.iterator();
			while(it.hasNext()){
				if(!contains(it.next()))
					return true;
			}
			return false;
		}

		public boolean isEmpty() {
			return size()==0;
		}

		public Iterator<V> iterator() {
			return new Iterator<V>() {
				
				private Iterator<CacheObject<V>> it = cacheObjects.iterator();
				
				public boolean hasNext() {
					return it.hasNext();
				}

				public V next() {
					if(it.hasNext()){
						DefaultCache.CacheObject<V> object = it.next();
						if(object==null)
							return null;
						else
							return object.object;
					}
					else
						throw new NoSuchElementException();
				}
				
				public void remove() {
                    throw new UnsupportedOperationException();
                }
			};
		}

		public int size() {
			return cacheObjects.size();
		}

		public Object[] toArray() {
			Iterator it = iterator();
			Object[] array = new Object[size()];
			int i= 0;
			while(it.hasNext()){
				array[i++] = it.next();
			}
			return array;
		}

		public <V> V[] toArray(V[] array) {
			Iterator<V> it = (Iterator<V>) iterator();
			int i = 0;
			while(it.hasNext()){
				array[i++] = it.next();
			}
			return array;
		}
		
		public boolean add(V o) {
            throw new UnsupportedOperationException();
        }

        public boolean remove(Object o) {
            throw new UnsupportedOperationException();
        }

        public boolean addAll(Collection<? extends V> coll) {
            throw new UnsupportedOperationException();
        }

        public boolean removeAll(Collection<?> coll) {
            throw new UnsupportedOperationException();
        }

        public boolean retainAll(Collection<?> coll) {
            throw new UnsupportedOperationException();
        }

        public void clear() {
            throw new UnsupportedOperationException();
        }
	}
	
}
