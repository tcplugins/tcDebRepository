package debrepo.teamcity.archive;

import jetbrains.buildServer.serverSide.SBuild;

public interface DebFileReaderFactory {

	DebFileReader createFileReader(SBuild build);

}