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

import lombok.AllArgsConstructor;
import lombok.Data;

@Data @AllArgsConstructor
public class DebPackageEntityKey  implements Comparable<DebPackageEntityKey>{
	
	private String packageName;
	private String version;
	private String arch;
	
	public boolean isSamePackageAndVersion(DebPackageEntityKey debPackageEntityKey){
		return this.packageName.equalsIgnoreCase(debPackageEntityKey.getPackageName())
				&& this.version.equalsIgnoreCase(debPackageEntityKey.getVersion());
	}
	
	public boolean isSamePackage(DebPackageEntityKey debPackageEntityKey){
		return this.packageName.equalsIgnoreCase(debPackageEntityKey.getPackageName());
	}

	@Override
	public int compareTo(DebPackageEntityKey o) {
		if (this.isSamePackageAndVersion(o)){
			return this.getArch().compareToIgnoreCase(o.getArch());
		} else if (this.isSamePackage(o)) {
			return this.getVersion().compareToIgnoreCase(o.getVersion());
		} else {
			return this.getPackageName().compareToIgnoreCase(o.getPackageName());
		}
	}

}
