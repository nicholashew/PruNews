/********************************************************************/
/* Asponte
/* cmknight
/********************************************************************/

package com.prudential.authoring.launchpage;

import java.io.IOException;
import java.security.Principal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.ibm.portal.ModelException;
import com.ibm.portal.ObjectID;
import com.ibm.portal.navigation.NavigationNode;
import com.ibm.portal.navigation.NavigationSelectionModel;
import com.ibm.portal.portlet.service.PortletServiceHome;
import com.ibm.portal.portlet.service.PortletServiceUnavailableException;
import com.ibm.portal.portlet.service.model.NavigationSelectionModelProvider;
import com.ibm.portal.resolver.uri.PortletURI;
import com.ibm.portal.state.EngineURL;
import com.ibm.portal.state.PortletStateManager;
import com.ibm.portal.state.URLFactory;
import com.ibm.portal.state.accessors.portlet.PortletAccessorController;
import com.ibm.portal.state.accessors.portlet.PortletAccessorFactory;
import com.ibm.portal.state.accessors.selection.SelectionAccessorController;
import com.ibm.portal.state.accessors.selection.SelectionAccessorFactory;
import com.ibm.portal.state.exceptions.StateException;
import com.ibm.portal.state.service.PortletStateManagerService;
import com.ibm.workplace.wcm.api.*;
import com.ibm.workplace.wcm.api.exceptions.AuthorizationException;
import com.ibm.workplace.wcm.api.exceptions.ComponentNotFoundException;
import com.ibm.workplace.wcm.api.exceptions.DocumentRetrievalException;
import com.ibm.workplace.wcm.api.exceptions.IllegalDocumentTypeException;
import com.ibm.workplace.wcm.api.exceptions.OperationFailedException;
import com.ibm.workplace.wcm.api.exceptions.PropertyRetrievalException;
import com.ibm.workplace.wcm.api.exceptions.QueryServiceException;
import com.ibm.workplace.wcm.api.query.PageIterator;
import com.ibm.workplace.wcm.api.query.Query;
import com.ibm.workplace.wcm.api.query.QueryService;
import com.ibm.workplace.wcm.api.query.QueryStructureException;
import com.ibm.workplace.wcm.api.query.ResultIterator;
import com.ibm.workplace.wcm.api.query.Selector;
import com.ibm.workplace.wcm.api.query.Selectors;
import com.ibm.workplace.wcm.api.query.SortDirection;
import com.ibm.workplace.wcm.api.query.Sorts;
import com.prudential.utils.Utils;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.portlet.PortletRequest;
import javax.portlet.PortletResponse;
import javax.portlet.RenderResponse;
//import org.apache.jetspeed.portlet.PortletResponse;
//import org.apache.jetspeed.portlet.PortletURI;
import javax.portlet.RenderRequest;

public class CustomAuthoringLaunchPageQueries {

   /** Logger for the class */
   private static Logger s_log = Logger.getLogger(CustomAuthoringLaunchPageQueries.class.getName());

   private static Map<String, String> m_iconPathMap = new HashMap();

   private static Map<String, String> m_statusMap = new HashMap();

   static PortletStateManagerService portletStateManagerService;

   protected static PortletServiceHome navSelHome = null;

   public enum SupportedComponentTypes {

      DateComponent, FileComponent, HTMLComponent, ImageComponent, JSPComponent, LinkComponent, NumericComponent, OptionSelectionComponent, ReferenceComponent, RichTextComponent, TextComponent, ShortTextComponent, UserSelectionComponent
   }

   // populate the map
   static {
      m_iconPathMap.put("WCM_Content", "/images/forms/Content.png");
      m_iconPathMap.put("WCM_ContentLink", "/images/views/ContentLink.png");
      m_statusMap.put("1", "Draft");
      m_statusMap.put("2", "Published");
      m_statusMap.put("4", "Expired");

   }

