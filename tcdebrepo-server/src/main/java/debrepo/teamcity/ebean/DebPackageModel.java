/*******************************************************************************
 * Copyright 2016, 2017 Net Wolf UK
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

import java.util.Map;
import java.util.TreeMap;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import debrepo.teamcity.DebPackage;
import io.ebean.Finder;
import io.ebean.Model;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "o_debpackage")
@Getter
@Setter
public class DebPackageModel extends Model implements DebPackage {

	public static final MyFinder find = new MyFinder();

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	Long id;
	
	@ManyToOne
	private DebRepositoryModel repository;
	
	@ManyToOne(optional=false, cascade=CascadeType.PERSIST)
	private DebFileModel debFile;

	private String dist;
	
	private String component;

	private String uri;
	
	//private String filename;
	
	public static DebPackageModel copy(DebPackage deb) {
		
		DebFileModel f = DebFileModel.find.query().where().eq("buildId", deb.getBuildId()).eq("filename", deb.getFilename()).findOne();
		if (f == null) {
			f = new DebFileModel();
			f.setArch(deb.getArch());
			f.setFilename(deb.getFilename());
			f.setPackageName(deb.getPackageName());
			f.setParameters(deb.getParameters());
			f.setBuildId(deb.getBuildId());
			f.setBuildTypeId(deb.getBuildTypeId());
			f.setVersion(deb.getVersion());
			//f.save();
		}
		DebPackageModel e = new DebPackageModel();
		e.setDebFile(f);
		e.setComponent(deb.getComponent());
		e.setDist(deb.getDist());
		e.setUri(deb.getUri());
		return e;
	}



	@Override
	public void buildUri() {
		if ("".equals(this.component) || "".equals(this.debFile.getPackageName()) || this.debFile.getFilename() == null || "".equals(this.debFile.getFilename())) {
			this.uri = "";
			//if (this.debFile.getParameters().containsKey("Filename")) {
			//	this.debFile.removeParameter("Filename");
			//}
		} else {	
			this.setUri("pool/" + this.getComponent() + "/" + this.getPackageName() + "/" + this.debFile.getFilename().replace("\\", "/"));
			//this.debFile.replaceParameter("Filename", this.getUri());
		}
	}



	@Override
	public String getPackageName() {
		return debFile.getPackageName();
	}

	@Override
	public void setPackageName(String packageName) {
		debFile.setPackageName(packageName);
	}

	@Override
	public String getVersion() {
		return debFile.getVersion();
	}

	@Override
	public void setVersion(String version) {
		debFile.setVersion(version);
	}

	@Override
	public String getArch() {
		return debFile.getArch();
	}

	@Override
	public void setArch(String arch) {
		debFile.setArch(arch);
	}

	@Override
	public Long getBuildId() {
		return debFile.getBuildId();
	}

	@Override
	public void setBuildId(Long sBuildId) {
		debFile.setBuildId(sBuildId);
	}

	@Override
	public String getBuildTypeId() {
		return debFile.getBuildTypeId();
	}

	@Override
	public void setBuildTypeId(String sBuildTypeId) {
		debFile.setBuildTypeId(sBuildTypeId);
	}
	
	@Override
	public String getFilename() {
		return debFile.getFilename();
	}
	
	@Override
	public void setFilename(String filename) {
		debFile.setFilename(filename);
		
	}

	@Override
	public Map<String, String> getParameters() {
		Map<String, String> params = debFile.getParameters();
		params.put("Filename", getUri());
		return params;
	}



	@Override
	public void setParameters(Map<String, String> parameters) {
		Map<String, String> params= new TreeMap<>();
		params.putAll(parameters);
		params.remove("Filename");
		debFile.setParameters(params);
	}



	@Override
	public boolean isPopulated() {
		return debFile.isPopulated();
	}



	@Override
	public void populateMetadata(Map<String, String> metaDataFromPackage) {
		debFile.populateMetadata(metaDataFromPackage);
	}

	public static class MyFinder extends Finder<Long, DebPackageModel> {

		/**
		 * Construct using the default EbeanServer.
		 */
		public MyFinder() {
			super(DebPackageModel.class);
		}

	}
	
}