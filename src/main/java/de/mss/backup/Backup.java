package de.mss.backup;

import java.util.logging.Level;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import de.mss.configtools.ConfigFile;
import de.mss.logging.BaseLogger;
import de.mss.utils.Tools;
import de.mss.utils.os.OsType;

public class Backup {

   private String  archiveType = null;
   private Level   logLevel    = BaseLogger.getLevelInfo();

   protected String     configFile  = null;
   protected String     backupDir   = null;
   protected boolean    fullBackup  = false;

   protected ConfigFile cfg         = null;


   public Backup(String[] args) {
      try {
         init(args);
      }
      catch (ParseException e) {
         e.printStackTrace();
      }
   }


   public void doBackup() {
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
         backup.setLoglevel(this.logLevel);
         backup.doBackup(this.configFile, this.backupDir, this.fullBackup, this.cfg);
      }
   }


   public void setArchiveType(String a) {
      this.archiveType = a;
   }


   public void setLogLevel(Level l) {
      this.logLevel = l;
   }


   public String getArchiveType() {
      return this.archiveType;
   }


   public Level getLogLevel() {
      return this.logLevel;
   }


   private void init(String[] args) throws ParseException {
      Options cmdArgs = new Options();

      Option confFile = new Option("f", "config-file", true, "configuration file");
      confFile.setRequired(false);
      cmdArgs.addOption(confFile);

      Option backDir = new Option("d", "backup-dir", true, "backup directory");
      backDir.setRequired(false);
      cmdArgs.addOption(backDir);

      Option forceFullBackup = new Option("fb", "full-backup", false, "force full backup");
      forceFullBackup.setRequired(false);
      cmdArgs.addOption(forceFullBackup);

      Option archType = new Option("a", "archive", true, "backup directory");
      archType.setRequired(false);

      Option debug = new Option("dd", "debug", false, "debug info");
      debug.setRequired(false);
      cmdArgs.addOption(debug);

      Option verbose = new Option("vv", "verbose", false, "be verbose");
      verbose.setRequired(false);
      cmdArgs.addOption(verbose);

      cmdArgs.addOption(archType);

      CommandLineParser parser = new DefaultParser();
      CommandLine cmd = parser.parse(cmdArgs, args);

      this.fullBackup = cmd.hasOption("full-backup");
      this.configFile = getConfigFile(cmd);
      this.backupDir = getBackupDir(cmd);
      this.archiveType = getArchiveType(cmd);

      if (cmd.hasOption("verbose"))
         this.logLevel = BaseLogger.getLevelVerbose();
      else if (cmd.hasOption("debug"))
         this.logLevel = BaseLogger.getLevelDebug();
   }


   private String getArchiveType(CommandLine cmd) {
      if (Tools.isSet(cmd.getOptionValue("archive")))
         return cmd.getOptionValue("archive");

      switch (OsType.getOsType()) {
         case LINUX:
         case MACOS:
         case UNKNOWN:
         default:
            return "tarbz2";

         case WINDOWS:
            return "zip";
      }
   }


   private String getConfigFile(CommandLine cmd) {
      if (Tools.isSet(cmd.getOptionValue("config-file")))
         return cmd.getOptionValue("config-file");

      switch (OsType.getOsType()) {
         case LINUX:
         case MACOS:
         case UNKNOWN:
         default:
            return "/etc/" + BackupBase.BACKUP_CONFIG_FILENAME;

         case WINDOWS:
            return "C:\\" + BackupBase.BACKUP_CONFIG_FILENAME;
      }
   }


   private String getBackupDir(CommandLine cmd) {
      if (Tools.isSet(cmd.getOptionValue("backup-dir")))
         return cmd.getOptionValue("backup-dir");

      switch (OsType.getOsType()) {
         case LINUX:
         case MACOS:
         case UNKNOWN:
         default:
            return "/var/backup";

         case WINDOWS:
            return "C:\\Backup";
      }
   }


   public String getConfigFile() {
      return this.configFile;
   }


   public void setConfigFile(String s) {
      this.configFile = s;
   }


   public boolean getFullBackup() {
      return this.fullBackup;
   }


   public void setFullBackup(boolean f) {
      this.fullBackup = f;
   }


   public String getBackupDir() {
      return this.backupDir;
   }


   public void setBackupDir(String b) {
      this.backupDir = b;
   }


   public ConfigFile getCfg() {
      return this.cfg;
   }


   public void setCfg(ConfigFile c) {
      this.cfg = c;
   }


   public static void main(String[] args) {
      new Backup(args).doBackup();
   }
}
