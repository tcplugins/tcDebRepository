package debrepo.teamcity.service;

public interface DebReleaseFileLocator {
	public abstract String findReleaseFile(String reponame, String dist, ReleaseFileType releaseFileType) throws NonExistantRepositoryException, DebRepositoryDatabaseItemNotFoundException;
	public abstract String findReleaseFile(String reponame, String dist, String component, String architecture, ReleaseFileType releaseFileType) throws NonExistantRepositoryException, DebRepositoryDatabaseItemNotFoundException;
	public abstract byte[] findPackagesFile(String reponame, PackagesFileType packagesFileType, String dist, String component, String architecture) throws NonExistantRepositoryException, DebRepositoryDatabaseItemNotFoundException;
	
	public static enum ReleaseFileType {
		Release ("Release"),
		InRelease ("InRelease"), 
		ReleaseGpg ("Release.gpg");
		
		String filename;
		
		private ReleaseFileType(String filename) {
			this.filename = filename;
		}
		
		public String getFilename() {
			return this.filename;
		}		
	}
	
	public static enum PackagesFileType {
		Packages ("Packages"), 
		PackagesGz ("Packages.gz"), 
		PackagesXz ("Packages.xz"), 
		PackagesBz2 ("Packages.bz2");
		
		String filename;
		
		private PackagesFileType(String filename) {
			this.filename = filename;
		}
		
		public String getFilename() {
			return this.filename;
		}
	}

}