   /**
    * Build a query
    * @param p_selectors Query selectors
    * @param p_queryParams The query parameters
    * @return The query
    */
   public static Query buildQuery(List<Selector> p_selectors, CustomAuthoringLaunchPageQueryParams p_queryParams) {

      boolean isDebug = s_log.isLoggable(Level.FINEST);

      if (isDebug) {
         s_log.entering("CustomAuthoringLaunchPageQueries", "buildQuery", new Object[] {p_selectors, p_queryParams});
      }

      long startTime = -1;
      if (isDebug) {
         startTime = System.currentTimeMillis();
      }

      Query query = null;

      Workspace wcmWorkspace = Utils.getWorkspace();
      if (wcmWorkspace != null) {
         wcmWorkspace.login();

         try {
            // Get the query service
            QueryService queryService = wcmWorkspace.getQueryService();
            if (queryService != null) {
               // Create a query
               query = queryService.createQuery();

               // Optional library selector.
               if (p_queryParams.getLibraries() != null) {
                  Selector selectedLibraries = Selectors.libraryIn(p_queryParams.getLibraries());
                  if (selectedLibraries != null) {
                     query.addSelector(selectedLibraries);
                  }
               }

               // Limit the types
               // if the params have a selector by type add it
               Selector classesSelector = p_queryParams.getClassesSelector();
               if (classesSelector != null) {
                  query.addSelector(classesSelector);
               }

               if (p_selectors != null) {
                  for (Selector selector : p_selectors) {
                     query.addSelector(selector);
                  }
               }

               // Sorting for the query
               query = applySortingToQuery(query, p_queryParams);
            }
            else {
               query = null;
            }
         }
         finally {
            wcmWorkspace.logout();
         }
      }

      if (s_log.isLoggable(Level.FINE)) {
         s_log.fine("Total time for LaunchPageQueries.buildQuery = " + (System.currentTimeMillis() - startTime) + " ms");
      }

      if (s_log.isLoggable(Level.FINER)) {
         s_log.exiting("LaunchPageQueries", "buildQuery", query);
      }

      return query;
   }

   /**
    * Apply the sorting specified in the query parameters
    * @param p_query The query
    * @param p_queryParams The query parameters
    * @return
    */
   private static Query applySortingToQuery(Query p_query, CustomAuthoringLaunchPageQueryParams p_queryParams) {
      // Sorting for the query
      if (p_queryParams.getTitleSortActive()) {
         if (p_queryParams.getTitleSortReverse()) {
            p_query.addSort(Sorts.byTitle(SortDirection.DESCENDING));
         }
         else {
            p_query.addSort(Sorts.byTitle(SortDirection.ASCENDING));
         }
      }

      if (p_queryParams.getModifiedSortActive()) {
         if (p_queryParams.getModifiedSortReverse()) {
            p_query.addSort(Sorts.byDateModified(SortDirection.DESCENDING));
         }
         else {
            p_query.addSort(Sorts.byDateModified(SortDirection.ASCENDING));
         }
      }

      if (p_queryParams.getCreatedSortActive()) {
         if (p_queryParams.getCreatedSortReverse()) {
            p_query.addSort(Sorts.byDateCreated(SortDirection.DESCENDING));
         }
         else {
            p_query.addSort(Sorts.byDateCreated(SortDirection.ASCENDING));
         }
      }

      return p_query;
   }

   /**
    * Run the query to get the results
    * @param p_workspace 
    * @param p_query The query
    * @param p_queryParams The query parameters
    * @return The query results
    * @throws IllegalArgumentException 
    * @throws QueryServiceException 
    * @throws QueryStructureException 
    */
   public static ResultIterator runQuery(Workspace p_workspace, Query p_query, CustomAuthoringLaunchPageQueryParams p_queryParams)
      throws QueryServiceException, IllegalArgumentException, QueryStructureException {
      if (s_log.isLoggable(Level.FINER)) {
         s_log.entering("CustomAuthoringLaunchPageQueries", "runQuery", new Object[] {p_workspace, p_query, p_queryParams});
      }

      long startTime = -1;
      if (s_log.isLoggable(Level.FINE)) {
         startTime = System.currentTimeMillis();
      }

      ResultIterator results = null;

      // Get the query service
      QueryService queryService = p_workspace.getQueryService();

      // Run the query.
      PageIterator pages = queryService.execute(p_query, p_queryParams.getPageSize(), p_queryParams.getFirstPage());
      results = pages.next();

      if (s_log.isLoggable(Level.FINE)) {
         s_log.fine("Total time for CustomAuthoringLaunchPageQueries.runQuery = " + (System.currentTimeMillis() - startTime) + " ms");
      }

      if (s_log.isLoggable(Level.FINER)) {
         s_log.exiting("CustomAuthoringLaunchPageQueries", "runQuery", results);
      }

      return results;
   }

