package debrepo.teamcity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public enum RepoDataFileType {
	
	// Files from the root of a dist. Located at /dists/jessie/
	Release			("Release", 		"text/plain",	true), 
	InRelease		("InRelease", 		"text/plain",	true), 
	ReleaseGpg		("Release.gpg", 	"text/plain",	true),
	
	// Files in an arch dir. Located /dists/jessie/main/binary-amd64/
	Packages 		("Packages", 		"text/plain",			false), 
	PackagesGz 		("Packages.gz",		"application/x-gzip",	false), 
	PackagesXz 		("Packages.xz", 	"application/x-xz",		false), 
	PackagesBz2 	("Packages.bz2",	"application/x-bzip2",	false),
	SimpleRelease	("Release",			"text/plain",			false);
	
	private final String fileName;
	private final String contentType;
	private final boolean isTopLevel;
	
	// Private constructor
	private RepoDataFileType(String fileName, String contentType, boolean isTopLevel) {
		this.fileName  = fileName;
		this.contentType = contentType;
		this.isTopLevel = isTopLevel;
	}
	
	public String getFileName() {
		return fileName;
	}
	
	public String getContentType() {
		return contentType;
	}

	/** Finds the {@link RepoDataFileType} with the matching filename.
	 * If none found, returns the {@link release} file type.
	 * 
	 * @param fileName
	 * @return Relevant {@link RepoDataFileType} or {@link release} if not found.
	 */
	public static RepoDataFileType findByName(String fileName) {
		for (RepoDataFileType type : values()) {
			if (type.getFileName().equals(fileName)) {
				return type;
			}
		}
		return Release;
	}
	
	public static List<RepoDataFileType> getTopLevelFileTypes() {
		List<RepoDataFileType> topLevelFileTypes = new ArrayList<RepoDataFileType>();
		for (RepoDataFileType type : values()) {
			if (type.isTopLevel) {
				topLevelFileTypes.add(type);	
			}
		}
		return Collections.unmodifiableList(topLevelFileTypes);
	}
	
	public static List<RepoDataFileType> getArchLevelFileTypes() {
		List<RepoDataFileType> archLevelFileTypes = new ArrayList<RepoDataFileType>();
		for (RepoDataFileType type : values()) {
			if ( ! type.isTopLevel) {
				archLevelFileTypes.add(type);	
			}
		}
		return Collections.unmodifiableList(archLevelFileTypes);
	}
}
