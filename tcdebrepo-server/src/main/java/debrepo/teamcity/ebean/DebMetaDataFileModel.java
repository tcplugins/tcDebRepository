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

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import debrepo.teamcity.GenericRepositoryFile;
import io.ebean.Finder;
import io.ebean.Model;
import io.ebean.annotation.Index;
import io.ebean.annotation.WhenModified;
import lombok.Getter;
import lombok.Setter;

@Index(columnNames = {"repository_id", "dist", "path" ,"modified_time"})
@Entity
@Table(name = "o_deb_metadata_file")
@Getter
@Setter
public class DebMetaDataFileModel extends Model implements GenericRepositoryFile {

	public static final MyFinder find = new MyFinder();
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	Long id;
	
	@Index
	@ManyToOne
	private DebRepositoryModel repository;
	
	@Index
	String fileName;
	
	@Index
	@WhenModified
	Date modifiedTime;

	@Lob
	byte[] fileContent;
	
	@Index
	String dist;
	String component;
	String arch;
	
	@Index
	String path;
	
	String md5;
	String sha1;
	String sha256;

	@Override
	public int getSizeInBytes() {
		if (fileContent == null) {
			return 0;
		}
		return fileContent.length;
	}

	@Override
	public String getFilePath() {
		return path;
	}
	
	public static class MyFinder extends Finder<Long, DebMetaDataFileModel> {

		/**
		 * Construct using the default EbeanServer.
		 */
		public MyFinder() {
			super(DebMetaDataFileModel.class);
		}

	}

}

