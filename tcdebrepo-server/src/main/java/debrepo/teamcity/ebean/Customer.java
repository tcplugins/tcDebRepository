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

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import com.avaje.ebean.Model;

import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name="o_customer")
@Getter @Setter
public class Customer extends Model {

	public static final Find<Long,Customer> find = new Find<Long,Customer>(){};
	
  @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
  Long id;

  String name;

  Date registered;

  String comments;
  
}