   /**
    * 
    * wrapResults wrap the results of a query in item wrappers
    * @param theResults the list of results
    * @param request the request
    * @param response the response
    * @param attributeNames String[] of any components on the content/site areas we want to load.
    * @return
    */
   //public static List<CustomAuthoringItemWrapper> wrapResults(ResultIterator theResults, PortletRequest request, PortletResponse response,
   public static List<CustomAuthoringItemWrapper> wrapResults(ResultIterator theResults, Object request, Object response,
      String[] attributeNames, boolean retrieveCats) {
      boolean isDebug = s_log.isLoggable(Level.FINEST);
      ArrayList<CustomAuthoringItemWrapper> resultList = new ArrayList();

      if (isDebug) {
         s_log.entering("CustomAuthoringLaunchPageQueries", "wrapResults");
      }

      // iterate, add the result to the item wrapper
      while (theResults.hasNext()) {
         // get the current result
         Document currentResult = (Document)theResults.next();
         if (currentResult != null) {
            CustomAuthoringItemWrapper tempWrapper = wrapSingleResult(currentResult,request,response,attributeNames,retrieveCats);
            if (tempWrapper != null) {
               resultList.add(tempWrapper);
            }
         }
      }
      if (isDebug) {
         s_log.exiting("CustomAuthoringLaunchPageQueries", "wrapResults");
      }

      return resultList;
   }

   /**
    * 
    * wrapResults wrap the results of a query in item wrappers
    * @param theResults the list of results
    * @param request the request
    * @param response the response
    * @param attributeNames String[] of any components on the content/site areas we want to load.
    * @return
    */
   //public static List<CustomAuthoringItemWrapper> wrapResults(ResultIterator theResults, PortletRequest request, PortletResponse response,
   public static List<CustomAuthoringItemWrapper> wrapResults(DocumentIdIterator theResults, Object request, Object response,
      String[] attributeNames, boolean retrieveCats) {
      boolean isDebug = s_log.isLoggable(Level.FINEST);
      ArrayList<CustomAuthoringItemWrapper> resultList = new ArrayList();

      if (isDebug) {
         s_log.entering("CustomAuthoringLaunchPageQueries", "wrapResults");
      }

      // iterate, add the result to the item wrapper
      while (theResults.hasNext()) {
         // get the current result
         DocumentId currentId = (DocumentId) theResults.next();
         Document currentResult;
         try {
            currentResult = (Document) Utils.getSystemWorkspace().getById(currentId);
            if (currentResult != null) {
               CustomAuthoringItemWrapper tempWrapper = wrapSingleResult(currentResult,request,response,attributeNames,retrieveCats);
               if (tempWrapper != null) {
                  resultList.add(tempWrapper);
               }
            }
         }
         catch (DocumentRetrievalException e1) {
            // TODO Auto-generated catch block
            if (s_log.isLoggable(Level.FINEST)) {
               s_log.log(Level.FINEST, "", e1);
            }
         }
         catch (AuthorizationException e1) {
            // TODO Auto-generated catch block
            if (s_log.isLoggable(Level.FINEST)) {
               s_log.log(Level.FINEST, "", e1);
            }
         }

      }
      if (isDebug) {
         s_log.exiting("CustomAuthoringLaunchPageQueries", "wrapResults");
      }

      return resultList;
   }

