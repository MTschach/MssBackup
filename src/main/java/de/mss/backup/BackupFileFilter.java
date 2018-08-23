package de.mss.backup;

import java.io.File;
import java.io.FileFilter;
import java.nio.file.FileSystems;
import java.nio.file.PathMatcher;
import java.nio.file.Paths;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class BackupFileFilter implements FileFilter {

   private String[] includes = null;
   private String[] excludes = null;


   public BackupFileFilter(String[] inc, String[] exc) {
      this.includes = inc;
      this.excludes = exc;
   }


   @Override
   public boolean accept(File pathname) {
      if (isSet(excludes) && matches(pathname, excludes)) {
         return false;
      }

      return matches(pathname, includes);
   }


   private boolean matches(File pathname, String[] list) {
      for (String e : list) {
         if ("*".equals(e))
            return true;

//         else if (e.contains("*")) {
//            String[] p = e.split("\\*");
//
//            if (p[0].length() > 0 && !pathname.getName().startsWith(p[0]))
//               return false;
//
//            if (p.length >= 2 && p[p.length - 1].length() > 0 && !pathname.getName().endsWith(p[p.length - 1]))
//               return false;
//
//            for (int i = 1; i < p.length - 2; i++ )
//               if (!pathname.getName().contains(p[i]))
//                  return false;
//
//            return true;
//         }
//
//         else if (pathname.getName().equals(e))
//            return true;
         
         if (pathname.getName().contains("bash"))
         {
        	 System.out.println("halt");
         }
         
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
