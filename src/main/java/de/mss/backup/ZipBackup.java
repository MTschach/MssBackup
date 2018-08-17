package de.mss.backup;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;

import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.ArchiveException;
import org.apache.commons.compress.archivers.ArchiveOutputStream;
import org.apache.commons.compress.archivers.ArchiveStreamFactory;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveOutputStream;

public class ZipBackup extends BackupBase {

   public ZipBackup() {
      super();
   }


   @Override
   protected ArchiveOutputStream getOutStream(String filename) throws FileNotFoundException {
      ArchiveOutputStream outStream = null;
      try {
         outStream = new ArchiveStreamFactory()
               .createArchiveOutputStream(ArchiveStreamFactory.ZIP, new BufferedOutputStream(new FileOutputStream(filename)));
      }
      catch (ArchiveException e) {
         e.printStackTrace();
      }

      return outStream;
   }


   @Override
   protected ArchiveEntry getOutStreamEntry(ArchiveOutputStream outStream, File file, String name) {

      if (!(outStream instanceof ZipArchiveOutputStream))
         return null;


      ZipArchiveEntry entry = new ZipArchiveEntry(file, name);
      return entry;
   }


   @Override
   protected String getFileExtension() {
      return "zip";
   }

}
