package debrepo.teamcity.util;

import static org.junit.Assert.*;

import org.junit.Test;

public class StringUtilsTest {

	@Test
	public void testStripTrailingSlash() {
		assertEquals("https://some.thing/with/a/slash/on/the/end", StringUtils.stripTrailingSlash("https://some.thing/with/a/slash/on/the/end/"));
	}

	@Test
	public void testGetDebRepoUrl() {
		assertEquals("https://some.thing/httpAuth/app/debrepo-restricted/Test01/", 
				StringUtils.getDebRepoUrl("https://some.thing/", 
											"Test01", true));
		assertEquals("https://some.thing/app/debrepo/Test01/", 
				StringUtils.getDebRepoUrl("https://some.thing/", 
						"Test01", false));
	}

	@Test
	public void testGetDebRepoUrlWithUserPassExample() {
		assertEquals("https://<em><strong>username:password</strong></em>@some.thing/httpAuth/app/debrepo-restricted/Test01/", 
					StringUtils.getDebRepoUrlWithUserPassExample("https://some.thing/", 
												"Test01", true));	
		assertEquals("https://some.thing/app/debrepo/Test01/", 
				StringUtils.getDebRepoUrlWithUserPassExample("https://some.thing/", 
						"Test01", false));	
	}

}
