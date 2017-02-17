package debrepo.teamcity;

import java.util.Map;

import lombok.Data;

@Data
public class DebPackageItem implements DebPackage {
	
	private String packageName;

	private String version;

	private String arch;

	private String dist;

	private String component;

	private Long buildId;
	
	private String buildTypeId;

	private String filename;

	private String uri;
	
	private Map<String, String> parameters;

	@Override
	public boolean isPopulated() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void populateMetadata(Map<String, String> metaDataFromPackage) {
		// TODO Auto-generated method stub

	}

	@Override
	public void buildUri() {
		// TODO Auto-generated method stub

	}

}
