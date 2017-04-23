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
package debrepo.teamcity.ebean.server;

import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import debrepo.teamcity.DebPackage;
import debrepo.teamcity.Loggers;
import debrepo.teamcity.entity.DebRepositoryConfiguration;
import debrepo.teamcity.entity.helper.JaxDbFileRenamer;
import debrepo.teamcity.service.DebReleaseFileGenerator;
import debrepo.teamcity.service.DebRepositoryManager;
import debrepo.teamcity.service.NonExistantRepositoryException;

public class JaxToEbeanMigrator {
	
	private DebRepositoryManager myJaxDebRepositoryManager;
	private DebRepositoryManager myEbeanDebRepositoryManager;
	private DebReleaseFileGenerator myDebReleaseFileGenerator;
	private JaxDbFileRenamer myJaxDbFileRenamer;

	public JaxToEbeanMigrator(
						DebRepositoryManager jaxDebRepositoryManager, 
						DebRepositoryManager ebeanDebRepositoryManager,
						DebReleaseFileGenerator debReleaseFileGenerator,
						JaxDbFileRenamer jaxDbFileRenamer) {
		myJaxDebRepositoryManager = jaxDebRepositoryManager;
		myEbeanDebRepositoryManager = ebeanDebRepositoryManager;
		myDebReleaseFileGenerator = debReleaseFileGenerator;
		myJaxDbFileRenamer = jaxDbFileRenamer;
		
	}
	
	@SuppressWarnings("unchecked")
	public void migrate(DebRepositoryConfiguration config) throws NonExistantRepositoryException {
		if (myJaxDebRepositoryManager.getPackageStore(config.getRepoName()).isEmpty()) {
			Loggers.SERVER.debug("JaxToEbeanMigrator :: No packages found. Skipping migration of empty repo: " + config.getRepoName() + " (" + config.getUuid().toString() + ")");
			return;
		}
		int packagesToMigrateCount = myJaxDebRepositoryManager.getPackageStore(config.getRepoName()).size();
		int packageMigrationCount = 0;
		for (String filename : myJaxDebRepositoryManager.findUniqueFilenames(config.getRepoName())) {
			myEbeanDebRepositoryManager.initialisePackageStore(config);
			Set<String> filenames = new TreeSet<>();
			filenames.add(filename);
			List<DebPackage> packages = (List<DebPackage>) myJaxDebRepositoryManager.findAllByFilenames(config.getRepoName(), filenames);
			Loggers.SERVER.debug("JaxToEbeanMigrator :: Migrating all packages in repo \"" + config.getRepoName() + "\" of filename: " + filename);
			myEbeanDebRepositoryManager.bulkAddBuildPackages(config, packages);
			//myEbeanDebRepositoryManager.addBuildPackages(config, packages);
			packageMigrationCount += packages.size();
		}
		Loggers.SERVER.info("JaxToEbeanMigrator :: " + packageMigrationCount + " of " + packagesToMigrateCount + " packages migrated from JAX to eBean. Removing repository items from JAX cache...");
		myJaxDebRepositoryManager.getPackageStore(config.getRepoName()).clear();;
		Loggers.SERVER.info("JaxToEbeanMigrator :: Repository items removed from JAX cache. Renaming XML database file so it won't be migrated again in future...");
		if (myJaxDbFileRenamer.renameToBackup(config)) {
			Loggers.SERVER.info("JaxToEbeanMigrator :: XML database file renamed for " + config.getRepoName() + "(" + config.getUuid().toString() + ")");
		}
		Loggers.SERVER.info("JaxToEbeanMigrator :: Requesting Release file generation for " + config.getRepoName() + "(" + config.getUuid().toString() + ")");
		this.myDebReleaseFileGenerator.updateAllReleaseFiles(config);
		Loggers.SERVER.info("JaxToEbeanMigrator :: Completed Release file generation for " + config.getRepoName() + "(" + config.getUuid().toString() + ")");
	}

}
