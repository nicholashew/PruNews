
<%
   /********************************************************************/
   /* Licensed Materials - Property of IBM */
   /* (c) Copyright IBM Corp. 2011. All rights reserved. */
   /* */
   /* US Government Users Restricted Rights - Use, duplication or */
   /* disclosure restricted by GSA ADP Schedule Contract with IBM */
   /* Corp. */
   /* */
   /* DISCLAIMER OF WARRANTIES. The following [enclosed] code is */
   /* sample code created by IBM Corporation. This sample code is */
   /* not part of any standard or IBM product and is provided to you */
   /* solely for the purpose of assisting you in the development of */
   /* your applications. The code is provided "AS IS", without */
   /* warranty of any kind. IBM shall not be liable for any damages */
   /* arising out of your use of the sample code, even if they have */
   /* been advised of the possibility of such damages. */
   /********************************************************************/
%><%@ page session="false" isELIgnored="false"%><%@ page
	import="java.util.*,java.io.*,java.lang.*,com.prudential.mypru.common.rule.*,com.ibm.workplace.wcm.api.*"%><%@ taglib
	uri="/WEB-INF/tld/wcm.tld" prefix="wcm"%>
<%@ taglib uri="/WEB-INF/tld/std-portlet.tld" prefix="portlet"%>
<portlet:defineObjects />
<%--
  Render a component in selected or current context
  --------------------------------------------------
  
  Expects the current content item to specify:
  
  - Component reference  
    Mandatory, specifies the component to be rendered
  
  - Override Context
    Optional, if not supplied, the current page context is used
    Drives both the setting of a context for rendering, and is  also passed along in a request param
  
  - Templates
    Optional list of templates
    Will be set into request params in two formats, one suitable for menus, one for Pzn rules
  
  - Categories
    Optional, pulled from the standard categories field
    Will be set into request params in two formats, one suitable for menus, one for Pzn rules
  
  After setting up the context and request params, the specific component is rendered.
  
  Note that from this point on in the rendering pipeline, WCM will act as though the context set up in this JSP is the "current" context.
  - To retrieve elements/properties from the original portlet context the "portletContext" context option can be used
  - To retrieve elements/properties from the original page context, even if this overriden in this code, the "portalContext" option can be used
--%><%!/*
                   * declarations
                   */
   static final String PARAM_CMPNT_REFERENCE = "CmpntElement";

   static final String PARAM_CONTEXT_OVERRIDE = "ContextElement";

   static final String ELEMENT_TEMPLATES = "List Templates";

   static final String PARAM_SITEAREAPATH = "siteAreaPath";

   static final String PARAM_TEMPLATES = "templates";

   static final String PARAM_CATEGORIES = "categories";

   static final String PARAM_SEARCH = "query";

   static final String PARAM_SEARCH_QUERY = "search_query";

   static final String SEARCH_FILTER_FLAG = "searchFilter";

   static final String LIST_PRES = "List Presentation Component";

   static final String PARAM_FORMATTER = "FORMATTERID";

   static final String PARAM_IS_DRAFT = "isdraft";

   static final String PARAM_CONTENT_ID = "id";

   static final String RULES_CONTAINER = "Personalization Rules";%><wcm:initworkspace
	user="<%= request.getUserPrincipal() %>">login fail</wcm:initworkspace>
