package debrepo.teamcity.entity.helper;

import java.util.Collection;
import java.util.Date;
import java.util.Set;

import debrepo.teamcity.GenericRepositoryFile;
import debrepo.teamcity.entity.DebRepositoryConfiguration;

public interface ReleaseDescriptionBuilder {

	String buildPackageDescriptionList(DebRepositoryConfiguration configuration,
			Collection<? extends GenericRepositoryFile> repositoryFiles, String dist, Date modifiedTime);

	String buildReleaseHeader(DebRepositoryConfiguration configuration, String dist, Set<String> components,
			Set<String> archs, Date modifiedTime);

	String buildSimpleReleaseFile(DebRepositoryConfiguration configuration, String dist, String component,
			String arch);
	


}