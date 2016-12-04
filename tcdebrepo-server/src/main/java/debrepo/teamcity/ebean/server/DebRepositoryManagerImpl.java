package debrepo.teamcity.ebean.server;

import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.UUID;

import debrepo.teamcity.DebPackage;
import debrepo.teamcity.ebean.DebPackageModel;
import debrepo.teamcity.ebean.DebRepositoryModel;
import debrepo.teamcity.ebean.query.QDebPackageModel;
import debrepo.teamcity.ebean.query.QDebRepositoryModel;
import debrepo.teamcity.entity.DebPackageNotFoundInStoreException;
import debrepo.teamcity.entity.DebPackageStore;
import debrepo.teamcity.entity.DebRepositoryConfiguration;
import debrepo.teamcity.entity.DebRepositoryStatistics;
import debrepo.teamcity.service.DebRepositoryConfigurationFactory;
import debrepo.teamcity.service.DebRepositoryConfigurationManager;
import debrepo.teamcity.service.DebRepositoryConfigurationManagerImpl;
import debrepo.teamcity.service.DebRepositoryManager;
import debrepo.teamcity.service.NonExistantRepositoryException;
import debrepo.teamcity.settings.DebRepositoryConfigurationChangePersister;

public class DebRepositoryManagerImpl extends DebRepositoryConfigurationManagerImpl implements DebRepositoryManager, DebRepositoryConfigurationManager {

	public DebRepositoryManagerImpl(
			EbeanServerProvider ebeanServerProvider,
			DebRepositoryConfigurationFactory debRepositoryConfigurationFactory,
			DebRepositoryConfigurationChangePersister debRepositoryConfigurationChangePersister) {
		super(debRepositoryConfigurationFactory, debRepositoryConfigurationChangePersister);
	}

	/**
	 * Shouldn't be used anyway.
	 */
	@Override
	public DebPackageStore getPackageStore(String storeName) throws NonExistantRepositoryException {
		return null;
	}

	
	@Override
	public DebPackageStore initialisePackageStore(DebRepositoryConfiguration conf) {
		DebRepositoryModel repo = new QDebRepositoryModel().uuid.eq(conf.getUuid().toString()).findUnique();
		if (repo == null) {
			repo = new DebRepositoryModel();
		}
		repo.setName(conf.getRepoName());
		repo.setUuid(conf.getUuid().toString());
		repo.setProjectId(conf.getProjectId());
		repo.save();
		return null;
	}

	/**
	 * Shouldn't be required for DB.
	 */
	@Override
	public List<DebPackageStore> getPackageStoresForBuildType(String buildTypeid) throws NonExistantRepositoryException {
		// Shouldn't be required for DB.
		return null;
	}

	/** 
	 * This is a noop on a DB.
	 */
	@Override
	public boolean persist(UUID uuid) {
		// This is a noop on a DB.
		return true;
	}

	@Override
	public DebRepositoryStatistics getRepositoryStatistics(String uuid, String repoUrl) {
		int count = new QDebPackageModel().repository.uuid.eq(uuid).findCount();
		return new DebRepositoryStatistics(count, repoUrl);
	}

	@Override
	public DebRepositoryStatistics getRepositoryStatistics(DebRepositoryConfiguration projectConfig, String repoUrl) {
		return getRepositoryStatistics(projectConfig.getUuid().toString(), repoUrl);
	}

	@Override
	public void addBuildPackage(DebRepositoryConfiguration config, DebPackage newEntity) {
		//initialisePackageStore(config);
		DebPackageModel m = DebPackageModel.copy(newEntity);
		DebRepositoryModel repo = new QDebRepositoryModel().uuid.eq(config.getUuid().toString()).findUnique();
		
		m.setRepository(repo);
		m.save();
	}
	
	@Override
	public Set<String> findUniqueArchByDistAndComponent(String repoName, String distName, String component) throws NonExistantRepositoryException {
		if (! isExistingRepository(repoName)){
			throw new NonExistantRepositoryException();
		}
		List<String> archs = new QDebPackageModel().select("arch")
				  .setDistinct(false)
				  .repository.name.eq(repoName)
				  .dist.eq(distName)
				  .component.eq(component)
				  .findSingleAttributeList();
		return new TreeSet<String>(archs);
	}

	@Override
	public Set<String> findUniqueComponentByDist(String repoName, String distName) throws NonExistantRepositoryException {
		if (! isExistingRepository(repoName)){
			throw new NonExistantRepositoryException();
		}
		List<String> components = new QDebPackageModel().select("component")
				  .setDistinct(false)
				  .repository.name.eq(repoName)
				  .dist.eq(distName)
				  .findSingleAttributeList();
		return new TreeSet<String>(components);
	}

	@Override
	public Set<String> findUniqueDist(String repoName) throws NonExistantRepositoryException {
		if (! isExistingRepository(repoName)){
			throw new NonExistantRepositoryException();
		}
		List<String> dists = new QDebPackageModel().select("dist")
				  .setDistinct(false)
				  .repository.name.eq(repoName)
				  .findSingleAttributeList();
		return new TreeSet<String>(dists);
	}

	@Override
	public Set<String> findUniqueComponent(String repoName) throws NonExistantRepositoryException {
		if (! isExistingRepository(repoName)){
			throw new NonExistantRepositoryException();
		}
		List<String> components = new QDebPackageModel().select("component")
				  .setDistinct(false)
				  .repository.name.eq(repoName)
				  .findSingleAttributeList();
		return new TreeSet<String>(components);
	}

	@Override
	public Set<String> findUniquePackageNameByComponent(String repoName, String component)
			throws NonExistantRepositoryException {
		if (! isExistingRepository(repoName)){
			throw new NonExistantRepositoryException();
		}		

		List<String> packages = new QDebPackageModel().select("packageName")
				 					  .setDistinct(false)
				 					  .repository.name.eq(repoName)
				 					  .component.eq(component)
				 					  .findSingleAttributeList();
		return new TreeSet<String>(packages);

	}

	@Override
	public List<? extends DebPackage> getUniquePackagesByComponentAndPackageName(String repoName, String component,
			String packageName) throws NonExistantRepositoryException {
		if (! isExistingRepository(repoName)){
			throw new NonExistantRepositoryException();
		}
		return new QDebPackageModel().select("filename, uri")
									 .setDistinct(true)
									 .repository.name.eq(repoName)
									 .component.eq(component)
									 .packageName.eq(packageName)
									 .findList(); 
	}

	@Override
	public List<? extends DebPackage> findAllByDistComponentArch(String repoName, String distName, String component, String archName) throws NonExistantRepositoryException {
		return new QDebPackageModel().repository.name.eq(repoName).dist.eq(distName).component.eq(component).findList(); 
	}

	@Override
	public DebPackage findByUri(String repoName, String uri)
			throws NonExistantRepositoryException, DebPackageNotFoundInStoreException {
		return new QDebPackageModel().repository.name.eq(repoName).filename.iequalTo(uri).findUnique();
	}

	@Override
	public boolean isExistingRepository(String repoName) {
		return new QDebRepositoryModel().name.eq(repoName).findCount()  > 0;
	}

	@Override
	public boolean isExistingRepository(UUID uuid) {
		//return new QDebRepositoryModel().findCount() > 0;
		return new QDebRepositoryModel().uuid.eq(uuid.toString()).findCount() > 0;
	}


	@Override
	public void removeRepository(UUID uuid) {
		new QDebPackageModel().repository.uuid.eq(uuid.toString()).delete();
		new QDebRepositoryModel().uuid.eq(uuid.toString()).delete();
	}
	
}
