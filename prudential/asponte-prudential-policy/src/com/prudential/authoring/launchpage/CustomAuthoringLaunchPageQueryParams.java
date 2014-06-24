/********************************************************************/
/* Asponte
/* cmknight
/********************************************************************/

package com.prudential.authoring.launchpage;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.ibm.workplace.wcm.api.Library;
import com.ibm.workplace.wcm.api.query.Selector;
public class CustomAuthoringLaunchPageQueryParams {

   /** Logger for the class */
   private static Logger s_log = Logger.getLogger(CustomAuthoringLaunchPageQueryParams.class.getName());
   
   /** Order by last modified date */
   public static final int ORDER_BY_MODIFIED = 1;
   
   /** Order by creation date */
   public static final int ORDER_BY_CREATED = 2;
   
  /** Order by title (alphabetically) */
   public static final int ORDER_BY_TITLE = 3;
   
   /** The index of the first page to get */
   private int m_firstPage = 1;

   /** The default index of the  first page to get */
   public static final int DEFAULT_FIRST_PAGE = 1;

   /** The page size */
   private int m_pageSize = 25;

   /** The default page size */
   public static final int DEFAULT_PAGE_SIZE = 25;
   
   /** Libraries that the items in the query must be in */
   private List<Library> m_libraries;
   
   public void setLibraries(List<Library> p_libraries) {
      m_libraries = p_libraries;
   }

   /** Title Sort reverse (default to false - A to Z) */
   private boolean m_titleSortReverse = false;

   /** Title Sort Active (default false) */
   private boolean m_titleSortActive;
   
   public void setTitleSortActive(boolean p_titleSortActive) {
      m_titleSortActive = p_titleSortActive;
   }

   /** Created date sort reverse (default to true - most recent first) */
   private boolean m_createdSortReverse = true;

   /** Created date sort active (default false) */
   private boolean m_createdSortActive;

   /** Last modified date sort reverse (default to true - most recent first) */
   private boolean m_modifiedSortReverse = true;

   /** Last modified sort active (default false) */
   private boolean m_modifiedSortActive = false;

   public void setModifiedSortActive(boolean p_modifiedSortActive) {
      m_modifiedSortActive = p_modifiedSortActive;
   }

   public List<Library> getLibraries() {
      return m_libraries;      
   }
   
   /**
    * Get whether to sort by title in reverse
    * @return boolean
    */
   public boolean getTitleSortReverse()
   {
      return m_titleSortReverse;
   }

   /**
    * Get whether the title sort is active
    * @return boolean
    */
   public boolean getTitleSortActive()
   {
      return m_titleSortActive;
   }

   /**
    * Get whether to sort by created date in reverse
    * @return boolean
    */
   public boolean getCreatedSortReverse()
   {
      return m_createdSortReverse;
   }

   /**
    * Get whether the created date sort is active
    * @return boolean
    */
   public boolean getCreatedSortActive()
   {
      return m_createdSortActive;
   }

   /**
    * Get whether to sort by last modified date in reverse
    * @return boolean
    */
   public boolean getModifiedSortReverse()
   {
      return m_modifiedSortReverse;
   }

   /**
    * Get whether the last modified date sort is active
    * @return boolean
    */
   public boolean getModifiedSortActive()
   {
      return m_modifiedSortActive;
   }
   
   private static Selector s_classesSelector;

   public static Selector getClassesSelector() {
      return s_classesSelector;
   }

   public static void setClassesSelector(Selector p_classesSelector) {
      s_classesSelector = p_classesSelector;
   }
   
   /**
    * Get the first page index
    * @return first page index
    */
   public int getFirstPage()
   {
      return m_firstPage;
   }

   /**
    * Get the page size
    * @return page size
    */
   public int getPageSize()
   {
      return m_pageSize;
   }
   
}

