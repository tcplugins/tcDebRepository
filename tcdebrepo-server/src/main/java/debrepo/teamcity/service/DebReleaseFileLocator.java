package debrepo.teamcity.service;

import java.util.List;

import debrepo.teamcity.FileHashType;
import debrepo.teamcity.RepoDataFileType;

public interface DebReleaseFileLocator {
	public abstract String findReleaseFile(String reponame, String dist, RepoDataFileType releaseFileType) throws NonExistantRepositoryException, DebRepositoryItemNotFoundException;
	public abstract String findReleaseFile(String reponame, String dist, String component, String architecture, RepoDataFileType releaseFileType) throws NonExistantRepositoryException, DebRepositoryItemNotFoundException;
	public abstract byte[] findPackagesFile(String reponame, RepoDataFileType packagesFileType, String dist, String component, String architecture) throws NonExistantRepositoryException, DebRepositoryItemNotFoundException;
	public abstract String findPackagesTextFile(String reponame, RepoDataFileType packagesFileType, String dist, String component, String architecture) throws NonExistantRepositoryException, DebRepositoryItemNotFoundException;
	public List<String> findFileHashes(String repoName, FileHashType hashType, String distName) throws NonExistantRepositoryException;
	public List<String> findFileHashes(String repoName, FileHashType hashType, String distName, String component, String architecture) throws NonExistantRepositoryException;

}
