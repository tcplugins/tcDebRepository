package debrepo.teamcity.archive;

import java.io.File;
import java.io.IOException;

import org.rauschig.jarchivelib.ArchiveFormat;
import org.rauschig.jarchivelib.ArchiveStream;
import org.rauschig.jarchivelib.Archiver;
import org.rauschig.jarchivelib.ArchiverFactory;
import org.rauschig.jarchivelib.CompressionType;

import lombok.AllArgsConstructor;

/**
 * A wrapper class around a control file, which knows how to 
 * returns an appropriately uncompressed text stream.
 *
 */
@AllArgsConstructor
public class DebFileControlFile {

	CompressionType compressionType;
	File compressedFile;

	/**
	 * Delete the file referred to by compressedFile. A simple wrapper around File.delete()
	 * @return result of 
	 */
	public boolean delete() {
		return this.compressedFile.delete();
	}

	/**
	 * Return the {@link ArchiveStream} of the control file having been 
	 * uncompressed with the appropriate compression algorithm.
	 * @return A stream of the control file content.
	 * @throws IOException
	 */
	public ArchiveStream stream() throws IOException {
		Archiver archiver;

		if (compressionType.equals(CompressionType.XZ)) {
			archiver = ArchiverFactory.createArchiver(ArchiveFormat.TAR, CompressionType.XZ);
		} else {
			archiver = ArchiverFactory.createArchiver(ArchiveFormat.TAR, CompressionType.GZIP);
		}
		return archiver.stream(compressedFile);
	}
}
