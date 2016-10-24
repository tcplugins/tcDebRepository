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
package debrepo.teamcity.entity;

import java.util.TreeMap;

public class DebPackageStore extends TreeMap<DebPackageEntityKey, DebPackageEntity> {

	private static final long serialVersionUID = 5836877424915088844L;

	public DebPackageEntity find(DebPackageEntityKey key) {
		return this.get(key);
	}
	
	public DebPackageEntity find(String packageName, String version, String arch){
		return this.get(new DebPackageEntityKey(packageName, version, arch));
	}

}
