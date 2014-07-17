 /******************************************************************
 * Copyright IBM Corp. 2010                                       *
 ******************************************************************/
package com.ibm.portal.extension;
import java.util.Map;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.namespace.QName;
import com.ibm.portal.MetaData;
import com.ibm.portal.ModelException;
import com.ibm.portal.ObjectID;
import com.ibm.portal.content.ContentMetaDataModel;
import com.ibm.portal.content.ContentModel;
import com.ibm.portal.content.ContentNode;
import com.ibm.portal.model.ContentMetaDataModelHome;
import com.ibm.portal.model.ContentModelHome;
import com.ibm.portal.services.contentmapping.exceptions.ContentMappingDataBackendException;
import com.ibm.portal.state.StateHolder;
import com.ibm.portal.state.accessors.StateAccessor;
import com.ibm.portal.state.accessors.StateAccessorFactory;
import com.ibm.portal.state.accessors.exceptions.StateNotInRequestException;
import com.ibm.portal.state.accessors.portlet.PortletAccessorFactory;
import com.ibm.portal.state.accessors.portlet.SharedStateAccessor;
import com.ibm.portal.state.exceptions.CannotInstantiateAccessorException;
import com.ibm.portal.state.exceptions.StateException;
import com.ibm.portal.state.exceptions.UnknownAccessorTypeException;
import com.ibm.portal.state.service.PortalStateManagerServiceHome;
import com.ibm.portal.state.service.StateManagerService;
import com.ibm.workplace.wcm.api.exceptions.DocumentIdCreationException;
import com.ibm.workplace.wcm.api.exceptions.DocumentRetrievalException;
import com.ibm.workplace.wcm.api.exceptions.IllegalDocumentTypeException;
import com.ibm.workplace.wcm.api.exceptions.OperationFailedException;
import com.ibm.workplace.wcm.api.exceptions.ServiceNotAvailableException;
/**
 * Helper class to determine the current WCM context. This class can only be used from portal code. 
 * From portlet code please use PorteltWCMContextHelper instead. 
 */
public class PortalWCMContextHelper extends WCMContextHelper {
   
   /** QName of the shared render parameter that holds the WCM public context */
   static final QName PUBLIC_WCM_CONTEXT_PARAM_QNAME = 
         new QName("http://www.ibm.com/xmlns/prod/datatype/content", "context");
   
   /** QName of the shared render parameter that holds the path info */
   static final QName PUBLIC_PATH_INFO_QNAME = 
         new QName("http://www.ibm.com/xmlns/prod/websphere/portal/publicparams", "path-info");
   
   /**
    * Page metadata key that controls the sharing scope for public render
    * parameter of portlets on this page
    */
   static final String PARAM_SHARING_SCOPE_KEY = "param.sharing.scope";
   private final PortalStateManagerServiceHome stateManagerServiceHome;
   private final ContentModelHome contentModelHome;
   private final ContentMetaDataModelHome contentMetaDataModelHome;
   /**
    * Initializes the PortalWCMContextHelper.  
    * 
    * @throws NamingException 
    */
   public PortalWCMContextHelper() throws NamingException {
      // this initialization needs to be done only once.
      final Context ctx = new InitialContext();
      stateManagerServiceHome = 
         (PortalStateManagerServiceHome) ctx.lookup(PortalStateManagerServiceHome.JNDI_NAME);
      contentModelHome = (ContentModelHome) ctx.lookup(ContentModelHome.JNDI_NAME);
      contentMetaDataModelHome = (ContentMetaDataModelHome) ctx.lookup(ContentMetaDataModelHome.JNDI_NAME);
   }
   
