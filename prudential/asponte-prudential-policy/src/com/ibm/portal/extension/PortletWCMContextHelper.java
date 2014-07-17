/******************************************************************
 * Copyright IBM Corp. 2010                                       *
 ******************************************************************/
package com.ibm.portal.extension;
import java.util.Map;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;
import com.ibm.portal.portlet.service.PortletServiceHome;
import com.ibm.portal.portlet.service.PortletServiceUnavailableException;
import com.ibm.portal.services.contentmapping.exceptions.ContentMappingDataBackendException;
import com.ibm.portal.state.PortletStateManager;
import com.ibm.portal.state.accessors.exceptions.InvalidSelectionNodeIdException;
import com.ibm.portal.state.accessors.exceptions.StateNotInRequestException;
import com.ibm.portal.state.exceptions.CannotInstantiateAccessorException;
import com.ibm.portal.state.exceptions.StateManagerException;
import com.ibm.portal.state.exceptions.UnknownAccessorTypeException;
import com.ibm.portal.state.service.PortletStateManagerService;
import com.ibm.workplace.wcm.api.exceptions.DocumentIdCreationException;
import com.ibm.workplace.wcm.api.exceptions.DocumentRetrievalException;
import com.ibm.workplace.wcm.api.exceptions.IllegalDocumentTypeException;
import com.ibm.workplace.wcm.api.exceptions.OperationFailedException;
import com.ibm.workplace.wcm.api.exceptions.ServiceNotAvailableException;
/**
 * Helper class to determine the current WCM context. This class can only be used from portlet code. 
 * From portal code (e.g. theme)  please use PortalWCMContextHelper instead. 
 */
public class PortletWCMContextHelper extends WCMContextHelper {
   
   private final PortletStateManagerService stateManagerService;
   /**
    * Initializes the PortletWCMContextHelper. 
    * 
    * @throws NamingException 
    * @throws PortletServiceUnavailableException 
    */
   public PortletWCMContextHelper() throws NamingException, PortletServiceUnavailableException {
      // this initialization needs to be done only once
      final Context ctx = new InitialContext();
      PortletServiceHome psh = (PortletServiceHome) ctx.lookup(PortletStateManagerService.JNDI_NAME);
      stateManagerService = psh.getPortletService(PortletStateManagerService.class); 
   }
   
   /**
    * Gets the WCM context of the current page. It checks path info, public render parameter and 
    * content mapping in this order. A WCM context defined as private render parameter or 
    * preference of the Web Content Viewer portlet is NOT returned.
    *  
    * @param request PortletRequest
    * @param response PortletResponse
    * 
    * @return String representation of a content path.
    * 
    * @throws StateManagerException 
    * @throws IllegalDocumentTypeException 
    * @throws DocumentRetrievalException 
    * @throws DocumentIdCreationException 
    * @throws OperationFailedException 
    * @throws ServiceNotAvailableException 
    * @throws ContentMappingDataBackendException 
    * @throws StateNotInRequestException 
    * @throws InvalidSelectionNodeIdException 
    * @throws CannotInstantiateAccessorException 
    * @throws UnknownAccessorTypeException 
    */
   public String getCurrentWCMContext(final RenderRequest request, final RenderResponse response) 
         throws StateManagerException, UnknownAccessorTypeException, CannotInstantiateAccessorException, 
         InvalidSelectionNodeIdException, StateNotInRequestException, ContentMappingDataBackendException, 
         ServiceNotAvailableException, OperationFailedException, DocumentIdCreationException, 
         DocumentRetrievalException, IllegalDocumentTypeException {
      String contentPath = null;
      Map<String, String[]> publicParameter = request.getPublicParameterMap();
      // check path info
      if (publicParameter.containsKey("PATH_INFO")) {
         final String[] pathInfo = publicParameter.get("PATH_INFO");
         if (pathInfo != null && pathInfo.length > 0) {
            final PortletStateManager portletStateManager = stateManagerService.getPortletStateManager(request, response);
            String contentMapping = getContentMapping(portletStateManager, portletStateManager.getStateHolder());
            contentPath = assembleContentPath(contentMapping, pathInfo);
         }
      }
      if (contentPath == null) {
         // check public WCM context 
         contentPath = request.getParameter("PUBLIC_CONTEXT");
         if (contentPath == null) {
            // check content mapping
            final PortletStateManager portletStateManager = stateManagerService.getPortletStateManager(request, response);
            contentPath = getContentMapping(portletStateManager, portletStateManager.getStateHolder());
         }
      }
      return contentPath;
   }
}