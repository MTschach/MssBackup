package de.mss.backup;


import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.logging.Level;

import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.ArchiveOutputStream;
import org.apache.commons.compress.utils.IOUtils;

import de.mss.backup.exception.ErrorCodes;
import de.mss.configtools.ConfigFile;
import de.mss.configtools.XmlConfigFile;
import de.mss.logging.BaseLogger;
import de.mss.logging.LoggingFactory;
import de.mss.utils.Tools;
import de.mss.utils.exception.MssException;
import de.mss.utils.os.OsType;

public abstract class BackupBase {

   public static final String BACKUP_CONFIG_FILENAME = "backup-conf.xml";

   private BaseLogger         logger                 = null;

   private String             configFile             = null;
   protected String           backupDir              = null;
   private boolean            fullBackup             = false;

   private ConfigFile         cfg                    = null;


   public BackupBase() {
      getLogger().setLevel(Level.INFO);

   }


   public void setLoglevel(Level l) {
      getLogger().setLevel(l);
   }


   public void doBackup(String c, String b, boolean full) {
      String loggingId = Tools.getId(new Throwable());
      try {
         this.configFile = c;
         this.backupDir = b;
         this.fullBackup = full;

         new File(this.backupDir).mkdirs();

         this.cfg = new XmlConfigFile(this.configFile);
         this.cfg = readUserConfigs(this.cfg);

         doBackup(this.cfg);
      }
      catch (Exception e) {
         getLogger().logError(loggingId, e);
      }
   }


   private void doBackup(ConfigFile config) throws MssException {

      ArrayList<String> cfgBackupKeys = new ArrayList<>();
      for (String key : config.getKeys()) {
         if (key.startsWith("backup.") && key.endsWith(".files"))
            cfgBackupKeys.add(key.substring(0, key.lastIndexOf(".")));
      }

      for (String key : cfgBackupKeys)
         workBackup(config, key);

   }


   protected abstract ArchiveOutputStream getOutStream(String filename) throws MssException;


   protected abstract ArchiveEntry getOutStreamEntry(ArchiveOutputStream outStream, File file, String name) throws MssException;


   protected abstract String getFileExtension();


   @SuppressWarnings("resource")
   private void workBackup(ConfigFile config, String key) throws MssException {
      String loggingId = Tools.getId(new Throwable());
      getLogger().logInfo(loggingId, "running Backup for " + key);

      File basePath = new File(config.getValue(key + ".rootPath", "."));
      String tmpFilename = this.backupDir + File.separator + config.getValue(key + ".backupName", "backup") + ".tmp." + getFileExtension();
      ArchiveOutputStream outStream = getOutStream(tmpFilename);

      workBackup(
            loggingId,
            outStream,
            basePath,
            basePath,
            config.getValue(key + ".files", "*").split(","),
            config.getValue(key + ".exclude", "").split(","),
            getLastFullBackup(key));

      long bytesWritten = outStream.getBytesWritten();
      try {
         outStream.flush();
         outStream.close();
      }
      catch (IOException e) {
         throw new MssException(ErrorCodes.ERROR_ARCHIVE_FAILED, e, "Failed to write Archive");
      }

      if (bytesWritten <= 0) {
         getLogger().logInfo(loggingId, "keine Daten -> Backup wird nicht gespeichert");
         new File(tmpFilename).delete();
         return;
      }

      getLogger().logInfo(loggingId, "Backup wird gespichert");
      SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd-HHmmss");
      String filename = this.backupDir
            + File.separator
            + config.getValue(key + ".backupName", "backup")
            + (this.fullBackup || getLastFullBackup(key).getTime() == 0 ? ".fullbackup." : ".")
            + sdf.format(new java.util.Date())
            + "."
            + getFileExtension();

      new File(tmpFilename).renameTo(new File(filename));
   }


   private Date getLastFullBackup(String key) {
      if (this.fullBackup)
         return new java.util.Date(0);

      File backup = new File(this.backupDir);

      long lastBackup = 0;
      for (File f : backup.listFiles(new FullBackupFileFilter(this.cfg.getValue(key + ".backupName", "backup")))) {
         if (f.lastModified() > lastBackup)
            lastBackup = f.lastModified();
      }

      return new java.util.Date(lastBackup);
   }


