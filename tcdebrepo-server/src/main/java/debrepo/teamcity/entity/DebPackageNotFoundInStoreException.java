package debrepo.teamcity.entity;

public class DebPackageNotFoundInStoreException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 5565263992371156814L;

	public DebPackageNotFoundInStoreException(String message) {
		super(message);
	}

}
