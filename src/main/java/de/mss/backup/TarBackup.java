package de.mss.backup;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;

import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.ArchiveOutputStream;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream;

import de.mss.backup.exception.ErrorCodes;
import de.mss.utils.exception.MssException;

public class TarBackup extends BackupBase {

   public TarBackup() {
      super();
   }


   @SuppressWarnings("resource")
   @Override
   protected ArchiveOutputStream getOutStream(String filename) throws MssException {
      TarArchiveOutputStream outStream = null;

      try {
         outStream = new TarArchiveOutputStream(new BufferedOutputStream(new FileOutputStream(filename)));
         // TAR has an 8 gig file limit by default, this gets around that
         outStream.setBigNumberMode(TarArchiveOutputStream.BIGNUMBER_STAR);
         // TAR originally didn't support long file names, so enable the support for it
         outStream.setLongFileMode(TarArchiveOutputStream.LONGFILE_GNU);
         outStream.setAddPaxHeadersForNonAsciiNames(true);
      }
      catch (FileNotFoundException e) {
         throw new MssException(ErrorCodes.ERROR_ARCHIVE_FAILED, e, "Failed to create TAR-Archive");
      }

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
      return "tar";
   }

}
