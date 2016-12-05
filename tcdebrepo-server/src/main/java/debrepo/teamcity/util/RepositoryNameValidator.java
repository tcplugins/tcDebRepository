package debrepo.teamcity.util;

import java.util.regex.Pattern;

import lombok.AllArgsConstructor;
import lombok.Data;

public class RepositoryNameValidator {
	
	public RepositoryNameValidationResult nameIsURlSafe(String repoName) {
		if ( ! Pattern.matches("^[A-Za-z0-9_-]+$", repoName) ) {
			   return new RepositoryNameValidationResult(true, "Please use A-Za-z0-9_- in Debian Repository Names");
		}
		return new RepositoryNameValidationResult(false, "Looks good");
	}
	
	@Data @AllArgsConstructor
	public class RepositoryNameValidationResult {
		boolean error;
		String reason;
	}

}
