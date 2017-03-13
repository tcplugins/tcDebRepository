package debrepo.teamcity.service;

import java.util.List;

import debrepo.teamcity.DebPackage;
import jetbrains.buildServer.serverSide.SBuild;
import jetbrains.buildServer.serverSide.artifacts.BuildArtifacts.BuildArtifactsProcessor;

public interface DebFileBuildArtifactsProcessorFactory {
	
	public abstract BuildArtifactsProcessor getBuildArtifactsProcessor(SBuild build, List<DebPackage> entities);

}
