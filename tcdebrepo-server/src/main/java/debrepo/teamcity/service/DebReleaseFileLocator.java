package debrepo.teamcity.service;

public interface DebReleaseFileLocator {
	public abstract String findReleaseFile(String reponame, String dist, ReleaseFileType releaseFileType) throws NonExistantRepositoryException, DebRepositoryItemNotFoundException;
	public abstract String findReleaseFile(String reponame, String dist, String component, String architecture, ReleaseFileType releaseFileType) throws NonExistantRepositoryException, DebRepositoryItemNotFoundException;
	public abstract byte[] findPackagesFile(String reponame, PackagesFileType packagesFileType, String dist, String component, String architecture) throws NonExistantRepositoryException, DebRepositoryItemNotFoundException;
	
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
		
		/** Finds the {@link ReleaseFileType} with the matching filename.
		 * If none found, returns the {@link Release} file type.
		 * 
		 * @param releaseFileName
		 * @return Relevant {@link ReleaseFileType} or {@link Release} if not found.
		 */
		public static ReleaseFileType findByName(String releaseFileName) {
			for (ReleaseFileType type : values()) {
				if (type.getFilename().equals(releaseFileName)) {
					return type;
				}
			}
			return Release;
		}
	}
	
	public static enum PackagesFileType {
		Packages ("Packages", "text/plain"), 
		PackagesGz ("Packages.gz", "application/x-gzip"), 
		PackagesXz ("Packages.xz", "application/x-xz"), 
		PackagesBz2 ("Packages.bz2", "application/x-bzip2");
		
		String filename;
		String contentType;
		
		private PackagesFileType(String filename, String contentType) {
			this.filename = filename;
			this.contentType = contentType;
		}
		
		public String getFilename() {
			return this.filename;
		}
		
		public String getContentType() {
			return contentType;
		}
		
		/** Finds the {@link PackagesFileType} with the matching filename.
		 * If none found, returns the {@link Packages} file type.
		 * 
		 * @param packagesFileName
		 * @return Relevant {@link PackagesFileType} or {@link Packages} if not found.
		 */
		public static PackagesFileType findByName(String packagesFileName) {
			for (PackagesFileType type : values()) {
				if (type.getFilename().equals(packagesFileName)) {
					return type;
				}
			}
			return Packages;
		}
	}

}
