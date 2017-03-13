package debrepo.teamcity.service;

import java.util.List;

import debrepo.teamcity.DebPackage;
import debrepo.teamcity.Loggers;
import jetbrains.buildServer.serverSide.SBuild;
import jetbrains.buildServer.serverSide.artifacts.BuildArtifact;
import jetbrains.buildServer.serverSide.artifacts.BuildArtifacts;
import jetbrains.buildServer.serverSide.artifacts.BuildArtifacts.BuildArtifactsProcessor;

public class DebFileBuildArtifactsProcessorFactoryImpl implements DebFileBuildArtifactsProcessorFactory {

	@Override
	public BuildArtifactsProcessor getBuildArtifactsProcessor(SBuild build, List<DebPackage> entities) {
		// TODO Auto-generated method stub
		return new MyBuildArtifactsProcessor(build, entities);
	}

	
	public static class MyBuildArtifactsProcessor implements BuildArtifacts.BuildArtifactsProcessor {
		
		private List<DebPackage> myEntities;
		private SBuild myBuild;

		public MyBuildArtifactsProcessor(SBuild build, List<DebPackage> entities) {
			this.myBuild = build;
			this.myEntities = entities;
		}

		@Override
		public Continuation processBuildArtifact(BuildArtifact artifact) {
			Loggers.SERVER.debug("DebRepositoryBuildArtifactsPublisherImpl :: Processing artifact: " 
						+ artifact.getRelativePath() + " " + artifact.getName());
			this.myEntities.add(DebPackageFactory.buildFromArtifact(this.myBuild, artifact.getRelativePath()));
			return Continuation.CONTINUE;
		}
		
	}
}