   /**
    * 
    * wrapResults wrap the results of a query in item wrappers
    * @param theResults the list of results
    * @param request the request
    * @param response the response
    * @param attributeNames String[] of any components on the content/site areas we want to load.
    * @return
    */
   //public static List<CustomAuthoringItemWrapper> wrapResults(ResultIterator theResults, PortletRequest request, PortletResponse response,
   public static List<CustomAuthoringItemWrapper> wrapResults(DocumentId[] theResults, Object request, Object response,
      String[] attributeNames, boolean retrieveCats) {
      boolean isDebug = s_log.isLoggable(Level.FINEST);
      ArrayList<CustomAuthoringItemWrapper> resultList = new ArrayList();

      if (isDebug) {
         s_log.entering("CustomAuthoringLaunchPageQueries", "wrapResults");
      }

      // iterate, add the result to the item wrapper
      for (int x = 0; x < theResults.length; x++) {
         // get the current result
         DocumentId currentId = (DocumentId) theResults[x];
         Document currentResult;
         try {
            currentResult = (Document) Utils.getSystemWorkspace().getById(currentId);
            if (currentResult != null) {
               CustomAuthoringItemWrapper tempWrapper = wrapSingleResult(currentResult,request,response,attributeNames,retrieveCats);
               if (tempWrapper != null) {
                  resultList.add(tempWrapper);
               }
            }
            
         }
         catch (DocumentRetrievalException e1) {
            // TODO Auto-generated catch block
            if (s_log.isLoggable(Level.FINEST)) {
               s_log.log(Level.FINEST, "", e1);
            }
         }
         catch (AuthorizationException e1) {
            // TODO Auto-generated catch block
            if (s_log.isLoggable(Level.FINEST)) {
               s_log.log(Level.FINEST, "", e1);
            }
         }

      }
      if (isDebug) {
         s_log.exiting("CustomAuthoringLaunchPageQueries", "wrapResults");
      }

      return resultList;
   }

   /**
    * Create a legacy Portal URI with the given parameters
    * @param p_response Portlet response
    * @param p_parameters URI parameters
    * @return The URI
    */
   public static String createPortalURIJSR(RenderRequest p_request, RenderResponse p_response, Map<String, String> p_parameters) {
      String uri = "";//p_response.createURI();
      PortletStateManager portletStateManager = null;

      try {
         portletStateManager = getPortletStateManagerService().getPortletStateManager(p_request, p_response);
         final URLFactory urlFct = portletStateManager.getURLFactory();
         final EngineURL url = urlFct.newURL(null);
         ObjectID objectID = getCurrentPage(p_request, p_response);

         final SelectionAccessorFactory selectionAccessorFactory = portletStateManager.getAccessorFactory(SelectionAccessorFactory.class);
         SelectionAccessorController selectionAccessorCOntroller = selectionAccessorFactory.getSelectionController(url.getState());
         selectionAccessorCOntroller.setSelection(objectID);

         final PortletAccessorFactory portletFct = (PortletAccessorFactory) portletStateManager
            .getAccessorFactory(PortletAccessorFactory.class);
         final PortletAccessorController portletCtrl = portletFct.getPortletController(objectID, url.getState());

         for (Map.Entry<String, String> entry : p_parameters.entrySet()) {
            String[] values = {(String) entry.getValue()};
            portletCtrl.getParameters().put(entry.getKey(), values);
         }

         uri = url.toString();

      }
      catch (Exception e) {
         e.printStackTrace(System.out);
      }

      return uri;
   }

   /**
    * Create a legacy Portal URI with the given parameters
    * @param p_response Portlet response
    * @param p_parameters URI parameters
    * @return The URI
    */
   public static String createPortalURI(org.apache.jetspeed.portlet.PortletResponse p_response, Map<String, String> p_parameters) {
      org.apache.jetspeed.portlet.PortletURI uri = p_response.createURI();
      for (Map.Entry<String, String> entry : p_parameters.entrySet()) {
         uri.addParameter(entry.getKey(), entry.getValue());
      }
      return uri.toString();
   }

   public static String createURIGateway(Object theRequest, Object p_response, Map<String, String> p_parameters) {
      boolean isDebug = s_log.isLoggable(Level.FINEST);
      if (isDebug) {
         s_log.entering("CustomAuthoringLaunchPageQueries", "createURIGateway "+theRequest);
      }
      
      if (p_response instanceof org.apache.jetspeed.portlet.PortletResponse) {
         return createPortalURI((org.apache.jetspeed.portlet.PortletResponse) p_response, p_parameters);         
      }
      else {
         return createPortalURIJSR((RenderRequest) theRequest, (RenderResponse) p_response, p_parameters);
      }
   }

