/********************************************************************/
/* Asponte                     Some more text  extra
/* cmknight9
/********************************************************************/

package com.prudential.commons.wcm.authoring;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import com.ibm.portal.ListModel;
import com.ibm.workplace.wcm.api.Content;
import com.ibm.workplace.wcm.api.Document;
import com.ibm.workplace.wcm.api.DocumentId;
import com.ibm.workplace.wcm.api.DocumentIdIterator;
import com.ibm.workplace.wcm.api.DocumentLibrary;
import com.ibm.workplace.wcm.api.DocumentTypes;
import com.ibm.workplace.wcm.api.RichTextComponent;
import com.ibm.workplace.wcm.api.Taxonomy;
import com.ibm.workplace.wcm.api.TextComponent;
import com.ibm.workplace.wcm.api.Workspace;
import com.ibm.workplace.wcm.api.exceptions.AuthorizationException;
import com.ibm.workplace.wcm.api.exceptions.ComponentNotFoundException;
import com.ibm.workplace.wcm.api.exceptions.DocumentIdCreationException;
import com.ibm.workplace.wcm.api.exceptions.DocumentRetrievalException;
import com.ibm.workplace.wcm.api.exceptions.IllegalTypeChangeException;
import com.ibm.workplace.wcm.api.exceptions.WCMException;
import com.ibm.workplace.wcm.api.extensions.authoring.ActionResult;
import com.ibm.workplace.wcm.api.extensions.authoring.AuthoringAction;
import com.ibm.workplace.wcm.api.extensions.authoring.FormContext;
import com.prudential.commons.cache.TheCache;
import com.prudential.commons.cache.TheCacheEntry;

import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;

public class PopulateSuggestedCategoriesAction implements AuthoringAction {
   private static final long serialVersionUID = 3472280114414215179L;

   /**
    * Used in conjunction with {@link PopulateSuggestedCategoriesAction#findCategoryTitles(String, Map, CategorySearchType)}
    * 
    * @author lcarpenter
    * @see PopulateSuggestedCategoriesAction#findCategoryTitles(String, Map, CategorySearchType)
    */
   public static enum CategorySearchType {
      /** 
       * Does an exact match of the category title to the body text when searching for possible categories.
       * Text in the body has to match the category's title exactly (excluding case)
       */
      EXACT_MATCH,

      /**
       * Does a strict match of the category title to the body text when searching for possible categories.
       * Text in the body has to have all the same words in the same order as the category's title (excluding case and optional characters)
       * @see RegExUtil#createOptionalCharPattern(String)
       */
      STRICT_MATCH,

      /**
       * Does a loose match of the category title to the body text when searching for possible categories.
       * Text in the body has to have at least one word (that is at least two character in length) that is contained in the category's title
       */
      LOOSE_MATCH
   };

   private static String s_newsBodyName = "NewsBody";

   private static String s_newsSuggestedCats = "SuggestedCats";

   private static String s_categoryLib = "PrudentialNewsDesign";

   /** s_log for the class */
   static Logger s_log = Logger.getLogger(PopulateSuggestedCategoriesAction.class.getName());

   @Override
   public String getDescription(Locale p_arg0) {
      return "Populate Suggested Categories";

   }

   @Override
   public String getTitle(Locale p_arg0) {
      return "Populate Suggested Categories";

   }

   @Override
   public boolean isValidForForm(FormContext fc) {
      // TODO Auto-generated method stub
      boolean isDebug = s_log.isLoggable(Level.FINEST);
      boolean isValid = false;

      if (isDebug) {
         s_log.entering("PopulateSuggestedCategoriesAction", "isValidForForm " + fc);
      }
      Document theDoc = fc.document();
      if (theDoc instanceof Content) {
         Content theContent = (Content) theDoc;
         if (theContent.hasComponent(s_newsBodyName) && theContent.hasComponent(s_newsSuggestedCats)) {
            isValid = true;
         }
      }
      if (isDebug) {
         s_log.entering("PopulateSuggestedCategoriesAction", "isValidForForm returning " + isValid);
      }
      return isValid;
   }

