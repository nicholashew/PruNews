package com.prudential.commons.wcm.authoring;

import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.ibm.ivj.ejb.associations.interfaces.Link;
import com.ibm.workplace.wcm.api.extensions.authoring.AuthoringAction;

public class RegExUtil {

	/** s_log for the class */
	static Logger s_log = Logger
			.getLogger(RegExUtil.class.getName());

	public final static Pattern LINE_FEED_PATTERN = Pattern.compile("[\\n\\r]");
	public final static Pattern EXCESSIVE_WHITESPACE_PATTERN = Pattern.compile("\\s\\s+");
	public final static Pattern HTML_TAG_PATTERN = Pattern.compile("<[\\w\\W]*?>");
	
	/**
	 * Changing this pattern will impact the functionality of {@link #createOptionalCharPattern(String)}
	 * @see #createOptionalCharPattern(String)
	 */
	public final static Pattern OPTIONAL_CHARS_PATTERN = Pattern.compile("([^\\s\\r\\n\\w]|s\\b)");
	
	/**
	 * Used to further split tokens.
	 * Changing this pattern will impact the functionality of {@link #tokenizeText(String)}
	 * @see #tokenizeText(String)
	 */
	public final static Pattern TOKEN_SPLITTER_PATTERN = Pattern.compile("(?>^([0-9]+)([a-z]+)$)|(?>^([a-z]+)([0-9]+)$)|(?>^([a-z]+?)(s)$)");

	/**
	 * Creates a regex pattern where certain characters are optional.
	 * Optional characters are: all symbol and 's' at the end of a word boundary
	 * @param text
	 * @return
	 * @see #OPTIONAL_CHARS_PATTERN
	 */
	public static Pattern createOptionalCharPattern(String text) {
		StringBuilder patternStb = new StringBuilder();
		Matcher m = OPTIONAL_CHARS_PATTERN.matcher(text);
		int offset = 0;
		while(m.find()) {
			if(offset < m.start()) {
				patternStb.append(text.substring(offset, m.start()));
			}
			String grp = m.group();
			if(grp.length() > 1) {
				s_log.fine("Special Characters Pattern should only have single character groups. Group = "+grp);
			}
			if(Character.isLetterOrDigit(grp.charAt(0))) {
				patternStb.append("[");
				patternStb.append(grp);
				patternStb.append("]?");
			} else {
				patternStb.append("[\\");
				patternStb.append(grp);
				patternStb.append(" ]?");
			}
			offset = m.end();
		}
		if(offset < text.length()) {
			patternStb.append(text.substring(offset));
		}
	
		if (s_log.isLoggable(Level.FINEST)) {
			s_log.finest("Changed text from " + text + " to regex pattern: " + patternStb);
		}
		
		return Pattern.compile(patternStb.toString(), Pattern.CASE_INSENSITIVE);
	}

	/**
	 * Turns text into a set of tokens
	 * @param text
	 * @return
	 */
	public static Set<String> tokenizeText(String text) {
		boolean isDebug = s_log.isLoggable(Level.FINEST);
		List<String> tokens = new LinkedList<String>(Arrays.asList(text.toLowerCase().split("\\W")));
	
		// further process the tokens
		ListIterator<String> itor = tokens.listIterator();
		while(itor.hasNext()) {
			String token = itor.next();
			if(token.length() == 0) {
				// remove single character tokens
				itor.remove();
			} else {
				Matcher m = TOKEN_SPLITTER_PATTERN.matcher(token);
				if(m.matches()) {
					String newToken1 = null;
					String newToken2 = null;
					if(m.group(1) != null) {
						newToken1 = m.group(1);
						newToken2 = m.group(2);
					} else if(m.group(3) != null) {
						newToken1 = m.group(3);
						newToken2 = m.group(4);							
					} else if(m.group(5) != null) {
						newToken1 = m.group(5);
						newToken2 = m.group(6);
					}
	
					if (isDebug) {
						s_log.finest("Splitting token " + token + " into " + newToken1 + " and " + newToken2);
					}
	
					if(newToken1 != null && newToken1.length() > 1) {
						itor.add(newToken1);
					}
					if(newToken2 != null && newToken2.length() > 1) {
						itor.add(newToken2);
					}
				}
			}
		}
		return new HashSet<String>(tokens);
	}
}