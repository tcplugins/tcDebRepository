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

import org.jdom.Element;

import debrepo.teamcity.Loggers;
import jetbrains.buildServer.serverSide.settings.ProjectSettings;
import jetbrains.buildServer.serverSide.settings.ProjectSettingsManager;
import lombok.Getter;
import lombok.Setter;

public class DebRepoProjectSettingsPersister implements ProjectSettings {
	private static final String NAME = "name";
	private static final String ENABLED = "enabled";
	private static final String LOG_NAME = DebRepoProjectSettingsPersister.class.getName();
	ProjectSettingsManager psm;
	ProjectSettings ps;
	
	@Getter @Setter
	private DebRepoProjectSettings settings = new DebRepoProjectSettings();
	
	public void readFrom(Element rootElement)
    /* Is passed an Element by TC, and is expected to load it into the in memory settings object.
     * Old settings should be overwritten.
     */
    {
    	Loggers.SERVER.debug("readFrom :: " + rootElement.toString());
    	settings = new DebRepoProjectSettings();
    	
    	if (rootElement.getAttribute(ENABLED) != null){
    		settings.setRepositoryEnabled(Boolean.parseBoolean(rootElement.getAttributeValue(ENABLED)));
    	}
    	
    	if (rootElement.getAttribute(NAME) != null){
    		settings.setRepositoryName(rootElement.getAttributeValue(NAME));
    	}
    	
   }

    public void writeTo(Element parentElement)
    /* Is passed an (probably empty) Element by TC, which is expected to be populated from the settings
     * in memory. 
     */
    {
    	Loggers.SERVER.debug(LOG_NAME + ":writeTo :: " + parentElement.toString());
    	parentElement.setAttribute(NAME, String.valueOf(settings.getRepositoryName()));
    	parentElement.setAttribute(ENABLED, String.valueOf(settings.isRepositoryEnabled()));
    }

    
	public void dispose() {
		Loggers.SERVER.debug(LOG_NAME + ":dispose() called");
	}
	
	public String getRepositoryName() {
		return settings.getRepositoryName();
	}
	
	public boolean isEnabled() {
		return settings.isRepositoryEnabled();
	}

	public String isEnabledAsChecked() {
		if (settings.isRepositoryEnabled()){
			return "checked ";
		}
		return "";
	}

}
