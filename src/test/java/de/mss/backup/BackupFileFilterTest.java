package de.mss.backup;

import java.io.File;

import org.junit.Test;

import junit.framework.TestCase;

public class BackupFileFilterTest extends TestCase {

   private String[] includes = null;
   private String[] excludes = null;

   private File[]   files    = null;


   @Override
   public void setUp() {
      this.includes = new String[] {"*.txt", "*.bat", "wichtig*", "*.doc"};
      this.excludes = new String[] {"*.bak", "*~", "bla.doc"};

      this.files = new File[10];
      this.files[0] = new File("bla.foo");
      this.files[1] = new File("datei.txt");
      this.files[2] = new File("datei.txt.bak");
      this.files[3] = new File("script.bat");
      this.files[4] = new File("wichtiges_files.doc");
      this.files[5] = new File("file.txt");
      this.files[6] = new File("script~");
      this.files[7] = new File("bla~");
      this.files[8] = new File("bla.doc");
      this.files[9] = new File("bli.doc");
   }


   @Test
   public void testOnlyIncludes() {
      BackupFileFilter bff = new BackupFileFilter(this.includes, null);

      assertFalse(this.files[0].getName(), bff.accept(this.files[0]));
      assertTrue(this.files[1].getName(), bff.accept(this.files[1]));
      assertFalse(this.files[2].getName(), bff.accept(this.files[2]));
      assertTrue(this.files[3].getName(), bff.accept(this.files[3]));
      assertTrue(this.files[4].getName(), bff.accept(this.files[4]));
      assertTrue(this.files[5].getName(), bff.accept(this.files[5]));
      assertFalse(this.files[6].getName(), bff.accept(this.files[6]));
      assertFalse(this.files[7].getName(), bff.accept(this.files[7]));
      assertTrue(this.files[8].getName(), bff.accept(this.files[8]));
      assertTrue(this.files[9].getName(), bff.accept(this.files[9]));
   }


   @Test
   public void testIncludesExcludes() {
      BackupFileFilter bff = new BackupFileFilter(this.includes, this.excludes);

      assertFalse(this.files[0].getName(), bff.accept(this.files[0]));
      assertTrue(this.files[1].getName(), bff.accept(this.files[1]));
      assertFalse(this.files[2].getName(), bff.accept(this.files[2]));
      assertTrue(this.files[3].getName(), bff.accept(this.files[3]));
      assertTrue(this.files[4].getName(), bff.accept(this.files[4]));
      assertTrue(this.files[5].getName(), bff.accept(this.files[5]));
      assertFalse(this.files[6].getName(), bff.accept(this.files[6]));
      assertFalse(this.files[7].getName(), bff.accept(this.files[7]));
      assertFalse(this.files[8].getName(), bff.accept(this.files[8]));
      assertTrue(this.files[9].getName(), bff.accept(this.files[9]));
   }


   @Test
   public void testExcludeAll() {
      BackupFileFilter bff = new BackupFileFilter(this.includes, new String[] {"*"});

      assertFalse(this.files[0].getName(), bff.accept(this.files[0]));
      assertFalse(this.files[1].getName(), bff.accept(this.files[1]));
      assertFalse(this.files[2].getName(), bff.accept(this.files[2]));
      assertFalse(this.files[3].getName(), bff.accept(this.files[3]));
      assertFalse(this.files[4].getName(), bff.accept(this.files[4]));
      assertFalse(this.files[5].getName(), bff.accept(this.files[5]));
      assertFalse(this.files[6].getName(), bff.accept(this.files[6]));
      assertFalse(this.files[7].getName(), bff.accept(this.files[7]));
      assertFalse(this.files[8].getName(), bff.accept(this.files[8]));
      assertFalse(this.files[9].getName(), bff.accept(this.files[9]));

   }
}