   @Override
   public int ordinal() {
      return 0;

   }

   @Override
   public ActionResult execute(FormContext formContext) {

      boolean isDebug = s_log.isLoggable(Level.FINEST);
      Content contentItem = (Content) formContext.document();
      if (contentItem.hasComponent(s_newsBodyName)) {
         try {
            RichTextComponent bodyCmpnt = (RichTextComponent) contentItem.getComponentByReference(s_newsBodyName);

            String bodyText = bodyCmpnt.getRichText();

            if (bodyText != null) {
               // Trim out leading and trailing spaces
               bodyText = bodyText.trim();

               // Strip out markup tags and extra white spaces
               bodyText = RegExUtil.HTML_TAG_PATTERN.matcher(bodyText).replaceAll(" ");
               bodyText = RegExUtil.EXCESSIVE_WHITESPACE_PATTERN.matcher(bodyText).replaceAll(" ");

               // If by chance the body text was completely empty (not
               // likely
               // since EditLive inserts <p>), don't do anything
               if (bodyText.length() != 0) {
                  Workspace ws = contentItem.getSourceWorkspace();
                  // TODO: persist categoryMap so it doesn't need to be build every time
                  //Map<String, Collection<String>> categoryMap = buildTokenIndexedCategoryMap(getCategoryTitles(ws));
                  ArrayList<ObjectWrapper> catList = (ArrayList<ObjectWrapper>) CategoryJSONWrapper.getCategoryWrapperList(ws, s_categoryLib);
                  Map<String, Collection<ObjectWrapper>> categoryMap = buildTokenIndexedObjectWrapperMap(catList);
                  List<ObjectWrapper> suggestedCategoryList = findCategoryTitles(bodyText, categoryMap, CategorySearchType.EXACT_MATCH);
                  StringBuilder suggestedCategoryText = new StringBuilder();

                  for (Iterator<ObjectWrapper> itor = suggestedCategoryList.iterator(); itor.hasNext();) {
                     suggestedCategoryText.append(itor.next());
                     if (itor.hasNext()) {
                        suggestedCategoryText.append(",");
                     }
                  }

                  TextComponent textCmpnt = (TextComponent) contentItem.getComponent(s_newsSuggestedCats);
                  textCmpnt.setText(suggestedCategoryText.toString());
                  contentItem.setComponent(s_newsSuggestedCats, textCmpnt);
               }
               else {
                  if (isDebug) {
                     s_log.finest("Body text was empty, don't continue");
                  }

               }
            }
         }
         catch (ComponentNotFoundException ex) {
            s_log.log(Level.WARNING, "Component not found when trying to add first sentence from Body text to First Sentence text", ex);

         }
         catch (IllegalTypeChangeException ex) {
            s_log.log(Level.SEVERE, "IllegalTypeChangeException", ex);
         }
      }
      else {
         if (s_log.isLoggable(Level.FINEST)) {
            if (!contentItem.hasComponent(s_newsBodyName)) {
               s_log.finest("Cannot find Body field");
            }
            if (!contentItem.hasComponent(s_newsSuggestedCats)) {
               s_log.finest("Cannot find 'Suggested Cats' field");
            }
            s_log.finest("In doc: " + formContext.document().getName().toString());
         }
      }
      return null;
   }

   protected static Map<String, Collection<String>> buildTokenIndexedCategoryMap(List<String> categories) {
      Map<String, Collection<String>> categoryTextMap = new HashMap<String, Collection<String>>();

      // iterate through category titles
      for (String catTitle : categories) {
         // split category title up by non word characters
         Set<String> tokens = RegExUtil.tokenizeText(catTitle);

         // iterate tokens
         for (String t : tokens) {
            // ignore single character tokens
            if (t.length() > 1) {
               // store category title in map by tokens
               Collection<String> values = categoryTextMap.get(t);
               if (values == null) {
                  values = new HashSet<String>();
                  categoryTextMap.put(t, values);
               }
               values.add(catTitle);
            }
         }
      }

      return categoryTextMap;
   }
   