   /**
    * 
    * buildActions description
    * @param uuid the uuid of the item in question
    * @param p_response portletresponse
    * @return
    */
   public static List<CustomAuthoringItemAction> buildActions(String itemType, String uuid, Object p_request, Object p_response) {

      ArrayList<CustomAuthoringItemAction> actionList = new ArrayList<CustomAuthoringItemAction>();
      boolean isDebug = s_log.isLoggable(Level.FINEST);

      if (isDebug) {
         s_log.entering("CustomAuthoringLaunchPageQueries", "buildActions");
      }

      // for now add the edit action
      Map<String, String> editParameters = new HashMap<String, String>(3);
      editParameters.put("wcmAuthoringAction", "edit");
      editParameters.put("docid", itemType + "/" + uuid);
      String editItemURI = createURIGateway(p_request, p_response, editParameters);
      actionList.add(new CustomAuthoringItemAction(CustomAuthoringItemWrapper.EDIT_ACTION_ID, "Edit", editItemURI));

      // for now add the edit action
      Map<String, String> readParameters = new HashMap<String, String>(3);
      editParameters.put("wcmAuthoringAction", "read");
      editParameters.put("docid", itemType + "/" + uuid);
      String readItemURI = createURIGateway(p_request, p_response, editParameters);
      actionList.add(new CustomAuthoringItemAction(CustomAuthoringItemWrapper.OPEN_ACTION_ID, "Read", readItemURI));

      // add the preview
      Map<String, String> previewParameters = new HashMap<String, String>(3);
      previewParameters.put("wcmAuthoringAction", "preview");
      previewParameters.put("docid", itemType + "/" + uuid);
      // remove this to not do new window
      //previewParameters.put("target", "_blank");      
      String previewItemURI = createURIGateway(p_request, p_response, previewParameters);

      actionList.add(new CustomAuthoringItemAction(CustomAuthoringItemWrapper.PREVIEW_ACTION_ID, "Preview", previewItemURI));

      if (isDebug) {
         s_log.exiting("CustomAuthoringLaunchPageQueries", "buildActions returning " + actionList);
      }

      return actionList;
   }

   /**
    * 
    * buildAdditionalAttributes get the additional attributes of the content we're trying to load.
    * @param attNames
    * @param p_currentDocument
    * @return
    */
   public static Map<String, Object> buildAdditionalAttributes(String[] attNames, Document p_currentDocument) {
      Map<String, Object> addAttributes = new HashMap();
      boolean isDebug = s_log.isLoggable(Level.FINEST);

      if (isDebug) {
         s_log.entering("CustomAuthoringLaunchPageQueries", "buildAdditionalAttributes");
      }

      if (attNames != null && attNames.length > 0) {
         // check if it has it
         if (p_currentDocument instanceof ContentComponentContainer) {
            ContentComponentContainer currentResult = (ContentComponentContainer) p_currentDocument;
            for (int x = 0; x < attNames.length; x++) {
               String currentKey = attNames[x];
               if (isDebug) {
                  s_log.log(Level.FINEST, "currentKey =" + currentKey);
               }
               if (currentResult.hasComponent(currentKey)) {
                  try {
                     ContentComponent resultCmpnt = currentResult.getComponentByReference(currentKey);
                     String resultString = getCmpntString(resultCmpnt);
                     if (resultString != null && !resultString.equalsIgnoreCase("")) {
                        if (isDebug) {
                           s_log.log(Level.FINEST, "found, adding with the key " + currentKey + " value " + resultString);
                        }
                        addAttributes.put(currentKey, resultString);
                     }

                  }
                  catch (ComponentNotFoundException e) {
                     // TODO Auto-generated catch block
                     if (s_log.isLoggable(Level.FINEST)) {
                        s_log.log(Level.FINEST, "", e);
                     }
                  }
               }

            }
         }

      }

      if (isDebug) {
         s_log.exiting("CustomAuthoringLaunchPageQueries", "buildAdditionalAttributes returning " + addAttributes);
      }

      return addAttributes;
   }

