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

package debrepo.teamcity.ebean;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import com.avaje.ebean.Model;

import debrepo.teamcity.DebPackage;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "o_debpackage")
@Getter
@Setter
public class DebPackageModel extends Model implements DebPackage {


	public static Find<Long, DebPackageModel> getFind() {
		return find;
	}

	public static final Find<Long, DebPackageModel> find = new Find<Long, DebPackageModel>() {};

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	Long id;
	
	@ManyToOne
	private DebRepositoryModel repository;

	private String packageName;

	private String version;

	private String arch;

	private String dist;

	private String component;

	private Long buildId;

	private String buildTypeId;

	private String filename;

	private String uri;

	@OneToMany(mappedBy = "debPackage", fetch=FetchType.EAGER, cascade=CascadeType.ALL)
	private List<DebPackageParameterModel> packageParameters;
	
	public Map<String,String> getParameters() {
		Map<String, String> map = new TreeMap<>();
		for (DebPackageParameterModel m : packageParameters) {
			map.put(m.getName(), m.getValue());
		}
		return map;
	}
	
	public void setParameters(Map<String,String> parametersMap) {
		packageParameters.clear();
		for (Entry<String,String> e : parametersMap.entrySet()) {
			packageParameters.add(new DebPackageParameterModel(null, this, e.getKey(), e.getValue()));
		}
	}
	
	public static DebPackageModel copy(DebPackage deb) {
		DebPackageModel e = new DebPackageModel();
		e.setArch(deb.getArch());
		e.setComponent(deb.getComponent());
		e.setDist(deb.getDist());
		e.setFilename(deb.getFilename());
		e.setPackageName(deb.getPackageName());
		e.setParameters(deb.getParameters());
		e.setBuildId(deb.getBuildId());
		e.setBuildTypeId(deb.getBuildTypeId());
		e.setUri(deb.getUri());
		e.setVersion(deb.getVersion());
		return e;
	}

	@Override
	public boolean isPopulated() {
		return this.arch != null && this.packageName != null && this.version != null;
	}

	@Override
	public void populateMetadata(Map<String, String> metaData) {
		this.packageParameters.clear();
		this.setParameters(metaData);
		
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

	@Override
	public void buildUri() {
		if ("".equals(this.component) || "".equals(this.packageName) || this.filename == null || "".equals(this.filename)) {
			this.uri = "";
			if (this.getParameters().containsKey("Filename")) {
				this.removeParameter("Filename");
			}
		} else {
			this.setUri("pool/" + this.getComponent() + "/" + this.getPackageName() + "/" + filename.replace("\\", "/"));
			this.replaceParameter("Filename", this.getUri());
		}
	}

	private void replaceParameter(String key, String newValue) {
		for (DebPackageParameterModel p : getPackageParameters()) {
			if (key.equals(p.getName())) {
				p.setValue(newValue);
			}
		}
	}

	private void removeParameter(String key) {
		DebPackageParameterModel itemToRemove = null;
		for (DebPackageParameterModel p : getPackageParameters()) {
			if (key.equals(p.getName())) {
				itemToRemove = p;
			}
		}
		if (itemToRemove != null) {
			getPackageParameters().remove(itemToRemove);
		}
		
	}

}