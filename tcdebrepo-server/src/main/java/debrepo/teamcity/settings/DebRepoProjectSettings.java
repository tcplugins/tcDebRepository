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
package debrepo.teamcity.settings;

import lombok.Getter;
import lombok.Setter;

public class DebRepoProjectSettings {
	
	public final static String PROJECT_SETTINGS_KEY = "deb-repository";

	@Getter 
	@Setter
	private String repositoryName = "";
	
	@Getter
	@Setter
	private boolean repositoryEnabled = true;

	public String isEnabledAsChecked() {
		if (this.repositoryEnabled){
			return "checked ";
		}
		return "";
	}

}
