package debrepo.teamcity.entity.helper;

import java.util.Collection;
import java.util.Map.Entry;

import debrepo.teamcity.entity.DebPackageEntity;

public class DebPackageToPackageDescriptionBuilder {
	
	public static String buildPackageDescription(DebPackageEntity debPackageEntity) {
		StringBuilder sb = new StringBuilder();
		sb.append("Package: ").append(debPackageEntity.getPackageName()).append("\n");
		for (Entry<String,String> e : debPackageEntity.getParameters().entrySet()) {
			if (!e.getKey().equals("Package")) {
				sb.append(e.getKey()).append(": ").append(e.getValue()).append("\n");
			}
		}
		return sb.toString();
	}
	
	public static String buildPackageDescriptionList(Collection<DebPackageEntity> debPackageEntities) {
		StringBuilder sb = new StringBuilder();
		for (DebPackageEntity entity : debPackageEntities) {
			sb.append(buildPackageDescription(entity));
			sb.append("\n\n");
		}
		return sb.toString();
	}

}
