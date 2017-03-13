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
package debrepo.teamcity.archive;

import debrepo.teamcity.entity.helper.PluginDataResolver;
import jetbrains.buildServer.serverSide.SBuild;
import jetbrains.buildServer.serverSide.ServerPaths;

public class DebFileReaderFactoryImpl implements DebFileReaderFactory {
	
	private PluginDataResolver myPluginPaths;

	public DebFileReaderFactoryImpl(PluginDataResolver pluginPaths, ServerPaths serverPaths) {
		this.myPluginPaths = pluginPaths;
	}

	@Override
	public DebFileReader createFileReader(SBuild build) {
		return new DebFileReaderImpl(build.getArtifactsDirectory(), this.myPluginPaths.getPluginTempFileDirectory());
	}
	
	

}
