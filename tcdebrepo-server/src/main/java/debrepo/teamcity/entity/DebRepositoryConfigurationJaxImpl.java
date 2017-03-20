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

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;

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
@XmlType(propOrder = { "repoName", "projectId", "restricted", "uuid", "buildTypes", "architecturesRepresentedByAll" })
public class DebRepositoryConfigurationJaxImpl implements DebRepositoryConfiguration {
	
	private static final String[] defaultArchitecturesForAll = { "arm64", "amd64", "armel", "amd64", "armhf", 
																 "i386", "mips", "mipsel", "powerpc", "ppc64el", "s390x" };
	
	private static final Set<String> defaultArchitecturesForAllSet = new TreeSet<>();
	
	@NotNull @XmlAttribute(name="uuid")
	private UUID uuid;
	
	@NotNull @XmlAttribute(name="project-id")
	private String projectId;
	
	@NotNull @XmlAttribute(name="repository-name")
	private String repoName;
	
	@XmlAttribute(name="restricted")
	private boolean restricted = false;
	
	@XmlElement(name="build-type") @XmlElementWrapper(name="build-types")
	private List<DebRepositoryBuildTypeConfig> buildTypes = new CopyOnWriteArrayList<>();
	
	@XmlElement(name="arch") @XmlElementWrapper(name="architectures-represented-by-all")
	private Set<String> architecturesRepresentedByAll = new TreeSet<>();
	
	public DebRepositoryConfigurationJaxImpl(String projectId, String repoName) {
		this.projectId = projectId;
		this.repoName = repoName;
		this.uuid = UUID.randomUUID();
		for (String arch : defaultArchitecturesForAll) {
			architecturesRepresentedByAll.add(arch);
		}
	}

	public boolean addBuildType(DebRepositoryBuildTypeConfig buildType) {
		return buildTypes.add(buildType);
	}
	
	public boolean containsBuildType(String buildTypeId) {
		for (DebRepositoryBuildTypeConfig config : this.buildTypes) {
			if (buildTypeId.equals(config.getBuildTypeId())) {
				return true;
			}
		}
		return false;
	}

	@Override
	public int compareTo(DebRepositoryConfiguration o) {
		return this.getUuid().compareTo(o.getUuid());
	}

	@Override
	public Set<String> getDefaultArchitecturesRepresentedByAll() {
		if (defaultArchitecturesForAllSet.isEmpty()) {
			Collections.addAll(defaultArchitecturesForAllSet, defaultArchitecturesForAll);
		}
		return defaultArchitecturesForAllSet;
	}
	
}