   /**
    * Gets the WCM context of the current page. It checks path info, public WCM context render parameter 
    * and content mapping in this order. A WCM context defined as private render parameter or 
    * preference of the Web Content Viewer portlet is NOT returned.
    *  
    * @param request HttpServletRequest
    * @param response HttpServletResponse
    * 
    * @return String representation of a content path.
    * 
    * @throws StateException 
    * @throws IllegalDocumentTypeException 
    * @throws DocumentRetrievalException 
    * @throws DocumentIdCreationException 
    * @throws OperationFailedException 
    * @throws ServiceNotAvailableException 
    * @throws ContentMappingDataBackendException 
    * @throws ModelException 
    */
   public String getCurrentWCMContext(final HttpServletRequest request, final HttpServletResponse response) 
         throws StateException, ContentMappingDataBackendException, ServiceNotAvailableException, 
         OperationFailedException, DocumentIdCreationException, DocumentRetrievalException, 
         IllegalDocumentTypeException, ModelException {
      String contentPath = null;
      final StateManagerService stateManagerService = 
         stateManagerServiceHome.getPortalStateManagerService(request, response);
      final StateHolder currentState = getCurrentPortalState(request, stateManagerService);
      final ContentModel<ContentNode> contentModel = 
         contentModelHome.getContentModelProvider().getContentModel(request, response);
      final ContentMetaDataModel metaDataModel = 
         contentMetaDataModelHome.getContentMetaDataModelProvider().getContentMetaDataModel(request, response);
      // find out public render parameter scope
      final ObjectID currentPage = getCurrentPageID(stateManagerService, currentState);
      final ContentNode page = contentModel.getLocator().findByID(currentPage);
      final MetaData metadata = metaDataModel.getMetaData(page);
      final Object scope = metadata.getValue(PARAM_SHARING_SCOPE_KEY);
      String publicRenderScope;
      if (scope != null) {
         publicRenderScope = scope.toString();
      } else {
         publicRenderScope = SharedStateAccessor.KEY_GLOBAL_PUBLIC_RENDER_PARAMETERS;
      }
   
      // load public render parameter
      final PortletAccessorFactory portletAccFct = 
         stateManagerService.getAccessorFactory(PortletAccessorFactory.class);
      final SharedStateAccessor sharedStateAcc = 
         portletAccFct.getSharedStateAccessor(publicRenderScope, currentState);
      if (sharedStateAcc != null) {
         try {
            final Map<QName, String[]> sharedRenderParams = sharedStateAcc.getParameters();
            // check path info first.
            if (sharedRenderParams.containsKey(PUBLIC_PATH_INFO_QNAME)) {
               final String[] pathInfo = sharedRenderParams.get(PUBLIC_PATH_INFO_QNAME);
               if (pathInfo != null && pathInfo.length > 0) {
                  String contentMapping = getContentMapping(stateManagerService, currentState);
                  contentPath = assembleContentPath(contentMapping, pathInfo);
               }
            }
            // if there is no path info check the public WCM context.
            if (contentPath == null && sharedRenderParams.containsKey(PUBLIC_WCM_CONTEXT_PARAM_QNAME)) {
               final String[] values = sharedRenderParams.get(PUBLIC_WCM_CONTEXT_PARAM_QNAME);
               if (values != null && values.length > 0) {
                  contentPath = values[0];
               }
            }
         } finally {
            sharedStateAcc.dispose();
         }
      }
      if (contentPath == null) {
         // check for a content mapping
         contentPath = getContentMapping(stateManagerService, currentState);
      }
      return contentPath;
   }
   
   /**
    * Gets the current portal state.
    *  
    * @param request HttpServletRequest
    * @param stateManagerService StateManagerService, entry point to the portal state API
    * 
    * @return the current portal state
    * 
    * @throws UnknownAccessorTypeException
    * @throws CannotInstantiateAccessorException
    * @throws StateNotInRequestException
    */
   private StateHolder getCurrentPortalState(final HttpServletRequest request, 
         final StateManagerService stateManagerService) throws UnknownAccessorTypeException, 
         CannotInstantiateAccessorException, StateNotInRequestException {
      StateHolder result = null;
      final StateAccessorFactory stateAccFac = 
         (StateAccessorFactory) stateManagerService.getAccessorFactory(StateAccessorFactory.class);
      final StateAccessor stateAcc = stateAccFac.getStateAccessor();
      try {
         result = stateAcc.getStateHolder(request);
      } finally {
         stateAcc.dispose();
      }
      return result;
   }
}