<%
   /*
   * Get context, content, workspace
   */
   RenderingContext rc = (RenderingContext) request.getAttribute(Workspace.WCM_RENDERINGCONTEXT_KEY);
   Content content = rc.getContent();
   Workspace ws = content.getSourceWorkspace();
   Map params = rc.getRequestParameters();
   String path = ws.getPathById(content.getId(), false, false);

   String attrFilterFlag = SEARCH_FILTER_FLAG + path;
   String sesAttrFilterQuery = PARAM_SEARCH_QUERY + path;

   /*
    * Get cmpnt reference from named element
    * If not found then don't do any more processin - no component to render!
    */
   String cmpntRefElement = request.getParameter(PARAM_CMPNT_REFERENCE);
   if (cmpntRefElement != null && content.hasComponent(cmpntRefElement)) {
      ReferenceComponent theCmpnt = (ReferenceComponent) content.getComponentByReference(cmpntRefElement);
      if (theCmpnt != null && theCmpnt.getComponentRef() != null) {
         try {
            /*
            * Check for context override - this will override the default of using the page context
            */
            String sitePathStr = null;
            String contextElement = request.getParameter(PARAM_CONTEXT_OVERRIDE);
            //out.println("<p>SDD: contextElement: "+contextElement+"</p>");
            if (contextElement != null && content.hasComponent(contextElement)) {
               //out.println("<p>SDD: found parameter and content has element"+"</p>");
               LinkComponent contextSelectedCmpnt = (LinkComponent) content.getComponentByReference(contextElement);
               if (contextSelectedCmpnt != null) {
                  //out.println("<p>SDD: found link element"+"</p>");
                  if (contextSelectedCmpnt.getLinkType() == LinkComponent.TYPE_MANAGEDCONTENT) {
                     //out.println("<p>SDD: link element points to managed content"+"</p>");
                     DocumentId docId = contextSelectedCmpnt.getDocumentReference();
                     if (docId != null
                        && (docId.isOfType(DocumentTypes.Site) || docId.isOfType(DocumentTypes.SiteArea) || docId
                           .isOfType(DocumentTypes.Content))) {
                        //out.println("<p>SDD: doc is appropriate type"+"</p>");
                        sitePathStr = "/" + ws.getPathById(docId, true, false);
                        sitePathStr = sitePathStr.replace('+', ' ');
                        //out.println("<p>SDD: sitePathStr="+sitePathStr+"</p>");
                     }
                     else {
                        out.println("Selected link for context must be site, site area or content");
                     }
                  }
               }
            }

            /*
            * If context was found in element, set it as the context, otherwise set the current page context
            * Also put the context path in request param/attribute for use in rules/menus
            */
            if (sitePathStr != null) {
               //out.println("<p>SDD: setting rendered content to: "+sitePathStr+"</p>");
               rc.setRenderedContent(sitePathStr);
            }
            else {
               //out.println("<p>SDD: Using portalContext instead...</p>");
%><wcm:setContext location="portalContext" param="none" />
<%
   rc = (RenderingContext) request.getAttribute(Workspace.WCM_RENDERINGCONTEXT_KEY);
            }
            params.put(PARAM_SITEAREAPATH, rc.getPath());
            request.setAttribute(PARAM_SITEAREAPATH, rc.getPath());

            /*
            * If no content mapping is defined, render current page
            */
            /*if (rc.getContent() == null)
            {
            //out.println("<p>SDD: content mapping is null, setting rendered content to "+path+"...</p>");
            rc.setRenderedContent(path);
            }*/

            /*
            * Look for categories. If found, put in request param/attribute
            * Note that these categories could have come from option selection elements, but they should be set to copy into the standard categories field
            */
            //out.println("<p>SDD: checking categories...</p>");
            DocumentId[] categories = content.getCategoryIds();
            if (categories.length > 0) {
               String categoriesStr = "";
               for (int i = 0; i < categories.length; i++) {
                  Category category = (Category) ws.getById(categories[i], true);
                  categoriesStr += (i > 0 ? "," : "") + category.getOwnerLibrary().getName() + "/" + category.getName();
               }
               //out.println("<p>SDD: setting categories to "+categoriesStr+"...</p>");
               params.put(PARAM_CATEGORIES, categoriesStr);
               request.setAttribute(PARAM_CATEGORIES, categoriesStr);
            }

            /*
            * Look for templates field. If found and not empty, put in request param/attribute
            */
            //out.println("<p>SDD: checking templates...</p>");
            if (content.hasComponent(ELEMENT_TEMPLATES)) {
               ShortTextComponent templatesCmpnt = (ShortTextComponent) content.getComponentByReference(ELEMENT_TEMPLATES);
               if (templatesCmpnt != null) {
                  String templatesStr = templatesCmpnt.getText().trim();
                  //out.println("<p>SDD: setting templates to "+templatesStr+"...</p>");
                  params.put(PARAM_TEMPLATES, templatesStr);
                  request.setAttribute(PARAM_TEMPLATES, templatesStr);
               }
            }

            /*
            * Look for list presentation component and override if necessary
            */
            //out.println("<p>SDD: checking for list presentation...</p>");
            if (content.hasComponent(LIST_PRES)) {
               ReferenceComponent presCmpnt = (ReferenceComponent) content.getComponentByReference(LIST_PRES);

               if (presCmpnt != null) {
                  if (presCmpnt.getComponentRef() != null) {
                     //out.println("<p>SDD: Setting list presentation...</p>");
                     params.put(PARAM_FORMATTER, presCmpnt.getComponentRef().getId().getId());
                  }
               }
            }

            /*
            * Render cmpnt in context
            */
            //out.println("<p>SDD: setting request parameters to "+params+"...</p>");
            rc.setRequestParameters(params);
            //out.println("<p>SDD: rendering component in context...</p>");
            String result = ws.render(rc, theCmpnt.getComponentRef());
            /* 
            * CMK - this is where we will get the filtered stuff
            *
            */
            // 1) check the content for the Personalization Rules component
            if (content.hasComponent(RULES_CONTAINER)) {
               ShortTextComponent stc = (ShortTextComponent) content.getComponentByReference(RULES_CONTAINER);
               String ruleNames = stc.getText();
               //out.println("Rules are: " + ruleNames + "<br>");
               if (ruleNames.length() > 0) {
                  // here's where we get the tags
                  List<String> rules = Arrays.asList(ruleNames.split(","));
                  List<List<String>> tags = RuleInvocation.executeRules(renderRequest, rules);
                  //out.println("returning tags "+tags);
                  // we will for loop for every item in the result set
                  if (tags.size() > 0) {
                     StringBuilder sb = new StringBuilder();
                     //out.println("CMK Result is " + result);
                     String[] splitString = result.split("CMK_REPLACE_HEAD");
                     sb.append(splitString[0]);
                     if (splitString.length > 2) {
                        for (int y = 1; y < splitString.length; y++) {
                           String tempString = splitString[y];
                           //out.println("CMK y = " + y + " tempString = " + tempString + "<br>");
                           int endDivIndex = tempString.indexOf("/div");
                           tempString = tempString.substring(0, endDivIndex);
                           //out.println("CMK y = " + y + " tempString after substring= " + tempString + "<br>");
                           for (List<String> rule : tags) {
                              for (String tag : rule) {
                                 if (tempString.toLowerCase().contains(tag.toLowerCase())) {                                    
                                    sb.append(splitString[y]);
                                    // need to put in from CMK_REPLACE_HEAD to CMK_REPLACE_FOOT not from the head out to the end
                                 }
                              } // end for	        
                           } // end for

                        } // end for
                        String endString = splitString[splitString.length - 1];
                        int lastFoot = endString.lastIndexOf("CMK_REPLACE_FOOT");
                        sb.append(endString.substring(lastFoot));
                        result = sb.toString();
                        result = result.replaceAll("CMK_REPLACE_HEAD", "");
                        result = result.replaceAll("CMK_REPLACE_FOOT", "");
                     } // end if

                  } // end if
               }
            }
            else {
               out.println("Did not contain rules<br>");
            }
            result = result.trim();
            //out.println("<p>SDD: writing "+result+"</p>");
            out.print(result);

            rc.setRenderedContent(path);
         }
         catch (Exception e) {
            //out.println("<p>SDD: exception: "+e.getMessage()+"!</p>");
            e.printStackTrace();
         }
      }
   }
%>