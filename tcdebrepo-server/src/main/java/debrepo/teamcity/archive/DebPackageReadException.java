package debrepo.teamcity.archive;

public class DebPackageReadException extends Exception {
	private static final long serialVersionUID = 1L;

	DebPackageReadException(String message) {
		super(message);
	}	
}
