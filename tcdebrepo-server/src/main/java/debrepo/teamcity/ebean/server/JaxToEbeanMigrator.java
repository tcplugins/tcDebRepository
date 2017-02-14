package debrepo.teamcity.ebean.server;

import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import debrepo.teamcity.DebPackage;
import debrepo.teamcity.Loggers;
import debrepo.teamcity.entity.DebRepositoryConfiguration;
import debrepo.teamcity.entity.helper.JaxDbFileRenamer;
import debrepo.teamcity.service.DebRepositoryManager;
import debrepo.teamcity.service.NonExistantRepositoryException;

public class JaxToEbeanMigrator {
	
	private DebRepositoryManager myJaxDebRepositoryManager;
	private DebRepositoryManager myEbeanDebRepositoryManager;
	private JaxDbFileRenamer myJaxDbFileRenamer;

	public JaxToEbeanMigrator(
						DebRepositoryManager jaxDebRepositoryManager, 
						DebRepositoryManager ebeanDebRepositoryManager,
						JaxDbFileRenamer jaxDbFileRenamer) {
		myJaxDebRepositoryManager = jaxDebRepositoryManager;
		myEbeanDebRepositoryManager = ebeanDebRepositoryManager;
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
			myEbeanDebRepositoryManager.addBuildPackages(config, packages);
			packageMigrationCount += packages.size();
		}
		Loggers.SERVER.info("JaxToEbeanMigrator :: " + packageMigrationCount + " of " + packagesToMigrateCount + " packages migrated. Removing repository items...");
		myJaxDebRepositoryManager.getPackageStore(config.getRepoName()).clear();;
		Loggers.SERVER.info("JaxToEbeanMigrator :: Repository items removed. Renaming XML database file...");
		if (myJaxDbFileRenamer.renameToBackup(config)) {
			Loggers.SERVER.info("JaxToEbeanMigrator :: XML database file renamed for " + config.getRepoName() + "(" + config.getUuid().toString() + ")");
		}
	}

}
