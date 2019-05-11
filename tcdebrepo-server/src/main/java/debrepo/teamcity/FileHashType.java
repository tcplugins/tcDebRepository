package debrepo.teamcity;

public enum FileHashType {
	
	md5("MD5Sum"), sha1("SHA1"), sha256("SHA256");
	
	private final String debianTypeName;
	
	
	private FileHashType(String debianTypeName) {
		this.debianTypeName  = debianTypeName;
	}
	
	public String getDebianTypeName() {
		return debianTypeName;
	}
	
	public String getTcDebRepoTypeName() {
		return this.toString();
	}
	
	

}
