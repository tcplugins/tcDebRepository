package debrepo.teamcity.util;

import static org.junit.Assert.*;

import org.junit.Test;

import debrepo.teamcity.util.RepositoryNameValidator.RepositoryNameValidationResult;

public class RepositoryNameValidatorTest {

	@Test
	public void testNameIsURlSafeReturnsErrorIsTrueForSpacedText() {
		RepositoryNameValidationResult result = new RepositoryNameValidator().nameIsURlSafe("Net Wolf UK");
		assertEquals(true, result.isError());
	}
	
	@Test
	public void testNameIsURlSafeReturnsErrorIsFalseForNonSpacedText() {
		RepositoryNameValidationResult result = new RepositoryNameValidator().nameIsURlSafe("NetwolfUK");
		assertEquals(false, result.isError());
	}

}
