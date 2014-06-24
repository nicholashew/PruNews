package com.prudential.commons.wcm.authoring;

import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.prudential.commons.wcm.authoring.PopulateSuggestedCategoriesAction.CategorySearchType;

public class TestSuggestedCategories {
	
	public static void main(String[] args) {
		// text to search
		String bodyText = "First category comes before the second category. But don't forget the other stuff about the 401k plan.";
		
		// categories to use
		String[] cats = {"First Category", "Second Category", "Forget Stuff", "Other Stuff", "401(K) Plans"};

		// print test preconditions
		System.out.println("Searching Text: \""+bodyText+"\"");
		System.out.print("For catigories: ");
		for(String cat: cats) {
			System.out.print("\""+cat+"\", ");
		}
		System.out.println();
		System.out.println();
		
		// build token indexed category map
		//Map<String, Collection<String>> categoryMap = PopulateSuggestedCategoriesAction.buildTokenIndexedCategoryMap(Arrays.asList(cats));
		Map<String, Collection<String>> categoryMap = PopulateSuggestedCategoriesAction.buildTokenIndexedCategoryMap(Arrays.asList(cats));
		
		// test all search types
		for(CategorySearchType searchType: CategorySearchType.values()) {
			
			// print search test precondition
			System.out.println("=====================================================================");
			System.out.println("Running test with search type " + searchType);
			System.out.println("=====================================================================");
			//List<String> suggestedCategoryList = PopulateSuggestedCategoriesAction.findCategoryTitles(bodyText, categoryMap, searchType);
			List<String> suggestedCategoryList = null;//PopulateSuggestedCategoriesAction.findCategoryTitles(bodyText, categoryMap, searchType);
			StringBuilder suggestedCategoryText = new StringBuilder("Categories Found: ");
			
			for(Iterator<String> itor = suggestedCategoryList.iterator(); itor.hasNext();) {
				suggestedCategoryText.append(itor.next());
				if(itor.hasNext()) {
					suggestedCategoryText.append(",");
				}
			}
			
			// print search test case postcondition
			System.out.println(suggestedCategoryText);
			System.out.println();
		}
	}
}
