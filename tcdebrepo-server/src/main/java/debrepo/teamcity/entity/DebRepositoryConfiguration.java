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
package debrepo.teamcity.entity;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import org.jetbrains.annotations.NotNull;

import lombok.Data;
import lombok.NoArgsConstructor;

/* Use the XmlAttributes on the fields rather than the getters
 * and setters provided by Lombok */
@XmlAccessorType(XmlAccessType.FIELD)

@Data  // Let Lombok generate the getters and setters.
@NoArgsConstructor // Empty constructor for JAXB.

@XmlRootElement()
@XmlType(propOrder = { "repoName", "projectId", "uuid", "buildTypes" })
public class DebRepositoryConfiguration {
	
	@NotNull @XmlAttribute(name="uuid")
	private UUID uuid;
	
	@NotNull @XmlAttribute(name="project-id")
	private String projectId;
	
	@NotNull @XmlAttribute(name="repository-name")
	private String repoName;
	
	@XmlElement(name="build-type") @XmlElementWrapper(name="build-types")
	private List<DebRepositoryBuildTypeConfig> buildTypes = new ArrayList<>();
	
	public DebRepositoryConfiguration(String projectId, String repoName) {
		this.projectId = projectId;
		this.repoName = repoName;
		this.uuid = UUID.randomUUID();
	}

	public boolean addBuildType(DebRepositoryBuildTypeConfig buildType) {
		return buildTypes.add(buildType);
	}
	
	public boolean addBuildType(String buildTypeId, String dist, String component) {
		return buildTypes.add(new DebRepositoryBuildTypeConfig(buildTypeId, dist, component, ".+\\.deb"));
	}
	
	public boolean containsBuildType(String buildTypeId) {
		for (DebRepositoryBuildTypeConfig config : this.buildTypes) {
			if (buildTypeId.equals(config.getBuildTypeId())) {
				return true;
			}
		}
		return false;
	}

	public boolean containsBuildTypeAndFilter(DebPackageEntity entity) {
//		for (DebRepositoryBuildTypeConfig config : this.buildTypes) {
//			if (entity.getSBuildTypeId().equals(config.getBuildTypeId()) &&
//				config.matchAgainstFilter(entity.getFilename())) {
//					return true;
//			}
//		}
		return false;
	}


}