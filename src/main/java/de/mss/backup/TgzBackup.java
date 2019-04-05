package de.mss.backup;

public class TgzBackup extends TarGzBackup {

   public TgzBackup(String logName) {
      super(logName);
   }


   @Override
   protected String getFileExtension() {
      return "tgz";
   }

}
