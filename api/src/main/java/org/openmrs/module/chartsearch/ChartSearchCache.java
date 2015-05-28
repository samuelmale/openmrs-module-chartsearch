/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.chartsearch;

import java.util.Date;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.openmrs.api.context.Context;
import org.openmrs.module.chartsearch.api.ChartSearchService;

import com.openmrs.module.chartsearch.saving.ChartSearchHistory;

/**
 * This basically provides access to chart-search module stored records such as notes on searches,
 * bookmarked searches, search history, search suggestions among others from the database emulating
 * the normal cache/buffer
 */
public class ChartSearchCache {
	
	//addHistory
	private static final Logger logger = Logger.getLogger(ChartSearchCache.class);
	
	private ChartSearchService chartSearchService = getComponent(ChartSearchService.class);
	
	public void saveOrUpdateSearchHistory(String searchText, Integer patientId) {
		ChartSearchHistory history = new ChartSearchHistory();
		ChartSearchHistory exisitingHistory = checkIfSearchIsAlreadyInHistory(searchText);
		
		if (StringUtils.isNotBlank(searchText) && null != patientId) {
			history.setHistoryOwner(Context.getAuthenticatedUser());
			history.setPatient(Context.getPatientService().getPatient(patientId));
			history.setSearchPhrase(searchText);
			
			if (null != exisitingHistory) {
				history = exisitingHistory;
			}
			history.setLastSearchedAt(new Date());//Date was duplicated, probably use Calendar
			
			chartSearchService.saveSearchHistory(history);
		}
	}
	
	private ChartSearchHistory checkIfSearchIsAlreadyInHistory(String searchText) {
		List<ChartSearchHistory> allHistory = chartSearchService.getAllSearchesInHistory();
		
		if (!allHistory.isEmpty()) {
			for (int i = 0; i < allHistory.size(); i++) {
				ChartSearchHistory history = allHistory.get(i);
				if (searchText.equals(history.getSearchPhrase())) {
					return history;
				}
			}
		}
		return null;
	}
	
	private <T> T getComponent(Class<T> clazz) {
		List<T> list = Context.getRegisteredComponents(clazz);
		if (list == null || list.size() == 0)
			throw new RuntimeException("Cannot find component of " + clazz);
		return list.get(0);
	}
}
