package debrepo.teamcity;

import java.util.Map;

public interface DebPackage {

	public String getPackageName();

	public void setPackageName(String packageName);

	public String getVersion();

	public void setVersion(String version);

	public String getArch();

	public void setArch(String arch);

	public String getDist();

	public void setDist(String dist);

	public String getComponent();

	public void setComponent(String component);

	public Long getBuildId();

	public void setBuildId(Long sBuildId);

	public String getBuildTypeId();

	public void setBuildTypeId(String sBuildTypeId);

	public String getFilename();

	public void setFilename(String filename);

	public String getUri();

	public void setUri(String uri);

	public Map<String, String> getParameters();

	public void setParameters(Map<String, String> parameters);

	public boolean isPopulated();

	public void populateMetadata(Map<String, String> metaDataFromPackage);

	public void buildUri();

}