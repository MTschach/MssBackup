package de.mss.backup;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.ArchiveOutputStream;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream;
import org.apache.commons.compress.compressors.bzip2.BZip2CompressorOutputStream;

public class TarBzip2Backup extends BackupBase {

   public TarBzip2Backup() {
      super();
   }


   @Override
   protected ArchiveOutputStream getOutStream(String filename) throws IOException {
      TarArchiveOutputStream outStream = new TarArchiveOutputStream(
            new BZip2CompressorOutputStream(new BufferedOutputStream(new FileOutputStream(filename))));
      // TAR has an 8 gig file limit by default, this gets around that
      outStream.setBigNumberMode(TarArchiveOutputStream.BIGNUMBER_STAR);
      // TAR originally didn't support long file names, so enable the support for it
      outStream.setLongFileMode(TarArchiveOutputStream.LONGFILE_GNU);
      outStream.setAddPaxHeadersForNonAsciiNames(true);

      return outStream;
   }


   @Override
   protected ArchiveEntry getOutStreamEntry(ArchiveOutputStream outStream, File file, String name) {

      if (!(outStream instanceof TarArchiveOutputStream))
         return null;

      if (file.isDirectory())
         return null;

      TarArchiveEntry entry = new TarArchiveEntry(file, name);
      return entry;
   }


   @Override
   protected String getFileExtension() {
      return "tar.bz2";
   }

}
