package de.mss.backup;


import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.logging.Level;

import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.ArchiveOutputStream;
import org.apache.commons.compress.utils.IOUtils;

import de.mss.configtools.ConfigFile;
import de.mss.configtools.XmlConfigFile;
import de.mss.logging.BaseLogger;
import de.mss.logging.LoggingFactory;
import de.mss.utils.Tools;
import de.mss.utils.os.OsType;

public abstract class BackupBase {

   public static final String BACKUP_CONFIG_FILENAME = "backup-conf.xml";

   private BaseLogger         logger                 = null;

   private String             configFile             = null;
   protected String           backupDir              = null;
   private boolean            fullBackup             = false;

   private ConfigFile         cfg                    = null;


   public BackupBase() {}


   public void doBackup(String c, String b, boolean full) {
      String loggingId = Tools.getId(new Throwable());
      try {
         getLogger().setLevel(Level.INFO);

         this.configFile = c;
         this.backupDir = b;
         this.fullBackup = full;
         
         new File(this.backupDir).mkdirs();

         cfg = new XmlConfigFile(this.configFile);
         cfg = readUserConfigs(cfg);

         doBackup(cfg);
      }
      catch (Exception e) {
         getLogger().log(loggingId, Level.SEVERE, e);
      }
   }


   private void doBackup(ConfigFile cfg) throws IOException {

      ArrayList<String> cfgBackupKeys = new ArrayList<>();
      for (String key : cfg.getKeys()) {
         if (key.startsWith("backup.") && key.endsWith(".files"))
            cfgBackupKeys.add(key.substring(0, key.lastIndexOf(".")));
      }

      for (String key : cfgBackupKeys)
         workBackup(cfg, key);

   }


   protected abstract ArchiveOutputStream getOutStream(String filename) throws FileNotFoundException, IOException;


   protected abstract ArchiveEntry getOutStreamEntry(ArchiveOutputStream outStream, File file, String name) throws IOException;


   protected abstract String getFileExtension();


   private void workBackup(ConfigFile cfg, String key) throws IOException {
      String loggingId = Tools.getId(new Throwable());
      getLogger().log(loggingId, Level.INFO, "running Backup for " + key);

      File basePath = new File(cfg.getValue(key + ".rootPath", "."));
      String tmpFilename = backupDir + File.separator + cfg.getValue(key + ".backupName", "backup") + ".tmp." + getFileExtension();
      ArchiveOutputStream outStream = getOutStream(tmpFilename);

      workBackup(
            loggingId,
            outStream,
            basePath,
            basePath,
            cfg.getValue(key + ".files", "*").split(","),
            cfg.getValue(key + ".exclude", "").split(","),
            getLastFullBackup(cfg, key));

      long bytesWritten = outStream.getBytesWritten();
      outStream.flush();
      outStream.close();

      if (bytesWritten <= 0) {
         getLogger().log(loggingId, Level.INFO, "keine Daten -> Backup wird nicht gespeichert");
         new File(tmpFilename).delete();
         return;
      }

      getLogger().log(loggingId, Level.INFO, "Backup wird gespichert");
      SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd-HHmmss");
      String filename = backupDir
            + File.separator
            + cfg.getValue(key + ".backupName", "backup")
            + (this.fullBackup || getLastFullBackup(cfg, key).getTime() == 0 ? ".fullbackup." : ".")
            + sdf.format(new java.util.Date())
            + "."
            + getFileExtension();

      new File(tmpFilename).renameTo(new File(filename));
   }


   private Date getLastFullBackup(ConfigFile cfg2, String key) {
      if (this.fullBackup)
         return new java.util.Date(0);

      File backup = new File(this.backupDir);

      long lastBackup = 0;
      for (File f : backup.listFiles(new FullBackupFileFilter(cfg.getValue(key + ".backupName", "backup")))) {
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
               throws IOException {

      getLogger().log(loggingId, Level.INFO, "working file/directory " + currentPath.getName());

      if (!currentPath.exists()) {
         getLogger().log(loggingId, Level.INFO, "file/directory " + currentPath.getName() + " does not exists");
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
         throws IOException {
      String name = f.getAbsolutePath().substring(basePath.getAbsolutePath().length() + 1);
      ArchiveEntry entry = getOutStreamEntry(outStream, f, name);

      if (entry != null) {
         outStream.putArchiveEntry(entry);
         outStream.closeArchiveEntry();
      }

      workBackup(loggingId, outStream, basePath, f, includes, excludes, lastModified);
   }


   private void backupFile(String loggingId, ArchiveOutputStream outStream, File basePath, File f) throws IOException {
      getLogger().log(loggingId, Level.INFO, "backing up file " + f.getAbsolutePath());
      String name = f.getAbsolutePath().substring(basePath.getAbsolutePath().length() + 1);
      ArchiveEntry entry = getOutStreamEntry(outStream, f, name);

      if (entry != null) {
         outStream.putArchiveEntry(entry);
         BufferedInputStream in = new BufferedInputStream(new FileInputStream(f));
         IOUtils.copy(in, outStream);
         in.close();

         outStream.closeArchiveEntry();
      }
   }


   private ConfigFile readUserConfigs(ConfigFile cfg) throws IOException {
      if (!"J".equalsIgnoreCase(cfg.getValue("backup.system.readUserDirs", "N")))
         return cfg;

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
      }

      for (File f : homeDir.listFiles()) {
         if (!f.isDirectory())
            continue;

         File userCfgFile = new File(f.getAbsolutePath() + File.separator + BACKUP_CONFIG_FILENAME);
         if (!userCfgFile.exists() || !userCfgFile.isFile())
            continue;

         cfg.loadConfig(userCfgFile, true);
      }

      return cfg;
   }


   private BaseLogger getLogger() {
      if (logger != null)
         return logger;

      logger = LoggingFactory.createInstance("system", new BaseLogger("system"));

      return logger;
   }


   private boolean isModified(File f, java.util.Date lastBackup) {
      return (lastBackup == null || f.lastModified() >= lastBackup.getTime());
   }
}
