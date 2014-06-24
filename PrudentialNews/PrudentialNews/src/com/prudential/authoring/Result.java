/*
* To change this license header, choose License Headers in Project Properties.
* To change this template file, choose Tools | Templates
* and open the template in the editor.
*/

package com.prudential.authoring;

import com.ibm.portal.*;
import com.ibm.workplace.wcm.api.extensions.authoring.*;
import com.ibm.workplace.wcm.api.extensions.authoring.directive.*;
import java.util.*;
import java.util.logging.Logger;

/**
*
* @author Pete Raleigh
*/
public class Result implements ActionResult {

	/** Logger for the class */
	private static Logger s_log = Logger.getLogger(Result.class.getName());
	
	public enum ResultStatus {
		SUCCESS, WARNING, ERROR;
	}

	private ResultStatus result;
	private String title;
	private String description;
	private static final ListModel<Locale> ENGLISH_ONLY = new SimpleLocaleListModel<Locale>(new Locale[]{Locale.ENGLISH});

	/**
	* A simple list model holding locales.
	*/
	protected static class SimpleLocaleListModel<K> implements ListModel<Locale> {
		/** the list of locales of this list model */
		final List<Locale> m_localeList = new ArrayList<Locale>();

		/**
		 * Constructs this simple list model holding the given locales.
		 * 
		 * @param locales
		 *           the locales of this list model. May be <code>null</code>.
		 */
		public SimpleLocaleListModel(final Locale[] p_locales) {
			if (p_locales != null) {
				for (int i = 0; i < p_locales.length; ++i) {
					m_localeList.add(p_locales[i]);
				}
			}
		}

		/*
		* (non-Javadoc) 
		* @see com.ibm.portal.ListModel#iterator()
		*/
		@Override
		public Iterator<Locale> iterator() throws ModelException {
			return m_localeList.iterator();
		}
	}

	public Result (ResultStatus result) {
		this.result = result;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	
	@Override
	public AuthoringDirective directive() {
		// TODO Auto-generated method stub
		return new Continue();
	}

	@Override
	public Localized successMessage() {
		if (result != ResultStatus.SUCCESS) return null;
		return new Localized()
		{
			@Override
			public String getTitle(Locale locale) {
				return "OK: " + title;
			}
			public ListModel<Locale> getLocales() {
				return ENGLISH_ONLY;
			}
			@Override
			public String getDescription(Locale locale) {
				return 	description;
			}
		};
	}

	@Override
	public Localized errorMessage() {
		if (result != ResultStatus.ERROR) return null;
		return new Localized()
		{
			@Override
			public String getTitle(Locale locale) {
				return "ERROR: " + title;
			}
			public ListModel<Locale> getLocales() {
				return ENGLISH_ONLY;
			}
			@Override
			public String getDescription(Locale locale) {
				return 	description;
			}
		};
	}

	@Override
	public Localized warningMessage() {
		if (result != ResultStatus.WARNING) return null;
		return new Localized()
		{
			@Override
			public String getTitle(Locale locale) {
				return "WARNING: " + title;
			}
			public ListModel<Locale> getLocales() {
				return ENGLISH_ONLY;
			}
			@Override
			public String getDescription(Locale locale) {
				return 	description;
			}
		};
	}
}