   /**
    * 
    * buildTokenIndexedObjectWrapperMap description
    * @param categories list of the ObjectWrapper 
    * @return
    */
   protected static Map<String, Collection<ObjectWrapper>> buildTokenIndexedObjectWrapperMap(List<ObjectWrapper> categories) {
      Map<String, Collection<ObjectWrapper>> categoryTextMap = new HashMap<String, Collection<ObjectWrapper>>();

      // iterate through category titles
      for (ObjectWrapper catWrapper : categories) {
         // split category title up by non word characters
         Set<String> tokens = RegExUtil.tokenizeText(catWrapper.getLabel());

         // iterate tokens
         for (String t : tokens) {
            // ignore single character tokens
            if (t.length() > 1) {
               // store category title in map by tokens
               Collection<ObjectWrapper> values = categoryTextMap.get(t);
               if (values == null) {
                  values = new HashSet<ObjectWrapper>();
                  categoryTextMap.put(t, values);
               }
               values.add(catWrapper);
            }
         }
      }

      return categoryTextMap;
   }

   protected static List<ObjectWrapper> findCategoryTitles(String text, Map<String, Collection<ObjectWrapper>> categoryMap, CategorySearchType searchType) {
      boolean isDebug = s_log.isLoggable(Level.FINEST);

      // set text to lower case to avoid case issues
      text = text.toLowerCase();
      // create unique list of tokens from text
      Set<String> tokenSet = RegExUtil.tokenizeText(text);

      // create sorted array of tokens
      String[] textTokens = tokenSet.toArray(new String[tokenSet.size()]);
      Arrays.sort(textTokens);

      if (isDebug) {
         StringBuilder strb = new StringBuilder();
         for (String t : textTokens) {
            strb.append(t).append(",");
         }
         s_log.finest("Text Tokens: " + strb);
      }

      // narrow categories to a list of possible categories by matching text tokens to category tokens
      HashSet<ObjectWrapper> possibleCategories = new HashSet<ObjectWrapper>();
      for (Map.Entry<String, Collection<ObjectWrapper>> entry : categoryMap.entrySet()) {
         int index = Arrays.binarySearch(textTokens, entry.getKey());
         if (index > -1) {
            possibleCategories.addAll(entry.getValue());
         }
      }

      if (isDebug) {
         StringBuilder strb = new StringBuilder();
         for (ObjectWrapper t : possibleCategories) {
            strb.append(t.getLabel()).append(",");
         }
         s_log.finest("Possible Categories: " + strb);
      }

      List<ObjectWrapper> categories = Collections.emptyList();
      if (searchType == CategorySearchType.LOOSE_MATCH) {
         // use categories found during token search for loose match results
         categories = new ArrayList<ObjectWrapper>(possibleCategories);
      }
      else if (searchType == CategorySearchType.STRICT_MATCH) {
         // check possible categories against full text
         // turn category titles into optional char regex pattern
         categories = new ArrayList<ObjectWrapper>(possibleCategories.size());
         for (ObjectWrapper category : possibleCategories) {
            Pattern categoryPattern = RegExUtil.createOptionalCharPattern(category.getLabel());
            if (categoryPattern.matcher(text).find()) {
               categories.add(category);
            }
         }
      }
      else if (searchType == CategorySearchType.EXACT_MATCH) {
         // check possible tokens against full text
         categories = new ArrayList<ObjectWrapper>(possibleCategories.size());
         for (ObjectWrapper category : possibleCategories) {
            if (text.indexOf(category.getLabel().toLowerCase()) > -1) {
               categories.add(category);
            }
         }
      }

      return categories;
   }

