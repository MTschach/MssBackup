package de.mss.backup;

import java.io.File;

import org.junit.Test;

import junit.framework.TestCase;

public class FullBackupFileFilterTest extends TestCase {
   private File[]   files    = null;


   @Override
   public void setUp() {
      this.files = new File[4];
      this.files[0] = new File("documents.zip");
      this.files[1] = new File("documents_fullbackup.zip");
      this.files[2] = new File("scripts.zip");
      this.files[3] = new File("scripts_fullbackup.zip");
   }


   @Test
   public void testOnlyIncludes() {
      FullBackupFileFilter bff = new FullBackupFileFilter("documents", "tar");

      assertFalse(this.files[0].getName(), bff.accept(this.files[0]));
      assertTrue(this.files[1].getName(), bff.accept(this.files[1]));
      assertFalse(this.files[2].getName(), bff.accept(this.files[2]));
      assertFalse(this.files[3].getName(), bff.accept(this.files[3]));
   }
}
