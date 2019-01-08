package de.mss.backup;

import java.io.File;
import java.io.FileFilter;
import java.nio.file.FileSystems;
import java.nio.file.PathMatcher;
import java.nio.file.Paths;


public class BackupFileFilter implements FileFilter {

   private String[] includes = null;
   private String[] excludes = null;


   public BackupFileFilter(String[] inc, String[] exc) {
      this.includes = inc;
      this.excludes = exc;
   }


   @Override
   public boolean accept(File pathname) {
      if (isSet(this.excludes) && matches(pathname, this.excludes)) {
         return false;
      }

      return matches(pathname, this.includes);
   }


   private boolean matches(File pathname, String[] list) {
      for (String e : list) {
         if ("*".equals(e))
            return true;

         PathMatcher m = FileSystems.getDefault().getPathMatcher("glob:"+e);
         if (m.matches(Paths.get(pathname.getName())))
        	 return true;
      }

      return false;
   }


   private boolean isSet(String[] list) {
      return list != null && list.length > 0;
   }


}