   /**
    * 
    * getCmpntString description get the string representation of the component value.
    * @param p_resultCmpnt
    * @return
    */
   public static String getCmpntString(ContentComponent p_resultCmpnt) {
      String returnString = "";
      boolean isDebug = s_log.isLoggable(Level.FINEST);

      if (isDebug) {
         s_log.entering("CustomAuthoringLaunchPageQueries", "getCmpntString");
      }

      // get the type

      //WCM_DocumentType. temp = null;
      String docType = p_resultCmpnt.getDocumentType().getApiType().getSimpleName();
      SupportedComponentTypes docTypeSwitch = SupportedComponentTypes.valueOf(docType);
      switch (docTypeSwitch) {

      case DateComponent:
         DateComponent theDateComponent = (DateComponent) p_resultCmpnt;
         returnString = "" + theDateComponent.getDate().getTime();
         break;
      case FileComponent:
         FileComponent theFileComponent = (FileComponent) p_resultCmpnt;
         returnString = theFileComponent.getResourceURL();
         break;
      case HTMLComponent:
         HTMLComponent theHTMLComponent = (HTMLComponent) p_resultCmpnt;
         returnString = theHTMLComponent.getHTML();
         break;
      case ImageComponent:
         ImageComponent theImageComponent = (ImageComponent) p_resultCmpnt;
         returnString = theImageComponent.getResourceURL();
         break;
      case JSPComponent:
         JSPComponent theJSPComponent = (JSPComponent) p_resultCmpnt;
         returnString = theJSPComponent.getJspPath();
         break;
      case LinkComponent:
         LinkComponent theLinkComponent = (LinkComponent) p_resultCmpnt;
         returnString = theLinkComponent.getURL();
         break;
      case NumericComponent:
         NumericComponent theNumericComponent = (NumericComponent) p_resultCmpnt;
         returnString = theNumericComponent.getNumber().toString();
         break;
      case OptionSelectionComponent:
         OptionSelectionComponent theOptionSelectionComponent = (OptionSelectionComponent) p_resultCmpnt;
         String[] selections = theOptionSelectionComponent.getSelections();
         if (selections != null && selections.length > 0) {
            returnString = selections.toString();
         }
         break;
      case ReferenceComponent:
         ReferenceComponent theReferenceComponent = (ReferenceComponent) p_resultCmpnt;
         // just get the name of the component it's referencing
         if (theReferenceComponent.getComponentRef() != null) {
            returnString = theReferenceComponent.getComponentRef().getName();
         }
         break;
      case RichTextComponent:
         RichTextComponent theRichTextComponent = (RichTextComponent) p_resultCmpnt;
         returnString = theRichTextComponent.getRichText();
         break;
      case TextComponent:
         TextComponent theTextComponent = (TextComponent) p_resultCmpnt;
         returnString = theTextComponent.getText();
         break;
      case ShortTextComponent:
         ShortTextComponent theShortTextComponent = (ShortTextComponent) p_resultCmpnt;
         returnString = theShortTextComponent.getText();
         break;
      case UserSelectionComponent:
         UserSelectionComponent theUserSelectionComponent = (UserSelectionComponent) p_resultCmpnt;
         Principal[] theSelections = theUserSelectionComponent.getSelections();
         if (theSelections != null && theSelections.length > 0) {
            StringBuilder selectionStringBuilder = new StringBuilder();
            for (int x = 0; x < theSelections.length; x++) {
               Principal tempPrincipal = theSelections[x];
               if (tempPrincipal != null) {
                  selectionStringBuilder.append(tempPrincipal.getName());
                  if (x < theSelections.length - 1) {
                     selectionStringBuilder.append(",");
                  }
               }
            }
            returnString = selectionStringBuilder.toString();
         }
         break;
      default:
         returnString = "";
         if (isDebug) {
            s_log.log(Level.FINEST, "default case reached for string " + docType);
         }

      }

      if (isDebug) {
         s_log.exiting("CustomAuthoringLaunchPageQueries", "getCmpntString returning " + returnString);

      }

      return returnString;
   }

