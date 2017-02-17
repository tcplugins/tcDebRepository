	/*******************************************************************************
 *
 *  Copyright 2016, 2017 Net Wolf UK
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

package debrepo.teamcity.web;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.jetbrains.annotations.NotNull;

import debrepo.teamcity.entity.DebRepositoryBuildTypeConfig;
import debrepo.teamcity.entity.DebRepositoryBuildTypeConfig.Filter;
import debrepo.teamcity.entity.DebRepositoryConfiguration;
import jetbrains.buildServer.serverSide.ProjectManager;
import jetbrains.buildServer.serverSide.SBuildType;
import jetbrains.buildServer.serverSide.SProject;
import jetbrains.buildServer.web.util.CameFromSupport;
import lombok.Getter;
import lombok.Setter;
import lombok.Value;

public class EditDebRepositoryBean {

	@Getter	@Setter
	private String name;
	
	@Getter	@Setter
	private String uuid;
	
	@Getter	@Setter
	private SProject project;
	
	@Getter	@Setter
	private String editAction = "create";
	
	@Getter @Setter 
	private Map<BuildTypeData, List<FilterAndBuildType>> filtersAndBuildTypes = new TreeMap<>();
	
	@Getter @Setter 
	private Set<String> allArchitectures;
	
	@Getter
	private final CameFromSupport cameFromSupport = new CameFromSupport();

	@Getter
	private Set<String> defaultAllArchitectures;
	
	public EditDebRepositoryBean() {
		name = "";
		uuid = "";
		editAction = "create";
	}

	public EditDebRepositoryBean(@NotNull final DebRepositoryConfiguration repoConfig, @NotNull SProject sproject) {
		name = repoConfig.getRepoName();
		uuid = repoConfig.getUuid().toString();
		project = sproject;
		editAction = "update";
	}
	
	public String getAllArchsAsCSL() {
		if (allArchitectures.isEmpty()) {
			return "None";
		}
		StringBuilder sb = new StringBuilder();
		for (String s : allArchitectures) {
			sb.append(", ").append(s);
		}
		return sb.toString().substring(2);
	}
	
	@Value
	public static class BuildTypeData implements Comparable<BuildTypeData>{
		String buildTypeId;
		String buildTypeName;
		String projectId;
		String projectName;
		String fullName;
		
		@Override
		public int compareTo(BuildTypeData o) {
			return fullName.compareTo(o.fullName); 
		}
	}
	
	@Value
	public static class FilterAndBuildType {
		String buildTypeId;
		Filter filter;
	}
	
	@Value
	public static class Architecture {
		String arch;
		boolean isEnabled;
	}
	
	public static EditDebRepositoryBean build(@NotNull ProjectManager projectManager, @NotNull final DebRepositoryConfiguration repoConfig, @NotNull SProject sproject) {
		EditDebRepositoryBean bean = new EditDebRepositoryBean(repoConfig, sproject);
		for (DebRepositoryBuildTypeConfig btConfig : repoConfig.getBuildTypes()) {
			SBuildType sBuildType = projectManager.findBuildTypeById(btConfig.getBuildTypeId());
			if (sBuildType != null) {
				List<FilterAndBuildType> filterAndBuildTypes = new ArrayList<>();
				for (Filter f : btConfig.getDebFilters()) {
					filterAndBuildTypes.add(new FilterAndBuildType(sBuildType.getBuildTypeId(), f));
				}
				bean.filtersAndBuildTypes.put(new BuildTypeData(sBuildType.getExternalId(), 
																sBuildType.getName(), 
																sBuildType.getProjectExternalId(), 
																sBuildType.getProjectName(), 
																sBuildType.getFullName()
																), 
												filterAndBuildTypes);
			}
		}
		bean.defaultAllArchitectures = repoConfig.getDefaultArchitecturesRepresentedByAll();
		bean.allArchitectures = repoConfig.getArchitecturesRepresentedByAll();
		return bean;
	}
	
	public List<Architecture> getAllArchitectureList() {
		List<Architecture> archs = new ArrayList<>(); 
		for (String arch : defaultAllArchitectures) {
			archs.add(new Architecture(arch, allArchitectures.contains(arch)));
		}
		return archs;
	}

}