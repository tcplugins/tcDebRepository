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

import java.util.Map;
import java.util.TreeMap;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import org.jetbrains.annotations.NotNull;

import jetbrains.buildServer.serverSide.SBuild;
import lombok.AllArgsConstructor;
import lombok.Data;

/* Use the XmlAttributes on the fields rather than the getters
 * and setters provided by Lombok */
@XmlAccessorType(XmlAccessType.FIELD)

@Data  // Let Lombok generate the getters and setters.

@XmlRootElement
public class DebPackageEntity implements Cloneable {
	
	@NotNull @XmlAttribute(name="Package")
	private String packageName;

	@NotNull @XmlAttribute(name="Version")
	private String version;
	
	@NotNull @XmlAttribute(name="Architecture")
	private String arch;
	
	@NotNull @XmlAttribute(name="Dist")
	private String dist;
	
	@NotNull @XmlAttribute(name="Component")
	private String component;
	
	@NotNull @XmlAttribute(name="sBuildId")
	private Long sBuildId;
	
	@NotNull @XmlAttribute(name="sBuildTypeId")
	private String sBuildTypeId;
	
	@NotNull @XmlAttribute(name="filename")
	private String filename;
	
	@NotNull @XmlAttribute(name="deb-uri")
	private String uri;
	
	@XmlElement(name="parameter") @XmlElementWrapper(name="package-parameters")
	private Map<String, String> parameters = new TreeMap<>();
	
	@XmlType(name = "format") @Data  @XmlAccessorType(XmlAccessType.FIELD) @AllArgsConstructor
	public static class PackageParameter {
		@XmlAttribute
		String name;
		
		@XmlAttribute
		String value;
		
		PackageParameter() {
			// empty constructor for JAXB
		}
	}

	/**
	 * setComponent which also updates the uri. This version overrides the Lombok one.
	 * 
	 * @param component The new value for the component
	 */
	public void setComponent(String component) {
		this.component = component;
		this.buildUri();
	}
	
	/**
	 * setPackageName which also updates the uri. This version overrides the Lombok one.
	 * 
	 * @param packageName The new value for the packageName
	 */
	public void setPackageName(String packageName) {
		this.packageName = packageName;
		this.buildUri();
	}
	
	/**
	 * setFilename which also updates the uri. This version overrides the Lombok one.
	 * 
	 * @param filename The new value for the filename
	 */
	public void setFilename(String filename) {
		this.filename = filename;
		this.buildUri();
	}
	
	public DebPackageEntity clone() {
		DebPackageEntity e = new DebPackageEntity();
		e.setArch(this.getArch());
		e.setComponent(this.getComponent());
		e.setDist(this.getDist());
		e.setFilename(this.getFilename());
		e.setPackageName(this.getPackageName());
		e.parameters.putAll(this.getParameters());
		e.setSBuildId(this.getSBuildId());
		e.setSBuildTypeId(this.getSBuildTypeId());
		e.setUri(this.getUri());
		e.setVersion(this.getVersion());
		return e;
	}
	
	public static DebPackageEntity buildFromArtifact(SBuild build, String filename) {
		DebPackageEntity e = new DebPackageEntity();
		e.setSBuildId(build.getBuildId());
		e.setSBuildTypeId(build.getBuildTypeId());
		e.setFilename(filename);
		return e;
	}
	
	public DebPackageEntityKey buildKey(){
		return new DebPackageEntityKey(this.getPackageName(), this.getVersion(), this.getArch(), this.getComponent(), this.getDist());
	}
	
	public boolean isPopulated() {
		return this.arch != null && this.packageName != null && this.version != null;
	}
	
	public void populateMetadata(Map<String,String> metaData) {
		this.parameters.clear();
		this.parameters.putAll(metaData);
		
		if (metaData.containsKey("Package")) {
			this.setPackageName(metaData.get("Package"));
		}
		
		if (metaData.containsKey("Version")) {
			this.setVersion(metaData.get("Version"));
		}
		
		if (metaData.containsKey("Architecture")) {
			this.setArch(metaData.get("Architecture"));
		}
		
	}
	
	public void buildUri() {
		if ("".equals(this.component) || "".equals(this.packageName) || this.filename == null || "".equals(this.filename)) {
			this.uri = "";
			if (this.parameters.containsKey("Filename")) {
				this.parameters.remove("Filename");
			}
		} else {
			this.setUri("pool/" + this.getComponent() + "/" + this.getPackageName() + "/" + filename.replace("\\", "/"));
			this.parameters.put("Filename", this.getUri());
		}
	}
}
