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

package debrepo.teamcity;

import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import debrepo.teamcity.entity.DebRepositoryConfigurations;
import debrepo.teamcity.entity.helper.JaxHelper;


/**
 * This is a quick way to test that the "build-server-plugin-tcdebrepo.xml"
 * file has a working spring context. Otherwise, one has to load it into 
 * TeamCity before failures are visible.
 * 
 * @author netwolfuk
 *
 */

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {
									"classpath:META-INF/test-spring-context.xml", 
									"classpath:META-INF/build-server-plugin-tcdebrepo.xml" 
								  })
public class SpringContextValidationTest {
	
	@Autowired
	private JaxHelper<DebRepositoryConfigurations> configs;

	@Test @Ignore
	public void test() {
		MockitoAnnotations.initMocks(this);
		
	}

}
