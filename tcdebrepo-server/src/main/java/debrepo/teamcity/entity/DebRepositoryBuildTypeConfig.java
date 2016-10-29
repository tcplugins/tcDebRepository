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

import java.util.Set;
import java.util.TreeSet;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

import org.jetbrains.annotations.NotNull;

import lombok.Data;
import lombok.NoArgsConstructor;

/* Use the XmlAttributes on the fields rather than the getters
 * and setters provided by Lombok */
@XmlAccessorType(XmlAccessType.FIELD)

@NoArgsConstructor
@Data
@XmlRootElement() 
public class DebRepositoryBuildTypeConfig {
	@NotNull @XmlAttribute(name="build-type-id")
	private String buildTypeId;
	
	@XmlElement(name="filter") @XmlElementWrapper(name="build-artifact-filters")
	private Set<String> debFilters = new TreeSet<>();
	
	public DebRepositoryBuildTypeConfig(String strBuildTypeId){
		this.buildTypeId = strBuildTypeId;
	}
	
	public DebRepositoryBuildTypeConfig(String strBuildTypeId, String filter){
		this.buildTypeId = strBuildTypeId;
		this.debFilters.add(filter);
	}
	
	public boolean addFilter(String filter) {
		return this.debFilters.add(filter);
	}
	
	public boolean removeFilter(String filter) {
		return this.debFilters.remove(filter);
	}
	
	/**
	 * Add a filter and returns the object for chaining.
	 * @param filter
	 * @return this instance of DebRepositoryBuildTypeConfig
	 */
	public DebRepositoryBuildTypeConfig af(String filter) {
		this.addFilter(filter);
		return this;
	}

}