package de.mss.backup;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.net.MalformedURLException;
import java.util.ArrayList;

import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

import de.mss.configtools.ConfigFile;
import de.mss.configtools.XmlConfigFile;
import de.mss.utils.Tools;

public class JBackup extends JFrame implements WindowListener, MouseListener, ActionListener {

   /**
    * 
    */
   private static final long serialVersionUID = -4042952905140396253L;

   private Backup backup = null;
   private ConfigFile        cfg                    = null;

   private JLabel            labelCurrentConfigFile = new JLabel();
   private JLabel            labelCurrentBackupDir  = new JLabel();

   private JComboBox<String> comboArchiveType       = new JComboBox<>();
   private JComboBox<String> comboBackupSection     = new JComboBox<>();

   private JTextField        editRootPath           = new JTextField(30);
   private JTextField        editBackupName         = new JTextField(30);
   private JTextField        editFiles              = new JTextField(30);
   private JTextField        editExcludes           = new JTextField(30);

   private JTextArea         editLog                = new JTextArea(15, 80);


   JCheckBox checkFullBackup = new JCheckBox();


   public JBackup(String[] args) {
      this.backup = new Backup(args);

      //      File resources = new File("resources");
      //      URL[] urls = {resources.toURI().toURL()};
      //      ClassLoader loader = new URLClassLoader(urls);
      //      this.messages = ResourceBundle.getBundle("MssBackupBundle", new Locale("de", "DE"), loader);
      //      System.out.println(this.messages.getString("TXT_GREETER"));

      initApp();
   }


