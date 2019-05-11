package debrepo.teamcity;

public interface DebPackagesFile extends GenericRepositoryFile {

	/**
	 * Get the file content as a byte array
	 * @return byte array containing the contents of the file.
	 */
	public byte[] getFileContents();
}
