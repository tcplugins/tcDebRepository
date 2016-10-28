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

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import org.jetbrains.annotations.NotNull;

import lombok.Data;

/* Use the XmlAttributes on the fields rather than the getters
 * and setters provided by Lombok */
@XmlAccessorType(XmlAccessType.FIELD)

@Data  // Let Lombok generate the getters and setters.

@XmlRootElement
public class DebPackageEntity {
	
	@NotNull @XmlAttribute(name="Package")
	private String packageName;
	
	@NotNull @XmlAttribute(name="Version")
	private String version;
	@NotNull @XmlAttribute(name="Architecture")
	private String arch;
	@NotNull @XmlAttribute(name="sBuildId")
	private Long sBuildId;
	@NotNull @XmlAttribute(name="sBuildTypeId")
	private String sBuildTypeId;
	
	@XmlElement(name="parameter") @XmlElementWrapper(name="package-parameters")
	private List<PackageParameter> parameters = new ArrayList<>();
	
	@XmlType(name = "format") @Data  @XmlAccessorType(XmlAccessType.FIELD)
	public static class PackageParameter {
		@XmlAttribute
		String name;
		
		@XmlAttribute
		String value;
		
		PackageParameter() {
			// empty constructor for JAXB
		}
	}
	
	public DebPackageEntityKey buildKey(){
		return new DebPackageEntityKey(this.getPackageName(), this.getVersion(), this.getArch());
	}
	
}
