/*******************************************************************************
 *
 *  Copyright 2016 Net Wolf UK
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

import java.util.List;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import com.avaje.ebean.Model;

import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "o_repository")
@Getter
@Setter
public class DebRepositoryModel extends Model {


	public static Find<Long, DebRepositoryModel> getFind() {
		return find;
	}

	public static final Find<Long, DebRepositoryModel> find = new Find<Long, DebRepositoryModel>() {};

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	Long id;
	
	String name;
	
	String uuid;
	
	String projectId;
	
	@OneToMany(mappedBy = "repository")
	List<DebPackageModel> debpackages;
	
}

