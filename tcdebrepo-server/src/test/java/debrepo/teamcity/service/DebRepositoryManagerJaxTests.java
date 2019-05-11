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
package debrepo.teamcity.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;

import javax.xml.bind.JAXBException;

import org.junit.Test;
import org.mockito.Mock;

import debrepo.teamcity.entity.DebPackageStore;
import debrepo.teamcity.entity.DebRepositoryConfiguration;
import debrepo.teamcity.entity.helper.XmlPersister;

public class DebRepositoryManagerJaxTests extends DebRepositoryManagerTest {
	
	
	@Mock protected XmlPersister<DebPackageStore, DebRepositoryConfiguration> debRepositoryDatabaseXmlPersister;
	DebRepositoryManagerImpl debRepositoryManagerImpl;

	
	public void setupLocal() throws JAXBException, IOException {
		System.out.println("Runing setupLocal()");
		when(debRepositoryDatabaseXmlPersister.loadfromXml(any(DebRepositoryConfiguration.class))).thenThrow(new IOException("DB file is not found when mocking"));
	}
	
	private void initialiseDebRepositoryManager() {
		debRepositoryManagerImpl = new DebRepositoryManagerImpl(projectManager, 
				debRepositoryDatabaseXmlPersister, 
				debRepositoryConfigurationFactory, 
				debRepositoryConfigurationChangePersister);
	}

	@Override
	public DebRepositoryManager getDebRepositoryManager() {
		if (debRepositoryManagerImpl == null) {
			initialiseDebRepositoryManager();
		}
		return debRepositoryManagerImpl; 
	}
	
	@Override
	public DebRepositoryConfigurationManager getDebRepositoryConfigurationManager() {
		if (debRepositoryManagerImpl == null) {
			initialiseDebRepositoryManager();
		}
		return debRepositoryManagerImpl; 
	}

	@Override @Test
	public void testPersist() throws IOException {
		DebRepositoryConfiguration config = readConfig.getDebRepositoryConfigurations().get(0);
		getDebRepositoryManager().addBuildPackage(config, entity);
		verify(debRepositoryDatabaseXmlPersister).persistToXml(any(DebPackageStore.class));
		
	}

}

