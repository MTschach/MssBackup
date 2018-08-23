package de.mss.backup;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import de.mss.utils.Tools;
import de.mss.utils.os.OsType;

public class Backup {

   private String  configFile  = null;
   private String  backupDir   = null;
   private boolean fullBackup  = false;
   private String  archiveType = null;


   public Backup(String[] args) {
      try {
         init(args);
      }
      catch (ParseException e) {
         e.printStackTrace();
      }

      BackupBase backup = null;
      switch (this.archiveType) {
         case "tar":
            backup = new TarBackup();
            break;

         case "targz":
            backup = new TarGzBackup();
            break;

         case "tgz":
            backup = new TgzBackup();
            break;

         case "tarbz2":
            backup = new TarBzip2Backup();
            break;

         case "zip":
            backup = new ZipBackup();
            break;

         default:
            System.out.println("keine Archivart angegeben");
            break;
      }

      if (backup != null) {
         backup.doBackup(this.configFile, this.backupDir, this.fullBackup);
      }

   }


   private void init(String[] args) throws ParseException {
      Options cmdArgs = new Options();

      Option configFile = new Option("f", "config-file", true, "configuration file");
      configFile.setRequired(false);
      cmdArgs.addOption(configFile);

      Option backupDir = new Option("d", "backup-dir", true, "backup directory");
      backupDir.setRequired(false);
      cmdArgs.addOption(backupDir);

      Option forceFullBackup = new Option("fb", "full-backup", false, "force full backup");
      forceFullBackup.setRequired(false);
      cmdArgs.addOption(forceFullBackup);

      Option archiveType = new Option("a", "archive", true, "backup directory");
      archiveType.setRequired(false);
      cmdArgs.addOption(archiveType);

      CommandLineParser parser = new DefaultParser();
      CommandLine cmd = parser.parse(cmdArgs, args);

      this.fullBackup = cmd.hasOption("full-backup");
      this.configFile = getConfigFile(cmd);
      this.backupDir = getBackupDir(cmd);
      this.archiveType = cmd.getOptionValue("archive");
   }


   private String getConfigFile(CommandLine cmd) {
      if (Tools.isSet(cmd.getOptionValue("config-file")))
         return cmd.getOptionValue("config-file");

      switch (OsType.getOsType()) {
         case LINUX:
         case MACOS:
         case UNKNOWN:
            return "/etc/" + BackupBase.BACKUP_CONFIG_FILENAME;

         case WINDOWS:
            return "C:\\" + BackupBase.BACKUP_CONFIG_FILENAME;
      }

      return "/etc/" + BackupBase.BACKUP_CONFIG_FILENAME;
   }


   private String getBackupDir(CommandLine cmd) {
      if (Tools.isSet(cmd.getOptionValue("backup-dir")))
         return cmd.getOptionValue("backup-dir");

      switch (OsType.getOsType()) {
         case LINUX:
         case MACOS:
         case UNKNOWN:
            return "/var/backup";

         case WINDOWS:
            return "C:\\Backup";
      }

      return "/var/backup";
   }


   public static void main(String[] args) {
      new Backup(args);
   }
}
