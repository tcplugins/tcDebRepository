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

import java.io.FileNotFoundException;
import java.io.InputStream;

import javax.xml.bind.JAXBException;

public interface JaxHelper<T> {

	/**
	 * Read saved configuration from file
	 * 
	 * @return T bean
	 * @throws JAXBException
	 * @throws FileNotFoundException
	 */
	public abstract T read(String configFilePath)
			throws JAXBException, FileNotFoundException;

	/**
	 * Read saved configuration from stream
	 * 
	 * @return T bean
	 * @throws JAXBException
	 */
	public abstract T read(InputStream stream)
			throws JAXBException;

	/**
	 * Write T bean to configuration file
	 * 
	 * @throws JAXBException
	 */
	public abstract void write(T jaxObject, String configFilePath)
			throws JAXBException;

}