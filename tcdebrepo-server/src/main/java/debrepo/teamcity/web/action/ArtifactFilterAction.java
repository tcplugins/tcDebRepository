/*******************************************************************************
 *
 *  Copyright 2016 Net Wolf UK
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *  
 *  
 *******************************************************************************/
package debrepo.teamcity.web.action;

import static debrepo.teamcity.web.DebRepoConfigurationEditPageActionController.*;

import javax.servlet.http.HttpServletRequest;

import org.jetbrains.annotations.NotNull;

import debrepo.teamcity.entity.DebRepositoryBuildTypeConfig.Filter;
import jetbrains.buildServer.util.StringUtil;

public abstract class ArtifactFilterAction {

	public ArtifactFilterAction() {
		super();
	}

	protected Filter getFilterFromRequest(@NotNull final HttpServletRequest request) throws IncompleteFilterException {
		
		String id = StringUtil.nullIfEmpty(request.getParameter(DEBREPO_FILTER_ID));
		
		String regex = getParameterAsStringOrNull(request, DEBREPO_FILTER_REGEX, "The Regex field must not be empty");
		String dist = getParameterAsStringOrNull(request, DEBREPO_FILTER_DIST, "The dist field must not be empty");
		String component = getParameterAsStringOrNull(request, DEBREPO_FILTER_COMPONENT, "The component field must not be empty");
		
		if (id == null || "_new".equals(id) || "_copy".equals(id)){
			return new Filter(regex, dist, component);
		}
	
		return new Filter(id, regex, dist, component);
	}
	
	public String getParameterAsStringOrNull(HttpServletRequest request, String paramName, String errorMessage) throws IncompleteFilterException {
		String returnValue = StringUtil.nullIfEmpty(request.getParameter(paramName));
		if (returnValue == null || "".equals(returnValue.trim())) {
			throw new IncompleteFilterException(errorMessage);
		}
		return returnValue;
	}
	
	public abstract String getFilterAction();

	public boolean canProcess(@NotNull HttpServletRequest request) {
		return getFilterAction().equals(request.getParameter(ACTION_TYPE));
	}
	
	@SuppressWarnings("serial")
	public class IncompleteFilterException extends Exception {
		public IncompleteFilterException(String message) {
			super(message);
		}
	}
	
	@SuppressWarnings("serial")
	public class DuplicateFilterException extends Exception {
		public DuplicateFilterException(String message) {
			super(message);
		}
	}

}