/*******************************************************************************
 *
 *  Copyright 2016, 2017 Net Wolf UK
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *  
 *  
 *******************************************************************************/
package debrepo.teamcity.ebean;

import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.MapKey;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import com.avaje.ebean.Model;
import com.avaje.ebean.annotation.WhenModified;

import debrepo.teamcity.GenericRepositoryFile;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "o_deb_packages_file")
@Getter
@Setter
public class DebPackagesFileModel extends Model implements GenericRepositoryFile {


	private static final String MD5 = "md5";
	private static final String SHA1 = "sha1";
	private static final String SHA256 = "sha256";

	public static Find<Long, DebPackagesFileModel> getFind() {
		return find;
	}

	public static final Find<Long, DebPackagesFileModel> find = new Find<Long, DebPackagesFileModel>() {};

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	Long id;
	
	@ManyToOne
	private DebRepositoryModel repository;
	
	@OneToMany(mappedBy = "packagesFile", cascade=CascadeType.ALL)
	@MapKey(name="hashType")
	Map<String,DebPackagesFileHashModel> debPackagesHashes;
	
	String packagesFileName;
	
	@WhenModified
	Date modifiedTime;

	@Lob
	byte[] packagesFile;
	
	String dist;
	String component;
	String arch;
	
	String path;

	public void setMd5(String md5Hex) {
		updateHash(MD5, md5Hex);
	}
	
	public void setSha1(String sha1Hex) {
		updateHash(SHA1, sha1Hex);
	}

	public void setSha256(String sha256Hex) {
		updateHash(SHA256, sha256Hex);
	}
	
	private void updateHash(String hashType, String hashValue) {
		if (debPackagesHashes.containsKey(hashType)) {
			debPackagesHashes.get(hashType).setHashValue(hashValue);
			return;
		}
	
		DebPackagesFileHashModel newHash = new DebPackagesFileHashModel();
		newHash.setPackagesFile(this);
		newHash.setHashType(hashType);
		newHash.setHashValue(hashValue);
		this.debPackagesHashes.put(hashType, newHash);
	}

	@Override
	public String getSizeInBytes() {
		return String.valueOf(packagesFile.length);
	}

	@Override
	public String getMd5() {
		return debPackagesHashes.get(MD5).getHashValue();
	}

	@Override
	public String getSha1() {
		return debPackagesHashes.get(SHA1).getHashValue();
	}

	@Override
	public String getSha256() {
		return debPackagesHashes.get(SHA256).getHashValue();
	}

	@Override
	public String getFilePath() {
		return path;
	}

}

