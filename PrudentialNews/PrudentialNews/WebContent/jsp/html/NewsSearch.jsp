<%@page import="com.ibm.workplace.wcm.api.exceptions.*"%><%@page import="com.ibm.workplace.wcm.api.query.*"%><%@page import="com.ibm.workplace.wcm.api.*"%><%@page import="java.util.*"%><%@page import="java.util.logging.*"%><%@page import="org.json.*"%><%@page import="com.prudential.objects.WCMQuery"%><%@page import="com.prudential.utils.Utils"%><%@page import="com.prudential.wcm.WCMUtils"%><%@page import="com.ibm.portal.*"%><%@page contentType="text/html" pageEncoding="UTF-8"%><%!

private static final String version = "v1.4 22/08/2014 - 9:50PM";
private static final Logger s_log = Logger.getLogger(WCMUtils.class.getName());
private static Workspace wksp;
boolean isDebug = s_log.isLoggable(Level.INFO);

private String processJSONObject(JSONObject j) {
	String operator = "";
	String[] expressions = null;
	String nestedexpression = null;
	
	if (j == null) {
		return null;
	} else if ("".equals(j.toString())) {
		return null;		
	}

	Iterator<?> keys = j.keys();
	while ( keys.hasNext() ) {
		String key = (String)keys.next();
		Object val = j.get(key);
		if (isDebug) {
			s_log.log(Level.INFO, "Key: " + key, "Val: " + val.toString());			
		}
		if ("operator".equals(key)) {
			// Found an operator - get the value
			operator = (String)val;
			if (isDebug) {
				s_log.log(Level.INFO, "Operator: " + operator);
			}
		} else if ("expressions".equals(key)) {
			// Found an expression... parse
			JSONArray exp = (JSONArray)val;
			int len = exp.length();
			if (len > 0) {
				expressions = new String[len];
				for (int x = 0; x < exp.length(); x++) {
					JSONObject e = exp.getJSONObject(x);
					expressions[x] = e.getString("colval") + " " + e.getString("opval") + " '" + e.getString("val") + "'";
					if (isDebug) s_log.log(Level.INFO, "Expression: " + expressions[x]);
				}
			} else {
				// Should never get here...
			}
		} else if ("nestedexpressions".equals(key)) {
			JSONArray nexp = (JSONArray)val;
			if (nexp.length() > 0) {
				JSONObject e = nexp.getJSONObject(0);
				if (isDebug) s_log.log(Level.INFO, "Processing nestedexpression...");
				nestedexpression = processJSONObject(e);
			} else {
				nestedexpression = null;
			}
		}
	}
	String result = "";
	for (int x = 0; x < expressions.length; x++) {
		if (x > 0) result += " " + operator.toUpperCase() + " ";
		result += expressions[x];
	}
	if (nestedexpression == null) {
		if (isDebug) s_log.log(Level.INFO, "Nested expression is null.");
		return result;
	}
	String compoundresult = result + " " + operator.toUpperCase() + " ( " + nestedexpression + " ) ";
	if (isDebug) s_log.log(Level.INFO, "Returning: " + compoundresult);
	return compoundresult;
}

