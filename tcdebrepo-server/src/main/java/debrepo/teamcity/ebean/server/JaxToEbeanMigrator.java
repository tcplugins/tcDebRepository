package debrepo.teamcity.ebean.server;

import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import debrepo.teamcity.DebPackage;
import debrepo.teamcity.entity.DebRepositoryConfiguration;
import debrepo.teamcity.service.DebRepositoryManager;
import debrepo.teamcity.service.NonExistantRepositoryException;

public class JaxToEbeanMigrator {
	
	private DebRepositoryManager myJaxDebRepositoryManager;
	private DebRepositoryManager myEbeanDebRepositoryManager;

	public JaxToEbeanMigrator(DebRepositoryManager jaxDebRepositoryManager, DebRepositoryManager ebeanDebRepositoryManager) {
		myJaxDebRepositoryManager = jaxDebRepositoryManager;
		myEbeanDebRepositoryManager = ebeanDebRepositoryManager;
		
	}
	
	@SuppressWarnings("unchecked")
	public void migrate(DebRepositoryConfiguration config) throws NonExistantRepositoryException {
		for (String filename : myJaxDebRepositoryManager.findUniqueFilenames(config.getRepoName())) {
			myEbeanDebRepositoryManager.initialisePackageStore(config);
			Set<String> filenames = new TreeSet<>();
			filenames.add(filename);
			myEbeanDebRepositoryManager.addBuildPackages(config, (List<DebPackage>) myJaxDebRepositoryManager.findAllByFilenames(config.getRepoName(), filenames));
		}
	}

}
