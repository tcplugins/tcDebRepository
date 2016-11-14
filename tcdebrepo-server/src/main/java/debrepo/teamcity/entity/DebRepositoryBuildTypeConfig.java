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
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Pattern;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

import org.jetbrains.annotations.NotNull;

import lombok.AllArgsConstructor;
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
	private Set<Filter> debFilters = new TreeSet<>();
	
	/**
	 * dist is the debian distribution name, eg versions like "wheezy", "squeeze", or symbolic names like "stable", "testing", "unstable" 
	 */
//	@NotNull @XmlAttribute(name="dist-name")
//	private String dist;
	
	/** 
	 * Component is the debian component, eg "main", "contrib", "non-free"
	 */
//	@NotNull @XmlAttribute(name="component-name")
//	private String component;
	
	public DebRepositoryBuildTypeConfig(String strBuildTypeId){
		this.buildTypeId = strBuildTypeId;
//		this.dist = dist;
//		this.component = component;		
	}
	
	public DebRepositoryBuildTypeConfig(String strBuildTypeId, String dist, String component, String filter){
		this.buildTypeId = strBuildTypeId;
//		this.dist = dist;
//		this.component = component;
		this.debFilters.add(new Filter(filter, dist, component));
	}
	
	public boolean addFilter(Filter filter) {
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
	public DebRepositoryBuildTypeConfig af(Filter filter) {
		this.addFilter(filter);
		return this;
	}

	public List<Filter> matchAgainstFilter(String filename) {
		List<Filter> matchingFilters = new ArrayList<>();
		for (Filter filter : debFilters) {
			if (Pattern.matches(filter.regex, filename)) {
				matchingFilters.add(filter);
			}
		}
		return matchingFilters;
	}
	
	/* Use the XmlAttributes on the fields rather than the getters
	 * and setters provided by Lombok */
	@XmlAccessorType(XmlAccessType.FIELD)
	@XmlRootElement @AllArgsConstructor @NoArgsConstructor @Data
	public static class Filter implements Comparable<Filter>{
		@XmlTransient
		final int BEFORE = -1;
		@XmlTransient
		final int EQUAL = 0;
		@XmlTransient
		final int AFTER = 1;
		
		@XmlAttribute
		private String regex;
		
		@XmlAttribute
		private String dist;
		
		@XmlAttribute
		private String component;
		
		public boolean matches(String filename) {
			return (Pattern.matches(this.regex, filename));
		}

		@Override
		public int compareTo(Filter o) {
			int comparison = this.dist.compareTo(o.getDist());
			if (comparison != EQUAL) return comparison;
			
			comparison = this.component.compareTo(o.getComponent());
			if (comparison != EQUAL) return comparison;
			
			comparison = this.regex.compareTo(o.getRegex());
			if (comparison != EQUAL) return comparison;
			
			assert this.equals(o) : "DebRepositoryBuildTypeConfig.Filter :: compareTo inconsistant with equals";
			
			return EQUAL;
		}
		
	}

}