   private
         void
         workBackup(
               String loggingId,
               ArchiveOutputStream outStream,
               File basePath,
               File currentPath,
               String[] includes,
               String[] excludes,
               java.util.Date lastModified)
               throws MssException {

      getLogger().logDebug(loggingId, "working file/directory " + currentPath.getName());

      if (!currentPath.exists()) {
         getLogger().logError(loggingId, "file/directory " + currentPath.getName() + " does not exists");
         return;
      }

      File files[] = currentPath.listFiles(new BackupFileFilter(includes, excludes));
      if (files == null)
         return;

      for (File f : files) {

         if (f.isDirectory())
            backupDirectory(loggingId, outStream, basePath, f, includes, excludes, lastModified);

         else if (f.isFile()) {
            if (isModified(f, lastModified))
               backupFile(loggingId, outStream, basePath, f);
         }
      }
   }


   private void backupDirectory(
         String loggingId,
         ArchiveOutputStream outStream,
         File basePath,
         File f,
         String[] includes,
         String[] excludes,
         Date lastModified)
         throws MssException {
      String name = f.getAbsolutePath().substring(basePath.getAbsolutePath().length() + 1);
      ArchiveEntry entry = getOutStreamEntry(outStream, f, name);

      if (entry != null) {
         try {
            outStream.putArchiveEntry(entry);
            outStream.closeArchiveEntry();
         }
         catch (IOException e) {
            throw new MssException(ErrorCodes.ERROR_ARCHIVE_FAILED, e, "Failed to archive directory '" + name + "'");
         }
      }

      workBackup(loggingId, outStream, basePath, f, includes, excludes, lastModified);
   }


   private void backupFile(String loggingId, ArchiveOutputStream outStream, File basePath, File f) throws MssException {
      getLogger().logDebug(loggingId, "backing up file " + f.getAbsolutePath());
      String name = f.getAbsolutePath().substring(basePath.getAbsolutePath().length() + 1);
      ArchiveEntry entry = getOutStreamEntry(outStream, f, name);

      if (entry != null) {
         try {
            outStream.putArchiveEntry(entry);
            @SuppressWarnings("resource")
            BufferedInputStream in = new BufferedInputStream(new FileInputStream(f));
            IOUtils.copy(in, outStream);
            in.close();

            outStream.closeArchiveEntry();
         }
         catch (IOException e) {
            throw new MssException(ErrorCodes.ERROR_ARCHIVE_FAILED, e, "Failed to archive file '" + name + "'");
         }
      }
   }


   private ConfigFile readUserConfigs(ConfigFile config) throws MssException {
      if (!"J".equalsIgnoreCase(config.getValue("backup.system.readUserDirs", "N")))
         return config;

      File homeDir = null;
      switch (OsType.getOsType()) {
         case LINUX:
         case MACOS:
         case UNKNOWN:
            homeDir = new File("/home");
            break;

         case WINDOWS:
            homeDir = new File("C:\\Users");
            break;
         default:
            throw new MssException(ErrorCodes.ERROR_UNKNOWN_OS_TYPE, "unkown/not supported OS type '" + OsType.getOsType().getName() + "'");
      }

      for (File f : homeDir.listFiles()) {
         if (!f.isDirectory())
            continue;

         File userCfgFile = new File(f.getAbsolutePath() + File.separator + BACKUP_CONFIG_FILENAME);
         if (!userCfgFile.exists() || !userCfgFile.isFile())
            continue;

         try {
            config.loadConfig(userCfgFile, true);
         }
         catch (IOException e) {
            throw new MssException(ErrorCodes.ERROR_FAILED_TO_READ_CONFIG, e, "Failed to read config '" + userCfgFile.getAbsolutePath() + '"');
         }
      }

      return config;
   }


   private BaseLogger getLogger() {
      if (this.logger != null)
         return this.logger;

      this.logger = LoggingFactory.createInstance("system", new BaseLogger("system"));

      return this.logger;
   }


   private boolean isModified(File f, java.util.Date lastBackup) {
      return (lastBackup == null || f.lastModified() >= lastBackup.getTime());
   }
}
