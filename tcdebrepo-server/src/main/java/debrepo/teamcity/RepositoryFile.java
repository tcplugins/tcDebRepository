package debrepo.teamcity;

public interface RepositoryFile {
	public String getSizeInKbAndFilename(); 
	public String getMD5Sum(); 
	public String getSHA1(); 
	public String getSHA256();

}