   private void initApp() {
      this.setVisible(false);
      this.setSize(800, 600);

      this.setTitle("MssBackup");

      GroupLayout layout = new GroupLayout(this.getContentPane());

      JLabel labelConfigFile = new JLabel("Konfiguration");
      JLabel labelBackupDir = new JLabel("Ziel");
      JLabel labelFullBackup = new JLabel("Komplettsicherung");
      JLabel labelArchiveType = new JLabel("Archivtyp");

      this.comboArchiveType.addItem("tar");
      this.comboArchiveType.addItem("targz");
      this.comboArchiveType.addItem("tgz");
      this.comboArchiveType.addItem("tarbz2");
      this.comboArchiveType.addItem("zip");

      JLabel labelBackupSection = new JLabel("Abschnitt");
      JLabel labelRootPath = new JLabel("Basispfad");
      JLabel labelBackupName = new JLabel("Name");
      JLabel labelFiles = new JLabel("Dateien");
      JLabel labelExcludes = new JLabel("Ausschlieﬂen");

      JButton buttonDefaultExcludes = new JButton("Standard");
      buttonDefaultExcludes.setActionCommand("defaultExcludes");
      buttonDefaultExcludes.addActionListener(this);

      JButton buttonLoad = new JButton("Laden");
      buttonLoad.setActionCommand("loadSection");
      buttonLoad.addActionListener(this);
      JButton buttonSave = new JButton("Speichern");
      buttonSave.setActionCommand("saveSection");
      buttonSave.addActionListener(this);
      JButton buttonBackup = new JButton("Sicherung");
      buttonBackup.setActionCommand("doBackup");
      buttonBackup.addActionListener(this);
      JButton buttonAddSection = new JButton("+");
      buttonAddSection.setActionCommand("addSection");
      buttonAddSection.addActionListener(this);
      JButton buttonDelSection = new JButton("-");
      buttonDelSection.setActionCommand("delSection");
      buttonDelSection.addActionListener(this);

      this.comboBackupSection.addItemListener(new ItemListener() {

         @Override
         public void itemStateChanged(ItemEvent arg0) {
            loadBackupSection();
         }
      });


      JScrollPane scrollLog = new JScrollPane(this.editLog);

      //@formatter:off
      // horizontal
      layout.setHorizontalGroup(
            layout.createParallelGroup(GroupLayout.Alignment.LEADING)
               .addGroup(layout.createSequentialGroup()
                  .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                        .addComponent(labelConfigFile)
                        .addComponent(labelBackupDir)
                        .addComponent(labelFullBackup)
                        .addComponent(labelArchiveType)
                        )
                  .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                        .addComponent(this.labelCurrentConfigFile)
                        .addComponent(this.labelCurrentBackupDir)
                        .addComponent(this.checkFullBackup)
                        .addComponent(this.comboArchiveType)
                        )
                  .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                        .addComponent(labelBackupSection)
                        .addComponent(labelRootPath)
                        .addComponent(labelBackupName)
                        .addComponent(labelFiles)
                        .addComponent(labelExcludes)
                        )
                  .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                        .addGroup(layout.createSequentialGroup()
                              .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                                    .addComponent(this.comboBackupSection)
                                    )
                              .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                                    .addComponent(buttonAddSection)
                                    )
                              .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                                    .addComponent(buttonDelSection)
                                    )
                              )
                        .addComponent(this.editRootPath)
                        .addComponent(this.editBackupName)
                        .addComponent(this.editFiles)
                        .addGroup(layout.createSequentialGroup()
                              .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                                    .addComponent(this.editExcludes)
                                    )
                              .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                                    .addComponent(buttonDefaultExcludes)
                                    )
                              )
                        )
                  .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                        .addComponent(buttonLoad)
                        .addComponent(buttonSave)
                        .addGap(2)
                        .addComponent(buttonBackup)
                        )
                  )
               .addComponent(scrollLog)
            );

      layout.linkSize(SwingConstants.HORIZONTAL, buttonLoad, buttonSave, buttonBackup);
      
      // vertical
      layout.setVerticalGroup(
            layout.createSequentialGroup()
               .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                     .addComponent(labelConfigFile)
                     .addComponent(this.labelCurrentConfigFile)
                     .addComponent(labelBackupSection)
                     .addGroup(layout.createSequentialGroup()
                           .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                 .addComponent(this.comboBackupSection)
                                 .addComponent(buttonAddSection)
                                 .addComponent(buttonDelSection)
                                 )
                           )
                     .addComponent(buttonLoad)
                     )
               .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                     .addComponent(labelBackupDir)
                     .addComponent(this.labelCurrentBackupDir)
                     .addComponent(labelRootPath)
                     .addComponent(this.editRootPath)
                     .addComponent(buttonSave)
                     )
               .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                     .addComponent(labelFullBackup)
                     .addComponent(this.checkFullBackup)
                     .addComponent(labelBackupName)
                     .addComponent(this.editBackupName)
                     )
               .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                     .addComponent(labelArchiveType)
                     .addComponent(this.comboArchiveType)
                     .addComponent(labelFiles)
                     .addComponent(this.editFiles)
                     )
               .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                     .addGap(2)
                     .addComponent(labelExcludes)
                     .addGroup(layout.createSequentialGroup()
                           .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                 .addComponent(this.editExcludes)
                                 .addComponent(buttonDefaultExcludes)
                                 )
                           )
                     .addComponent(buttonBackup)
                     )
               .addComponent(scrollLog)
            );
      
      //@formatter:on

      layout.setAutoCreateGaps(true);
      layout.setAutoCreateContainerGaps(true);

      this.getContentPane().setLayout(layout);


      this.addWindowListener(this);
      this.addMouseListener(this);

      showCommonInfo();
      parseBackupConfig();

      this.setVisible(true);
   }


   private void showCommonInfo() {
      this.labelCurrentConfigFile.setText(this.backup.getConfigFile());
      this.labelCurrentBackupDir.setText(this.backup.getBackupDir());
      this.checkFullBackup.setSelected(this.backup.getFullBackup());
      this.comboArchiveType.setSelectedItem(this.backup.getArchiveType());
   }


   private void parseBackupConfig() {
      ArrayList<String> list = new ArrayList<>();
      this.cfg = new XmlConfigFile(this.backup.getConfigFile());
      for (String key : this.cfg.getKeys()) {
         String[] k = key.split("\\.");
         if (k.length >= 3) {
            boolean found = false;
            for (String l : list)
               if (l.equals(k[2])) found = true;

            if (!found)
               list.add(k[2]);
         }
      }

      this.comboBackupSection.removeAllItems();
      for (String key : list)
         this.comboBackupSection.addItem(key);

      loadBackupSection();
   }


   void loadBackupSection() {
      String baseKey = getBaseKey();
      this.editBackupName.setText(this.cfg.getValue(baseKey + ".backupName", ""));
      this.editRootPath.setText(this.cfg.getValue(baseKey + ".rootPath", ""));
      this.editFiles.setText(this.cfg.getValue(baseKey + ".files", "*"));
      this.editExcludes.setText(this.cfg.getValue(baseKey + ".exclude", ""));
   }


   private void saveBackupSection() {
      String baseKey = getBaseKey();

      this.cfg.insertKeyValue(baseKey + ".backupName", this.editBackupName.getText());
      this.cfg.insertKeyValue(baseKey + ".exclude", this.editExcludes.getText());
      this.cfg.insertKeyValue(baseKey + ".files", this.editFiles.getText());
      this.cfg.insertKeyValue(baseKey + ".rootPath", this.editRootPath.getText());
   }


   private String getBaseKey() {
      return "backup." + System.getProperty("user.name") + "." + this.comboBackupSection.getSelectedItem();
   }


   @SuppressWarnings("unused")
   public static void main(String[] args) throws MalformedURLException {
      new JBackup(args);
   }


   @Override
   public void windowOpened(WindowEvent e) {
   }


   @Override
   public void windowClosing(WindowEvent e) {
      this.dispose();
   }


   @Override
   public void windowClosed(WindowEvent e) {
   }


   @Override
   public void windowIconified(WindowEvent e) {
   }


   @Override
   public void windowDeiconified(WindowEvent e) {}


   @Override
   public void windowActivated(WindowEvent e) {
   }


   @Override
   public void windowDeactivated(WindowEvent e) {}


   @Override
   public void mouseClicked(MouseEvent e) {}


   @Override
   public void mousePressed(MouseEvent e) {
   }


   @Override
   public void mouseReleased(MouseEvent e) {}


   @Override
   public void mouseEntered(MouseEvent e) {
   }


   @Override
   public void mouseExited(MouseEvent e) {}


   @Override
   public void actionPerformed(ActionEvent e) {
      switch (e.getActionCommand()) {
         case "addSection":
            addBackupSection();
            break;

         case "defaultExcludes":
            setDefaultExcludes();
            break;

         case "delSection":
            delBackupSection();
            break;

         case "doBackup":
            doBackup();
            break;

         case "loadSection":
            loadBackupSection();
            break;

         case "saveSection":
            saveBackupSection();
            break;

         default:
            break;
      }
   }


   private void doBackup() {
      saveBackupSection();
      this.backup.setArchiveType((String)this.comboArchiveType.getSelectedItem());
      this.backup.setBackupDir(this.labelCurrentBackupDir.getText());
      this.backup.setCfg(this.cfg);
      this.backup.setFullBackup(this.checkFullBackup.isSelected());

      this.backup.doBackup();
   }


   private void setDefaultExcludes() {
      this.editExcludes.setText("*.bak,*~,");
   }


   private void delBackupSection() {
      String baseKey = getBaseKey();
      this.cfg.removeKey(baseKey);

      String backupSection = (String)this.comboBackupSection.getSelectedItem();
      this.comboBackupSection.removeItem(backupSection);

      loadBackupSection();
   }


   private void addBackupSection() {
      String backupSection = JOptionPane.showInputDialog("Name neuer Abschnitt: ");
      if (!Tools.isSet(backupSection))
         return;

      this.comboBackupSection.addItem(backupSection);
      this.comboBackupSection.setSelectedItem(backupSection);

      this.editBackupName.setText(System.getProperty("user.name") + "-" + backupSection);
      this.editFiles.setText("*");
      this.editRootPath.setText("");
      setDefaultExcludes();
   }
}
