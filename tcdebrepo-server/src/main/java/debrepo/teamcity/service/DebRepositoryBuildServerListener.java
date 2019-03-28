package debrepo.teamcity.service;

import debrepo.teamcity.Loggers;
import jetbrains.buildServer.serverSide.BuildServerAdapter;
import jetbrains.buildServer.serverSide.SBuildServer;
import jetbrains.buildServer.util.executors.ExecutorsFactory;

public class DebRepositoryBuildServerListener extends BuildServerAdapter {
	
	private final DebRepositoryConfigurationManager myDebRepositoryConfigurationManager;
	private final DebRepositoryManager myDebRepositoryManager;
	private final SBuildServer myBuildServer;

	public DebRepositoryBuildServerListener(
			DebRepositoryManager maintenanceManager,
			DebRepositoryConfigurationManager configurationManager,
			SBuildServer buildServer) {
		myDebRepositoryManager = maintenanceManager;
		myDebRepositoryConfigurationManager = configurationManager;
		myBuildServer = buildServer;
	}
	
    public void register(){
        myBuildServer.addListener(this);
        Loggers.SERVER.info("DebRepositoryBuildServerListener" + " :: Registering");
    }
	
	@Override
	public void serverStartup() {
		ExecutorsFactory.newExecutor("debRepoStartupThread").execute(
				new DebRepoOnBootExecutor(myDebRepositoryConfigurationManager, myDebRepositoryManager));
	}

}
