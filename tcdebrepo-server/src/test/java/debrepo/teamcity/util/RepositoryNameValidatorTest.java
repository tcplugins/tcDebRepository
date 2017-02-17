/*******************************************************************************
 * Copyright 2017 Net Wolf UK
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/
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
