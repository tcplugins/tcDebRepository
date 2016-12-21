package debrepo.teamcity.entity.helper;

import debrepo.teamcity.RepositoryFile;
import lombok.Builder;
import lombok.Value;

@Value @Builder
public class RepositoryFileImpl implements RepositoryFile {
	
	private static final int padnum = 16;
	
	private String sizeInKbAndFilename;
	private String MD5Sum;
	private String SHA1;
	private String SHA256;

	private static void padSize(StringBuilder stringBuilder, String fileSize) {
		int rest = padnum - fileSize.length();
		for(int i = 1; i < rest; i++)
		    {
			stringBuilder.append(" ");
		    }
		stringBuilder.append(fileSize);
	}
	
}
