package debrepo.teamcity.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.zip.GZIPOutputStream;

import debrepo.teamcity.Loggers;
import debrepo.teamcity.RepoDataFileType;

public class TextConverter {

	public static String fromByteArray(RepoDataFileType fileType, byte[] bytes) {
		return new String(bytes, StandardCharsets.UTF_8);
	}
	
	public static byte[] toByteArray(RepoDataFileType fileType, String string) throws IOException {
		try {
			switch(fileType) {
				case PackagesGz:
					return gzip(string);
				// TODO: case PackagesBz2:
				// TODO: case PackagesBz2:
			 	default:
			 		return utf(string);
			}

		} catch (IOException e) {
			Loggers.SERVER.warn("TextCompressorImpl::toByteArray - Could not create " + fileType.getFileName() + " file");
			throw e;
		}
	}
	
	private static byte[] gzip(String string) throws IOException {
		ByteArrayOutputStream out = new ByteArrayOutputStream(string.length());
		GZIPOutputStream gzip = new GZIPOutputStream(out);
		gzip.write(string.getBytes());
		gzip.close();
		return out.toByteArray();
	}
	
	private static byte[] utf(String string) throws IOException {
		return string.getBytes(StandardCharsets.UTF_8);
	}

}
