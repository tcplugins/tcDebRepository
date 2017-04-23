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
import javax.persistence.UniqueConstraint;

import com.avaje.ebean.Model;

import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "o_deb_release_file",
	uniqueConstraints =
		@UniqueConstraint( columnNames={ "repository_id", "dist" } )
	)
@Getter
@Setter
public class DebReleaseFileModel extends Model {


	public static Find<Long, DebReleaseFileModel> getFind() {
		return find;
	}

	public static final Find<Long, DebReleaseFileModel> find = new Find<Long, DebReleaseFileModel>() {};

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	Long id;
	
	@ManyToOne
	private DebRepositoryModel repository;
	
	String dist;
	
	Date modifiedTime;
	
	@Lob
	String releaseFile;
	
	@Lob
	String inReleaseFile;
	
	@Lob
	String releaseFileGpg;
	
}