private Selector generateSelector(JSONObject j, JSONObject json) {
	String operator = "";
	Object[] selectors = null;
	Selector nestedCondition = null;

	if (j == null) {
		return null;
	} else if ("".equals(j.toString())) {
		return null;		
	}
	Iterator<?> keys = j.keys();
	while ( keys.hasNext() ) {
		String key = (String)keys.next();
		Object val = j.get(key);
		if ("operator".equals(key)) {
			// Found an operator - get the value
			operator = (String)val;
			if (isDebug) { System.out.println("Operator: " + val); }
		} else if ("expressions".equals(key)) {
			// Found an expression... parse
			JSONArray exp = (JSONArray)val;
			int len = exp.length();
			if (len > 0) {
				selectors = new Object[len];
				JSONArray errArray = new JSONArray();
				for (int x = 0; x < exp.length(); x++) {
					JSONObject e = exp.getJSONObject(x);
					String colval = e.getString("colval");
					String opval = e.getString("opval");
					String textval = e.getString("val");
					if (isDebug) { System.out.println("Colval: " + colval + ", opval: " + opval + ", textval: " + textval); }
					
					boolean not = false;
					if (opval.indexOf("not_") == 0) {
						not = true;
						opval = opval.substring(4, opval.length());
					}
					if (isDebug) { System.out.println("Opval: " + opval); }
					
					if ("category".equals(colval)) {
						if (isDebug) { System.out.println("Category"); }

						if ("equals".equals(opval)) {
							// Equals
							if ("category".equals(colval)) {
								if (isDebug) { System.out.println("Category equals " + textval); }

								// Find the category
								try {
									DocumentId catid = wksp.createDocumentId(textval);
									System.out.println("Adding [" + x + "]: " + catid.toString());
									selectors[x] = ProfileSelectors.categoriesContains(catid);
								} catch (DocumentIdCreationException dice) {
									System.out.println("Unable to locate ID: " + textval);
									JSONObject errObj = new JSONObject();
									errObj.put("error", "Unable to match category id: " + textval); 
									errArray.put(errObj);
								}
							}
						} else {
							System.out.println("Not supported - " + opval);
						}
					} else if ("creationdate".equals(colval) || "modifieddate".equals(colval)) {
						// HistorySelectors
					} else if ("name".equals(colval) || "title".equals(colval)) {
						// Selectors
					}
				}
				json.accumulate("errors", errArray);
				System.out.println("Errors: " + json.toString());
			} else {
				// Should never get here...
			}
		} else if ("nestedexpressions".equals(key)) {
			JSONArray nexp = (JSONArray)val;
			if (nexp.length() > 0) {
				JSONObject e = nexp.getJSONObject(0);
				if (isDebug) {
					s_log.log(Level.INFO, "Processing nestedexpression...");
				}
				nestedCondition = generateSelector(e, json);
			} else {
				nestedCondition = null;
			}
		}
	}
	Association condition;
	if ("AND".equals(operator.toUpperCase())) {
		condition = new Conjunction();
	} else {
		condition = new Disjunction();
	}
	for (int x = 0; x < selectors.length; x++) {
		System.out.println("Selector " + x + ": " + selectors[x]);
		Object sel = selectors[x];
		if (sel != null) {
			condition.add((Selector)sel);
		}
	}
	if (nestedCondition == null) {
		if (isDebug) s_log.log(Level.INFO, "Nested Selector is null.");
		if (isDebug) s_log.log(Level.INFO, "Returning: " + condition.toString());
		return condition;
	} else {
		System.out.println("Nested Selector: " + nestedCondition);
	}
	if ("AND".equals(operator.toUpperCase())) {
		Conjunction and = new Conjunction();
		and.add(condition);
		and.add(nestedCondition);
		if (isDebug) s_log.log(Level.INFO, "Returning: " + and.toString());
		return and;
	} else {
		Disjunction or = new Disjunction();
		or.add(condition);
		or.add(nestedCondition);
		if (isDebug) s_log.log(Level.INFO, "Returning: " + or.toString());
		return or;
	}
}
private String serializeSelector(Selector s) {
	String result = "";
	System.out.println(s.toString());
	if (s instanceof Conjunction) {
		Conjunction c = (Conjunction)s;
		Iterator it = c.getSelectors().iterator();
		while (it.hasNext()) {
			if (result != "") result += " " + c.getOperator().toString().toUpperCase() + " ";
			result += serializeSelector((Selector)it.next());
		}
		return "(" + result + ")";
	} else if (s instanceof Disjunction) {
		Disjunction d = (Disjunction)s;
		Iterator it = d.getSelectors().iterator();
		while (it.hasNext()) {
			if (result != "") result += " " + d.getOperator().toString().toUpperCase() + " ";
			result += serializeSelector((Selector)it.next());
		}
		return "(" + result + ")";
	} else if (s instanceof Association) {
		Association a = (Association)s;
		Iterator it = a.getSelectors().iterator();
		while (it.hasNext()) {
			if (result != "") result += a.getOperator();
			result += serializeSelector((Selector)it.next());
		}
		return "(" + result + ")";
	} else {
		return s.toString();
	}
}
private void serializeQuery(Query q) {
	String result = "";
	Iterator<Selector> it = q.getSelectors().iterator();
	int count = 0;
	while (it.hasNext()) {
		Selector sel = it.next();
		result += "[" + count + "]" + serializeSelector(sel);
		count++;
	}
	System.out.println("Query: " + result);
}
%><%
StringBuilder parsedQuery = new StringBuilder();
Query wcmQuery = null;
JSONObject json = new JSONObject();
if ("POST".equalsIgnoreCase(request.getMethod())) {
	json.put("version", version);
	
	// Only process form if posting...
	wksp = Utils.getSystemWorkspace();
	if (wksp != null) {
		wksp.login();
		String q = request.getParameter("json");
		String p = request.getParameter("sitepath");
		if (q != null) {
			// Should be in a JSON format
			JSONObject j = new JSONObject(q);
			parsedQuery.append(processJSONObject(j));
			if (isDebug) { System.out.println("Result: " + parsedQuery.toString()); }

			QueryService queryService = wksp.getQueryService();
			wcmQuery = queryService.createQuery(Content.class);

			// Generate the "Selector" condition
			Selector select = generateSelector(j, json);
			wcmQuery.addSelector(select);
			if (isDebug) { serializeQuery(wcmQuery); }

			if (json.isNull("errors")) {
				// Return the whole document (rather than the document id)
				wcmQuery.returnObjects();
				// Set the starting point for the query - we only want the News sitearea
				DocumentIdIterator saidi = wksp.findByPath(q, Workspace.WORKFLOWSTATUS_PUBLISHED);
				if (saidi.hasNext()) {
					DocumentId id = saidi.next();
					wcmQuery.addParentId(id, QueryDepth.DESCENDANTS);					
					// Generate the search...
					ResultIterator ri = queryService.execute(wcmQuery);
					JSONArray rArr = new JSONArray();
					long count = 0;
					while (ri.hasNext()) {
						// Returns a content item - based on:
						// 1) QueryService.createQuery(Content.class)
						// 2) Query.returnObjects();
						Content cont = (Content)ri.next();
						JSONObject jobj = new JSONObject();
						jobj.put("item", count);
						jobj.put("id", cont.getId());
						jobj.put("name", cont.getName());
						jobj.put("title", cont.getTitle());
						// Generate the URL to the Content item
						DocumentId parent = cont.getParentId();
						String path = cont.getName();
						while ( !(parent.isOfType(DocumentTypes.Site)) ) {
							path = parent.getName() + "/" + path;
							SiteArea sa = (SiteArea)wksp.getById(parent);
							parent = sa.getParentId();
						}
						path = parent.getName() + "/" + path;
						DocumentLibrary lib = cont.getOwnerLibrary();
						jobj.put("url", "/wps/wcm/connect/" + lib.getName() + "/" + path);
						rArr.put(jobj);
						count++;
					}
					json.put("itemcount", count);
					json.put("results", rArr);
				}
			}
		} else {
			JSONObject errObj = new JSONObject();
			errObj.put("error", "Query parameter not present. Nothing to do");
			JSONArray errArr = new JSONArray();
			errArr.put(errObj);
			json.put("errors", errArr);
		}
	} else {
		JSONObject errObj = new JSONObject();
		errObj.put("error", "ERROR - unable to retrieve Workspace. Cannot continue.");
		JSONArray errArr = new JSONArray();
		errArr.put(errObj);
		json.put("errors", errArr);
	}
}
%><%= json.toString() %>
