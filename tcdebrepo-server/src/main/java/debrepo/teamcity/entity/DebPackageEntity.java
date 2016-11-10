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
import java.util.Map;
import java.util.Map.Entry;

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
	private List<PackageParameter> parameters = new ArrayList<>();
	
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
	
	public DebPackageEntity clone() {
		DebPackageEntity e = new DebPackageEntity();
		e.setArch(this.getArch());
		e.setComponent(this.getComponent());
		e.setFilename(this.getFilename());
		e.setPackageName(this.getPackageName());
		e.parameters.addAll(this.getParameters());
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
		return new DebPackageEntityKey(this.getPackageName(), this.getVersion(), this.getArch(), this.getComponent(), this.dist);
	}
	
	public boolean isPopulated() {
		return this.arch != null && this.packageName != null && this.version != null;
	}
	
	public void populateMetadata(Map<String,String> metaData) {
		for (Entry<String,String> entry : metaData.entrySet()){
			this.parameters.add(new PackageParameter(entry.getKey(), entry.getValue()));
		}
		
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
		this.setUri("pool/" + this.getComponent() + "/" + this.getPackageName() + "/" + filename.replace("\\", "/"));
		if (this.getUri() != null && !this.getUri().equals("")) {
			this.parameters.add(new PackageParameter("Filename", this.getUri()));
		}
	}
}
