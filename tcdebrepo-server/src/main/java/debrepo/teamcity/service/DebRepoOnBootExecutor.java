package debrepo.teamcity.service;

import debrepo.teamcity.Loggers;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class DebRepoOnBootExecutor implements Runnable {
	
	private final DebRepositoryConfigurationManager myDebRepositoryConfigurationManager;
	private final DebRepositoryManager myDebRepositoryManager;
	
	@Override
	public void run() {
		myDebRepositoryConfigurationManager.getAllConfigurations().forEach(
				debConfig -> {
					try {
						myDebRepositoryManager.updateRepositoryMetaData(debConfig);
					} catch (NonExistantRepositoryException | DebRepositoryPersistanceException e) {
						Loggers.SERVER.warn("Unable to update DebRepository meta-data", e);
					}
				}
			);
	}
}