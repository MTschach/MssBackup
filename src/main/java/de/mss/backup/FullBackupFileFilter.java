package de.mss.backup;

import java.io.File;
import java.io.FileFilter;


public class FullBackupFileFilter implements FileFilter {

   private String filename = null;


   public FullBackupFileFilter(String f) {
      this.filename = f;
   }


   @Override
   public boolean accept(File pathname) {
      if (!pathname.getName().startsWith(this.filename))
         return false;

      if (!pathname.getName().contains("fullbackup"))
         return false;

      return true;
   }
}