   /**
    * 
    * getPortletStateManagerService method to get the portletStateManagerService
    * @return
    */
   public static PortletStateManagerService getPortletStateManagerService() {
      if (portletStateManagerService == null) {
         try {
            InitialContext context = new InitialContext();

            final PortletServiceHome serviceHome = (PortletServiceHome) context
               .lookup("portletservice/com.ibm.portal.state.service.PortletStateManagerService");
            portletStateManagerService = (PortletStateManagerService) serviceHome.getPortletService(PortletStateManagerService.class);

         }
         catch (NamingException e) {
            e.printStackTrace(System.out);
         }
         catch (PortletServiceUnavailableException e) {
            // TODO Auto-generated catch block
            if (s_log.isLoggable(Level.FINEST)) {
               s_log.log(Level.FINEST, "", e);
            }
         }
      }
      return portletStateManagerService;
   }

   /**
   * This is a method on how to get the current page you are on. This can be used when targetting portlets on the same page and you do
   * not want to hard code the ObjectId, once you have the ObjectID you can get the uniquename from that. This should only be used when trying to 
   * get the current page from within a portlet not using PPR
   * 
   * @param request PortletRequest
   * @param response PortletResponse
   * @return ObjectID of the page
   * @throws StateException
   * @throws NamingException
   * @throws IOException
   */
   public static ObjectID getCurrentPage(PortletRequest request, PortletResponse response) throws StateException, NamingException,
      IOException {
      ObjectID oId = null;
      try {
         NavigationSelectionModelProvider provider = getNavigationSelectionModelProvider();

         NavigationSelectionModel model = provider.getNavigationSelectionModel(request, response);
         NavigationNode node = (NavigationNode) model.getSelectedNode();
         oId = node.getObjectID();
      }
      catch (ModelException e) {
         System.err.println("The current page could not be located = " + e);
      }

      return oId;
   }

   protected static NavigationSelectionModelProvider getNavigationSelectionModelProvider() {
      NavigationSelectionModelProvider provider = null;
      try {
         if (navSelHome == null) {
            Context ctx = new InitialContext();
            navSelHome = (PortletServiceHome) ctx
               .lookup("portletservice/com.ibm.portal.portlet.service.model.NavigationSelectionModelProvider");
         }
         provider = (NavigationSelectionModelProvider) navSelHome.getPortletService(NavigationSelectionModelProvider.class);
      }
      catch (Exception e) {
         System.err.println("There was an error getting the navigation selection model provider = " + e);
      }

      return provider;
   }

