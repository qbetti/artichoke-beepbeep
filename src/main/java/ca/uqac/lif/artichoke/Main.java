package ca.uqac.lif.artichoke;

import ca.uqac.lif.artichoke.exceptions.BadPassphraseException;
import ca.uqac.lif.artichoke.exceptions.PrivateKeyDecryptionException;
import ca.uqac.lif.artichoke.keyring.Keyring;
import ca.uqac.lif.cep.Connector;
import ca.uqac.lif.cep.io.Print;
import ca.uqac.lif.cep.tmf.Pump;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.security.Security;
import java.util.*;
import java.util.List;


public class Main {
    static JRadioButton fromPASFileRadioButton;
    static JRadioButton fromMetadataRadioButton;
    static JRadioButton fromWebSocketRadioButton;
    static DefaultListModel checkboxListModel;
    static Map<String, byte[]> keysMap = new HashMap<>();

    public static void main(String[] args) {

        Security.addProvider(new BouncyCastleProvider());
        JFrame myFrame = new JFrame("PWA opener");
        JButton importFileButton = new JButton("import keyring");

        //checkbox list
        JList checkBoxList = new CheckBoxList();
        checkboxListModel = new DefaultListModel();
        checkBoxList.setModel(checkboxListModel);


        //radio buttons
        JPanel radButtonsPanel = new JPanel(new GridLayout());
        fromPASFileRadioButton = new JRadioButton("from PAS file");
        fromMetadataRadioButton = new JRadioButton("From file metadata");
        fromWebSocketRadioButton = new JRadioButton("From web socket");

        ButtonGroup selectionGroup = new ButtonGroup();

        selectionGroup.add(fromPASFileRadioButton);
        selectionGroup.add(fromMetadataRadioButton);
        selectionGroup.add(fromWebSocketRadioButton);

        radButtonsPanel.add(fromMetadataRadioButton);
        radButtonsPanel.add(fromPASFileRadioButton);
        radButtonsPanel.add(fromWebSocketRadioButton);

        //text area
        JTextArea outputTextArea = new JTextArea();
        outputTextArea.setEditable(false);
        outputTextArea.insert("Output will be shown here", 0);


        //decrypt PAS button
        JButton decrypnPASButton = new JButton("decrypt sequence");


        //adding the components
        myFrame.add(importFileButton);
        myFrame.add(checkBoxList);
        myFrame.add(radButtonsPanel);
        myFrame.add(decrypnPASButton);
        myFrame.add(outputTextArea);

        GridLayout myManager = new GridLayout(5, 0);

        myFrame.setSize(400, 500);//400 width and 500 height
        myFrame.setLayout(myManager);//using no layout managers

        myFrame.setVisible(true);//making the frame visible


        importFileButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JFileChooser fc = new JFileChooser();

                int i = fc.showOpenDialog(myFrame);
                if (i == JFileChooser.APPROVE_OPTION) {
                    File keyringFile = fc.getSelectedFile();
                    String filepath = keyringFile.getPath();

                    try {
                        BufferedReader br = new BufferedReader(new FileReader(filepath));
                        String s1 = "", s2 = "";
                        while ((s1 = br.readLine()) != null) {
                            s2 += s1 + "\n";
                        }
                        br.close();


                        //the password popup
                        JPanel panel = new JPanel();
                        JLabel label = new JLabel("Enter a password:");
                        JPasswordField pass = new JPasswordField(10);
                        panel.add(label);
                        panel.add(pass);
                        String[] options = new String[]{"OK", "Cancel"};
                        int option = JOptionPane.showOptionDialog(null, panel, "Import keyring",
                                JOptionPane.NO_OPTION, JOptionPane.PLAIN_MESSAGE,
                                null, options, options[1]);
                        if (option == 0) // pressing OK button
                        {
                            char[] password = pass.getPassword();

                            Keyring importedKeyring = Keyring.loadFromFile(keyringFile, new String(password));
                            List<String> keyringGroupe = importedKeyring.getGroupIds();
                            for (String groupId : keyringGroupe) {
                                if (!keysMap.containsKey(groupId)) {
                                    keysMap.put(groupId, importedKeyring.retrieveGroupKey(groupId));

                                    Component myComponent = new JCheckBox(groupId);
                                    checkboxListModel.addElement(myComponent);
                                }
                            }
                        }

                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
            }
        });


        decrypnPASButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

                try {
                    DecryptPAS();
                } catch (IOException ex) {
                    ex.printStackTrace();
                }

            }
        });

    }

    static private void DecryptPAS() throws IOException {
        if (fromPASFileRadioButton.isSelected()) {
            System.out.println("from pas file");
            DecryptFromFASFile();
        } else if (fromMetadataRadioButton.isSelected()) {
            System.out.println("from metadata");
        } else if (fromWebSocketRadioButton.isSelected()) {
            System.out.println("from websocket");
        } else {
            System.out.println("no radio button selected");
        }
    }

    static private void DecryptFromFASFile() throws IOException {

        Map<String, byte[]> selectedKeysMap = new HashMap<>();


        int a = checkboxListModel.getSize();
        for (int i = 0; i < checkboxListModel.getSize(); i++) {
            JCheckBox b = (JCheckBox) checkboxListModel.getElementAt(i);
            if (b.isSelected()) {
                System.out.println(b.getText() + " is selected");
                String groupId = b.getText();
                byte[] key = keysMap.get(groupId);
                selectedKeysMap.put(groupId, key);
            }
        }


        PasFileReader reader = new PasFileReader();
        ParseActionsToStream parser = new ParseActionsToStream(selectedKeysMap);
        Connector.connect(reader, 0 , parser, 0);


        //Need a pump to automate the process
        Pump activator = new Pump(0);
        Connector.connect(parser, activator);

        Print p = new Print().setSeparator("\n\n");
        Connector.connect(activator, p);
        System.out.println("running pump");
        activator.run();
        System.out.println("end reached");
    }

}
