package debrepo.teamcity.entity.helper;

import java.util.Collection;
import java.util.Map.Entry;

import debrepo.teamcity.DebPackage;
import debrepo.teamcity.entity.DebPackageEntity;

/**
 * <p>
 * Responsible for generating a Debian Repository compliant Package
 * listing by extracting the text from the parameters map in the
 * DebPackageEntity. The output assembled into the Packages.gz file. 
 * </p>
 * 
 * <p>
 * Package: must be the first line, and package entries must have
 * a line break between entries.</p>
 * 
 * <p>
 *  An example listing looks like this:</p>
 *  <pre>
 *    Package: tcDummbyDeb
 *    Version: 1.0.153
 *    Architecture: i386
 *    Filename: pool/main/tcDummbyDeb/tcDummyDeb_i386_1.0.153.deb
 *    Maintainer: Net Wolf UK
 *    Priority: optional
 *    Section: misc
 *    Size: 624
 *    MD5sum: e8d5ce3ca9a4133b1a989e9babd73c5
 *    SHA1: 5660a5ee25ca7dc67b4ad2fd285afb2314879c10
 *    SHA256: 51d0b1eca9774272921f06082648d97eef4e1067c76bce54fdb02ed9f1b74d
 *    Description: my first sample application which isn't doing anything special.
 * </pre>
 */
		
public class DebPackageToPackageDescriptionBuilder {
	
	public static String buildPackageDescription(DebPackage debPackageEntity) {
		StringBuilder sb = new StringBuilder();
		sb.append("Package: ").append(debPackageEntity.getPackageName()).append("\n");
		for (Entry<String,String> e : debPackageEntity.getParameters().entrySet()) {
			if (!e.getKey().equals("Package")) {
				sb.append(e.getKey()).append(": ").append(e.getValue()).append("\n");
			}
		}
		return sb.toString();
	}
	
	public static String buildPackageDescriptionList(Collection<? extends DebPackage> debPackageEntities) {
		StringBuilder sb = new StringBuilder();
		for (DebPackage entity : debPackageEntities) {
			sb.append(buildPackageDescription(entity));
			sb.append("\n\n");
		}
		return sb.toString();
	}

}