   static CustomAuthoringItemWrapper wrapSingleResult(Document currentResult, Object request, Object response,
      String[] attributeNames, boolean retrieveCats) {
            
      boolean isDebug = s_log.isLoggable(Level.FINEST);
      if (isDebug) {
         s_log.entering("CustomAuthoringLaunchPageQueries", "wrapSingleResult");
      }
      
      CustomAuthoringItemWrapper tempWrapper = null;

      Item currentItem = (Item) currentResult;
      if (isDebug) {
         s_log.log(Level.FINEST, "currentItem +" + currentItem);
      }
      // get the icon path
      String iconPath = m_iconPathMap.get(currentItem.getClass());
      if (iconPath == null || iconPath.equals("")) {
         iconPath = "/images/forms/Content.png";
      }
      String iconDisplayName = "ICON";

      //iconPath = request.getContextPath() + iconPath;

      String status = "Published";
      if (currentResult instanceof Content) {
         Content currentContent = (Content) currentResult;
         if (isDebug) {
            s_log.log(Level.FINEST, "currentResult = content, status is " + currentContent.getWorkflowStatus());
         }
         status = "" + m_statusMap.get("" + currentContent.getWorkflowStatus());
      }

      Document currentDocument = (Document) currentResult;
      String resultLib = currentDocument.getOwnerLibrary().getTitle();

      // get the authors
      String[] authors = currentDocument.getAuthors();
      String author = "";
      // get the first one
      if (authors.length > 0) {
         author = authors[0];
      }

      // create actions
      String type = currentItem.getIdentity().getTypeClass().getName();
      List<CustomAuthoringItemAction> itemActions = buildActions(type, currentItem.getIdentity().getID(), request, response);

      Map<String, Object> additionalAttributeMap = buildAdditionalAttributes(attributeNames, currentDocument);

      if (isDebug) {
         s_log.log(Level.FINEST, "additionalAttributeMap =" + additionalAttributeMap);
      }

      // get last mod, live date, and review dates
      Date lastModDate = currentDocument.getModifiedDate();
      Date effectiveDate = null;
      String workflowStageName = "";

      if (currentDocument instanceof WorkflowedDocument) {
         try {
            effectiveDate = ((WorkflowedDocument) currentDocument).getEffectiveDate();
            DocumentId currentStageId = ((WorkflowedDocument) currentDocument).getWorkflowStageId();
            if(currentStageId != null) {
               workflowStageName = currentStageId.getName();
            }
         }
         catch (PropertyRetrievalException e) {
            // TODO Auto-generated catch block
            if (s_log.isLoggable(Level.FINEST)) {
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
      // if I'm content, get the authoring template
      String atName = "";
      Date expiredDate = null;
      if (currentDocument instanceof Content) {
         Content theContent = (Content) currentDocument;
         try {
            atName = theContent.getAuthoringTemplateID().getName();
            expiredDate = theContent.getExpiryDate();
         }
         catch (AuthorizationException e) {
            // TODO Auto-generated catch block
            if (s_log.isLoggable(Level.FINEST)) {
               s_log.log(Level.FINEST, "", e);
            }
         }
         catch (PropertyRetrievalException e) {
            // TODO Auto-generated catch block
            if (s_log.isLoggable(Level.FINEST)) {
               s_log.log(Level.FINEST, "", e);
            }
         }
      }

      Date createdDate = currentDocument.getCreationDate();
      
      tempWrapper = new CustomAuthoringItemWrapper(iconDisplayName, iconPath, currentItem.getTitle(),
         currentItem.getIdentity().getID(), status, itemActions, resultLib, author, createdDate, lastModDate, effectiveDate,
         additionalAttributeMap, atName, workflowStageName);
      
      if(expiredDate != null) {
         tempWrapper.setExpireDate(expiredDate);
      }
      String itemPath = "";
      Workspace ws = Utils.getWorkspace();
      try {
         itemPath = ws.getPathById(currentDocument.getId(), false, false);
         if(itemPath != null) {
            tempWrapper.setPath(itemPath);
         }
      }
      catch (DocumentRetrievalException e) {
         // TODO Auto-generated catch block
         if (s_log.isLoggable(Level.FINEST))
         {
            s_log.log(Level.FINEST, "", e);
         }
      }
      catch (IllegalDocumentTypeException e) {
         // TODO Auto-generated catch block
         if (s_log.isLoggable(Level.FINEST))
         {
            s_log.log(Level.FINEST, "", e);
         }
      }

      
      
     
      if(retrieveCats) {
         /** a map of categories where the key will be the uuid, the value the category name */
         ArrayList<DocumentId> categoryList = getCategoryList(currentDocument);
         
         tempWrapper.setCategories(categoryList);
      }
      
      if (isDebug) {
         s_log.exiting("CustomAuthoringLaunchPageQueries", "wrapSingleResult returning "+tempWrapper);
      }
      
      
      return tempWrapper;
   }
   
   /**
    * 
    * getCategoryMap retrieve the category map for an object
    * @param theDocument the document to retrieve the categories for 
    * @return
    */
   private static ArrayList<DocumentId> getCategoryList(Document theDocument) {
      ArrayList<DocumentId> categoryList = new ArrayList<DocumentId>();
      
      if(theDocument instanceof ContentComponentContainer) {
      
         // get the cats, get the names for the cats, 
         DocumentId[] catArray = ((ContentComponentContainer)theDocument).getCombinedCategoryIds();
         categoryList = new ArrayList<DocumentId>(Arrays.asList(catArray));
      }
      
      return categoryList;
      
   }

}
