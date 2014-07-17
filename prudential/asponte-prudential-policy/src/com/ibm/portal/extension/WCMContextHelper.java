/******************************************************************
 * Copyright IBM Corp. 2010                                       *
 ******************************************************************/

package com.ibm.portal.extension;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import com.ibm.portal.ObjectID;
import com.ibm.portal.services.contentmapping.ContentMapping;
import com.ibm.portal.services.contentmapping.ContentMappingInfo;
import com.ibm.portal.services.contentmapping.ContentMappingInfoHome;
import com.ibm.portal.services.contentmapping.exceptions.ContentMappingDataBackendException;
import com.ibm.portal.state.StateHolder;
import com.ibm.portal.state.accessors.exceptions.InvalidSelectionNodeIdException;
import com.ibm.portal.state.accessors.selection.SelectionAccessor;
import com.ibm.portal.state.accessors.selection.SelectionAccessorFactory;
import com.ibm.portal.state.exceptions.CannotInstantiateAccessorException;
import com.ibm.portal.state.exceptions.UnknownAccessorTypeException;
import com.ibm.portal.state.service.StateManagerService;
import com.ibm.workplace.wcm.api.DocumentId;
import com.ibm.workplace.wcm.api.Repository;
import com.ibm.workplace.wcm.api.WebContentService;
import com.ibm.workplace.wcm.api.Workspace;
import com.ibm.workplace.wcm.api.exceptions.DocumentIdCreationException;
import com.ibm.workplace.wcm.api.exceptions.DocumentRetrievalException;
import com.ibm.workplace.wcm.api.exceptions.IllegalDocumentTypeException;
import com.ibm.workplace.wcm.api.exceptions.OperationFailedException;
import com.ibm.workplace.wcm.api.exceptions.ServiceNotAvailableException;

/**
 * Abstract class containing helper methods to determine the current WCM context. 
 * This class can not be used directly. When in portal code please use PortalWCMContextHelper, 
 * when in portlet code please use PortletWCMContextHelper.
 */
public abstract class WCMContextHelper {

   private final ContentMappingInfoHome contentMappingInfoHome;
   private final WebContentService webContentService;

   /**
    * Initializes the WCMContextHelper.  
    * 
    * @throws NamingException 
    */
   public WCMContextHelper() throws NamingException {
      // this initialization needs to be done only once.
      final Context ctx = new InitialContext();
      contentMappingInfoHome = (ContentMappingInfoHome) ctx.lookup(ContentMappingInfoHome.JNDI_NAME);
      webContentService = (WebContentService) ctx.lookup("portal:service/wcm/WebContentService");
   }
   
    /**
     * Gets the default content mapping of the currently selected page. 
     * 
     * @param stateManagerService StateManagerService, entry point to the portal state API
     * @param state the current portal state
     * 
     * @return String representation of the content path that is mapped to the page.
     * 
     * @throws InvalidSelectionNodeIdException 
     * @throws CannotInstantiateAccessorException 
     * @throws UnknownAccessorTypeException 
     * @throws ContentMappingDataBackendException 
     * @throws OperationFailedException 
     * @throws ServiceNotAvailableException 
     * @throws DocumentIdCreationException 
     * @throws IllegalDocumentTypeException 
     * @throws DocumentRetrievalException 
     */
    protected String getContentMapping(final StateManagerService stateManagerService, final StateHolder state) 
          throws UnknownAccessorTypeException, CannotInstantiateAccessorException, 
          InvalidSelectionNodeIdException, ContentMappingDataBackendException, ServiceNotAvailableException, 
          OperationFailedException, DocumentIdCreationException, DocumentRetrievalException, 
          IllegalDocumentTypeException {
      final ObjectID currentPage = getCurrentPageID(stateManagerService, state);
      
      //get the default mapping for the current page
       final ContentMappingInfo cmInfo = contentMappingInfoHome.getContentMappingInfo(currentPage);
       final ContentMapping cm = cmInfo.getDefaultContentMapping();
       String contentPath = null;
       if (cm != null) {
          final String contentID = cm.getContentID();
          //find the content path to the ID
          final Repository repository = webContentService.getRepository();
          final Workspace workspace = repository.getWorkspace();
          try {
             final DocumentId docID = workspace.createDocumentId(contentID);
             contentPath = workspace.getPathById(docID, true, true);
          } finally {
             repository.endWorkspace();
          }
       }
      return contentPath;
    }
    
   /**
    * Gets the ID of the currently selected page.
    * 
    * @param stateManagerService StateManagerService, entry point to the portal state API
    * @param state the current portal state
    * 
    * @return Object ID of the currently selected page
    * 
    * @throws CannotInstantiateAccessorException 
    * @throws UnknownAccessorTypeException 
    * @throws InvalidSelectionNodeIdException 
    */
   protected ObjectID getCurrentPageID(final StateManagerService stateManagerService, final StateHolder state) 
         throws UnknownAccessorTypeException, CannotInstantiateAccessorException, 
         InvalidSelectionNodeIdException {
      ObjectID result = null;
      final SelectionAccessorFactory selectionAccFct = stateManagerService
            .getAccessorFactory(SelectionAccessorFactory.class);
      final SelectionAccessor selectionAcc = selectionAccFct.getSelectionAccessor(state);
      try {
         result = selectionAcc.getSelection();
      } finally {
         selectionAcc.dispose();
      }
      return result;
   }

   /**
    * Assembles a proper content path that resembles 'library/site/sitearea/content'. The method
    * takes care of placing path separators where necessary. The returned content path does not
    * contain path separators as first or as last character.
    * 
    * @param contentMapping
    *           The content mapping of the current page. Must not be <code>null</code>.
    * @param pathInfo
    *           The value of the path-info public render parameter for the current page. Must not be
    *           <code>null</code>.
    * @return A fully-qualified content path.
    */
   protected String assembleContentPath(final String contentMapping, final String[] pathInfo) {
      final StringBuilder result = new StringBuilder();
      // add the context mapping of the page w/o trailing forward slash
      if (contentMapping.charAt(contentMapping.length() - 1) == '/') {
         result.append(contentMapping, 0, contentMapping.length() - 1);
      } else {
         result.append(contentMapping);
      }

      // add all parts of path-info separated by a forward slashes
      for (final String pathInfoFragment : pathInfo) {
         if (pathInfoFragment != null && pathInfoFragment.length() > 0) {
            // add leading forward slash before each fragment
            result.append('/');

            // add the path-info fragment
            result.append(pathInfoFragment);
         }
      }
      return result.toString();
   }
} 