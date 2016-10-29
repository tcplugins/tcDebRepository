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

package debrepo.teamcity.entity.helper;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import debrepo.teamcity.entity.DebRepositoryConfigurations;

public class DebRepositoryConfigurationJaxHelperImpl implements JaxHelper<DebRepositoryConfigurations> {

	@Override
	@Nullable
	public DebRepositoryConfigurations read(@NotNull String configFilePath)
			throws JAXBException, FileNotFoundException {
		JAXBContext context = JAXBContext.newInstance(DebRepositoryConfigurations.class);
		Unmarshaller um = context.createUnmarshaller();
		File file = new File(configFilePath);
		if (!file.isFile()) {
			return new DebRepositoryConfigurations();
		}
		return (DebRepositoryConfigurations) um.unmarshal(file);
	}

	@Override
	@NotNull
	public DebRepositoryConfigurations read(@NotNull InputStream stream)
			throws JAXBException {
		JAXBContext context = JAXBContext.newInstance(DebRepositoryConfigurations.class);
		Unmarshaller um = context.createUnmarshaller();
		return (DebRepositoryConfigurations) um.unmarshal(stream);
	}

	@Override
	public void write(@NotNull DebRepositoryConfigurations repoConfigs,
			@NotNull String configFilePath) throws JAXBException {
		
		JAXBContext context = JAXBContext.newInstance(DebRepositoryConfigurations.class);
		Marshaller m = context.createMarshaller();
		m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
		//m.setProperty("com.sun.xml.bind.xmlHeaders", "\n<!-- This file is not intended to be edited. You have been warned :-) -->");
		m.marshal(repoConfigs, new File(configFilePath));
	}

}
