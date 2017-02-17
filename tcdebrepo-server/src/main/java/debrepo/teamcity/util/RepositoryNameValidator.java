/*******************************************************************************
 * Copyright 2017 Net Wolf UK
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

import java.util.regex.Pattern;

import lombok.AllArgsConstructor;
import lombok.Data;

public class RepositoryNameValidator {
	
	public RepositoryNameValidationResult nameIsURlSafe(String repoName) {
		if ( ! Pattern.matches("^[A-Za-z0-9_-]+$", repoName) ) {
			   return new RepositoryNameValidationResult(true, "Please use A-Za-z0-9_- in Debian Repository Names");
		}
		return new RepositoryNameValidationResult(false, "Looks good");
	}
	
	@Data @AllArgsConstructor
	public class RepositoryNameValidationResult {
		boolean error;
		String reason;
	}

}
