/*******************************************************************************
 * Copyright 2017 Net Wolf UK
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
import java.util.TreeMap;
import java.util.Map.Entry;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import io.ebean.Model;
import io.ebean.annotation.Index;

import io.ebean.Finder;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "o_debfile")
@Getter
@Setter
@Index(columnNames = {"build_id", "filename"})
public class DebFileModel extends Model {

	public static final MyFinder find = new MyFinder();

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	Long id;
	
	@OneToMany(mappedBy = "debFile", cascade=CascadeType.ALL)
	List<DebPackageModel> debpackages;

	private String packageName;

	private String version;

	private String arch;

	private Long buildId;

	private String buildTypeId;

	private String filename;

	@OneToMany(mappedBy = "debFile", fetch=FetchType.EAGER, cascade=CascadeType.ALL)
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
	
	public boolean isPopulated() {
		return this.arch != null && this.packageName != null && this.version != null;
	}

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
	
	protected void replaceParameter(String key, String newValue) {
		for (DebPackageParameterModel p : getPackageParameters()) {
			if (key.equals(p.getName())) {
				p.setValue(newValue);
			}
		}
	}

	protected void removeParameter(String key) {
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
	
	public static class MyFinder extends Finder<Long, DebFileModel> {

		/**
		 * Construct using the default EbeanServer.
		 */
		public MyFinder() {
			super(DebFileModel.class);
		}

	}
}
