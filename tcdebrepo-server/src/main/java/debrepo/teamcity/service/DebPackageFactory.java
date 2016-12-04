package debrepo.teamcity.service;

import debrepo.teamcity.DebPackage;
import debrepo.teamcity.entity.DebPackageEntity;
import jetbrains.buildServer.serverSide.SBuild;

public class DebPackageFactory {

	public static DebPackage buildFromArtifact(SBuild build, String filename) {
		DebPackage e = new DebPackageEntity();
		e.setBuildId(build.getBuildId());
		e.setBuildTypeId(build.getBuildTypeId());
		e.setFilename(filename);
		return e;
	}
	
	public static DebPackage copy(DebPackage debPackage) {
		return DebPackageEntity.copy(debPackage);
	}
	

}
