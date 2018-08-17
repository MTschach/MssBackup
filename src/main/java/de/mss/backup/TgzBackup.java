package de.mss.backup;

public class TgzBackup extends TarGzBackup {

   public TgzBackup() {
      super();
   }


   @Override
   protected String getFileExtension() {
      return "tgz";
   }

}
