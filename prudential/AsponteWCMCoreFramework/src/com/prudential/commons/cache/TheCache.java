/**
 * Taken from the IBM redbook sample
 */
package com.prudential.commons.cache;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * 
 * SimpleCacheImpl a Simple cache
 */
public class TheCache
{
   private static final Logger s_log = Logger.getLogger(TheCache.class.getName());
   private static long defaultExpirationMinutes = 30;
   private static TheCache s_theCacheImpl;
   
   private static Map<String, TheCacheEntry> s_simpleCacheEntries = new HashMap<String, TheCacheEntry>();
   
   /**
    * 
    * get retrieve the cache object from the cache
    * Will return null if the item is expired, and will remove the
    * item from the cache if its expired
    *
    * @param keyValue the key of the item to retrieve
    * @return
    */
   public TheCacheEntry get(String keyValue)
   {
	  TheCacheEntry returnObject = null;
      returnObject = s_simpleCacheEntries.get(keyValue);
      // return null if its expired
      if(returnObject != null && isExpired(returnObject))
      {
         remove(keyValue);
         returnObject = null;
      }
      if(s_log.isLoggable(Level.FINEST))
      {
         s_log.log(Level.FINEST, "get called with key "+keyValue);
         if(returnObject != null)
         {
            s_log.log(Level.FINEST,"get returning "+returnObject);
         }
         else
         {
            s_log.log(Level.FINEST, "returning null");
         }
         
      }
      return returnObject;
   }
   
   /**
    * 
    * remove remove the cache entry based on the key
    *
    * @param keyValue the key for the cache entry to be removed.
    */
   public void remove(String keyValue)
   {
      s_simpleCacheEntries.remove(keyValue);
   }
   
   /**
    * 
    * put add an item to the cache
    *
    * @param keyValue the key value for the cache item
    * @param cacheObject the object to add to the cache
    */
   public void put(String keyValue, Object cacheObject)
   {
      long expireTime = System.currentTimeMillis()+defaultExpirationMinutes * 60 * 1000;
      put(keyValue, cacheObject, expireTime);
   }
   
   /**
    * 
    * put add an item to the cache
    *
    * @param keyValue the key value for the cache item
    * @param cacheObject the object to add to the cache
    * @param expireTime the expiration of the object
    */
   public void put(String keyValue, Object cacheObject, long expireTime)
   {      
	   TheCacheEntry cachedObject = new TheCacheEntry(cacheObject, expireTime);
      s_simpleCacheEntries.put(keyValue, cachedObject);
      if(s_log.isLoggable(Level.FINEST))
      {
         s_log.log(Level.FINEST, "put called with key "+keyValue+" cacheObject "+cacheObject+" expireTime "+expireTime);
      }
   }
   
   /**
    * 
    * isExpired whether or not the cache item is expired
    *
    * @param theObject the cache item to check
    * @return boolean whether expired
    */
   public boolean isExpired(TheCacheEntry theObject)
   {
      boolean isExpiredReturn = false;
      if(theObject != null)
      {
         isExpiredReturn= System.currentTimeMillis()>theObject.getCacheExpireTime();
      }            
      if(s_log.isLoggable(Level.FINEST))
      {
         s_log.log(Level.FINEST, "isExpired "+isExpiredReturn);
      }
      return isExpiredReturn;
   }
   
   static public TheCache getInstance()
   {
      if(s_theCacheImpl == null)
      {
         s_theCacheImpl = new TheCache();
      }
      return s_theCacheImpl;      
   }
   
   public void clearall() {
      boolean isDebug = s_log.isLoggable(Level.FINEST);
      if (isDebug) {
         s_log.entering("TheCache", "clearall");
      }
      
      s_simpleCacheEntries.clear();
      
      if (isDebug) {
         s_log.exiting("TheCache", "clearall");
      }
      
   }
}
