/**
 * Taken from the IBM redbook sample
 */
package com.prudential.commons.cache;
import java.util.logging.Logger;

/**
 * 
 * TheCacheEntry a wrapper for a simple cache entry
 */
public class TheCacheEntry
{
   /**
    * Logger for the class
    */
   private static final Logger theLog = Logger.getLogger(TheCacheEntry.class.getName());

   /**
    * the object in the cache
    */
   private Object cacheEntry;
   
   /**
    * the expiration time
    */
   private long cacheExpireTime;
   
   /**
    * 
    * Constructor
    *
    * @param objectToCache the object to cache
    * @param cacheExpire the time to cache it for
    */
   TheCacheEntry(Object objectToCache, long cacheExpire)
   {
      cacheEntry = objectToCache;
      cacheExpireTime = cacheExpire;
   }
   
   /**
    * 
    * getCacheEntry returns the object stored in the cache
    *
    * @return the cached object
    */
   public Object getCacheEntry()
   {
      return cacheEntry;
   }

   /**
    * 
    * setCacheEntry sets the item to store in the cache
    *
    * @param p_cacheEntry the item to cache
    */
   public void setCacheEntry(Object p_cacheEntry)
   {
      cacheEntry = p_cacheEntry;
   }

   /**
    * 
    * getCacheExpireTime returns the cache expiration
    *
    * @return the cache expiration
    */
   public long getCacheExpireTime()
   {
      return cacheExpireTime;
   }

   /**
    * 
    * setCacheExpireTime sets the cache expiration time
    *
    * @param p_cacheExpireTime the time to set as cache expiration time
    */
   public void setCacheExpireTime(int p_cacheExpireTime)
   {
      cacheExpireTime = p_cacheExpireTime;
   }

   /**
    * override toString
    */
   public String toString()
   {
      String returnString = "";
      returnString = "TheCacheEntry cached::"+cacheEntry+":: expireTime::"+cacheExpireTime;
      return returnString;
   }
}

