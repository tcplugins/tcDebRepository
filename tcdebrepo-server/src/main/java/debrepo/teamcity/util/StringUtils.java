/*******************************************************************************
 * Copyright 2016 Net Wolf UK
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/
package debrepo.teamcity.util;

import debrepo.teamcity.web.DebDownloadController;

public class StringUtils {
	
	// Utility class. Prevent instantiation.
	private StringUtils() {}
	
    public static String stripTrailingSlash(String stringWithPossibleTrailingSlash){
    	if (stringWithPossibleTrailingSlash.endsWith("/")){
    		return stringWithPossibleTrailingSlash.substring(0, stringWithPossibleTrailingSlash.length()-1);
    	}
    	return stringWithPossibleTrailingSlash;
    	
    }

	public static String getDebRepoUrl(String rootUrl, String repositoryName, boolean isRestricted) {
		if (isRestricted) {
			return stripTrailingSlash(rootUrl) + DebDownloadController.DEBREPO_BASE_URL_FOR_REDIRECT_RESTRICTED + "/" + repositoryName + "/";
		}
		return stripTrailingSlash(rootUrl) + DebDownloadController.DEBREPO_BASE_URL_FOR_REDIRECT_UNRESTRICTED + "/" + repositoryName + "/";
	}
	
	public static String getDebRepoUrlWithUserPassExample(String rootUrl, String repositoryName, boolean isRestricted) {
		if (isRestricted) {
			return getDebRepoUrl(rootUrl, repositoryName, isRestricted)
					.replaceFirst("(^https?://)", "$1<em><strong>username:password</strong></em>@");
		}
		return getDebRepoUrl(rootUrl, repositoryName, isRestricted); 
		
	}
	
}