   protected static List<String> getCategoryTitles(Workspace ws) {
      boolean isDebug = s_log.isLoggable(Level.FINEST);
      if (isDebug) {
         s_log.entering("PopulateSuggestedCategoriesAction", "getCategoryTitles");
      }
      
      TheCache theCache = TheCache.getInstance();
      List<String> categories = null;
      TheCacheEntry entry = null;
      entry = theCache.get(s_newsSuggestedCats);
      if(entry != null) {
         categories = (List)entry.getCacheEntry();
         if (isDebug) {
            s_log.log(Level.FINEST, "categories from cache;");
         }
         return categories;
      }     
      
      //871d653e-5ad8-40bf-a83f-47f1bcbebd98
      // this is the taxonomy
      DocumentLibrary currentDocLib = ws.getCurrentDocumentLibrary();
      DocumentId taxId = null;
      DocumentIdIterator cats = null;
      try {
         taxId = ws.createDocumentId("871d653e-5ad8-40bf-a83f-47f1bcbebd98");
      }
      catch (DocumentIdCreationException e) {
         // TODO Auto-generated catch block
         if (s_log.isLoggable(Level.FINEST))
         {
            s_log.log(Level.FINEST, "", e);
         }
      }
      if(taxId != null) {
         // get the children
         Taxonomy theTax;
         try {
            theTax = (Taxonomy)ws.getById(taxId);
            cats = theTax.getAllChildren();
         }
         catch (DocumentRetrievalException e) {
            // TODO Auto-generated catch block
            if (s_log.isLoggable(Level.FINEST))
            {
               s_log.log(Level.FINEST, "", e);
            }
         }
         catch (AuthorizationException e) {
            // TODO Auto-generated catch block
            if (s_log.isLoggable(Level.FINEST))
            {
               s_log.log(Level.FINEST, "", e);
            }
         }
        
      }
      else {
         ws.setCurrentDocumentLibrary(ws.getDocumentLibrary(s_categoryLib));
         cats = ws.findByType(DocumentTypes.Category);
      }
      
      if(cats != null) {       
         if (isDebug) {
            s_log.log(Level.FINEST, "cats != null");
         }
         categories = new ArrayList<String>(cats.getCount());
         try {
            while (cats.hasNext()) {
               String title = ws.getById(cats.next()).getTitle();
               categories.add(title);
            }
         }
         catch (WCMException ex) {
            s_log.log(Level.SEVERE, "Error occured while fetching categories from the library: " + s_categoryLib, ex);
         }
         finally {
            ws.setCurrentDocumentLibrary(currentDocLib);
         }      
         theCache.put(s_newsSuggestedCats, categories);
      }
      
      if (s_log.isLoggable(Level.FINEST)) {
         StringBuilder strb = new StringBuilder();
         for (String t : categories) {
            strb.append(t).append(",");
         }
         s_log.finest("All Categories: " + strb);
      }      
      return categories;
   }
   
   protected static List<ObjectWrapper> getCategoryWrapperList(Workspace ws) {
      DocumentLibrary currentDocLib = ws.getCurrentDocumentLibrary();
      ws.setCurrentDocumentLibrary(ws.getDocumentLibrary(s_categoryLib));
      DocumentIdIterator<Document> itor = ws.findByType(DocumentTypes.Category);
      List<ObjectWrapper> categories = new ArrayList<ObjectWrapper>(itor.getCount());
      try {
         while (itor.hasNext()) {
            DocumentId tempId = itor.next();
            String title = tempId.getName();
            String id = tempId.getID();
            ObjectWrapper ow = new ObjectWrapper(id, title);
            categories.add(ow);
         }
      }
      finally {
         ws.setCurrentDocumentLibrary(currentDocLib);
      }

      if (s_log.isLoggable(Level.FINEST)) {
         StringBuilder strb = new StringBuilder();
         for (ObjectWrapper t : categories) {
            strb.append(t.getLabel()).append(",");
         }
         s_log.finest("All Categories: " + strb);
      }

      return categories;
   }

   public ListModel<Locale> getLocales() {
      return null;
   }
}