package ca.uqac.lif.artichoke;

import ca.uqac.lif.artichoke.exceptions.BadPassphraseException;
import ca.uqac.lif.artichoke.exceptions.PrivateKeyDecryptionException;
import ca.uqac.lif.artichoke.keyring.Keyring;
import ca.uqac.lif.cep.Connector;
import ca.uqac.lif.cep.io.Print;
import ca.uqac.lif.cep.tmf.Pump;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

import javax.crypto.spec.PSource;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.security.Security;
import java.util.*;
import java.util.List;

import static jdk.nashorn.internal.runtime.regexp.joni.Syntax.Java;

public class Main {
    static JRadioButton fromPASFileRadioButton;
    static JRadioButton fromMetadataRadioButton;
    static JRadioButton fromWebSocketRadioButton;
    static DefaultListModel checkboxListModel;
    static Map<String, byte[]> keysMap = new HashMap<>();
    static List<JCheckBox> groupsList = new ArrayList<>();

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
                        }br.close();


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
                                    groupsList.add(myComponent);
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


        // ParseUsingHashMap();
        //ParseUsingSingleKey();

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

        String s2 = "";
        Frame fileSelecterFrame = new Frame();


        JList<String> list = new JList<>();

        int a = list.getModel().getSize();
        for(int i = 0; i< list.getModel().getSize();i++){
            System.out.println(list.getModel().getElementAt(i));
        }
        
        
        
        
        

        JFileChooser fc = new JFileChooser();
        int i = fc.showOpenDialog(fileSelecterFrame);
        if (i == JFileChooser.APPROVE_OPTION) {

            File keyringFile = fc.getSelectedFile();
            String filepath = keyringFile.getPath();

            try {
                BufferedReader br = new BufferedReader(new FileReader(filepath));
                String s1 = "";
                while ((s1 = br.readLine()) != null) {
                    s2 += s1 + "\n";
                }
                br.close();


                String PAS_File = "./example/patient_file.pas";

                Scanner scanner = null;
                try {
                    scanner = new Scanner(new File(PAS_File));
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }

                String encodedHistory = scanner.nextLine();
                scanner.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        FromPASFile reader = new FromPASFile(s2);
    }

   /*private static void ParseUsingHashMap() {
        Security.addProvider(new BouncyCastleProvider());

        String KR_NURSE = "./example/kr-employee.json";
        String KR_INSURANCE = "./example/kr-insurance.json";

        File nurseFile = new File(KR_NURSE);
        File insuranceFile = new File(KR_INSURANCE);

        final String PWD_KR_NURSE = "nurse";
        final String PWD_KR_INSURANCE = "insurance";


        //Loading the keyrings from file
        Keyring nurseKeyring = null;
        try {
            nurseKeyring = Keyring.loadFromFile(nurseFile, PWD_KR_NURSE);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (PrivateKeyDecryptionException e) {
            e.printStackTrace();
        } catch (BadPassphraseException e) {
            e.printStackTrace();
        }

        Keyring insuranceKeyring = null;
        try {
            insuranceKeyring = Keyring.loadFromFile(insuranceFile, PWD_KR_INSURANCE);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (PrivateKeyDecryptionException e) {
            e.printStackTrace();
        } catch (BadPassphraseException e) {
            e.printStackTrace();
        }

        //adding the keyrings to the hashmap
        Map<String, byte[]> keysMap = new HashMap<>();
        keysMap.put("insurance", insuranceKeyring.retrieveGroupKey("insurance"));
        keysMap.put("employees", nurseKeyring.retrieveGroupKey("employees"));
        ParseActionsToStream ActionsStream = new ParseActionsToStream(keysMap);


        //Need a pump to automate the process
        Pump activator = new Pump(0);
        Connector.connect(ActionsStream, activator);

        Print p = new Print().setSeparator("\n\n");
        Connector.connect(activator, p);
        activator.run();
    }*/

    public static void ParseUsingSingleKey() {

        Security.addProvider(new BouncyCastleProvider());

        String KR_PATIENT = "./example/kr-patient.json";
        final String PWD_KR_PATIENT = "patient";
        File patientFile = new File(KR_PATIENT);

        Keyring ownerKeyring = null;
        try {
            ownerKeyring = Keyring.loadFromFile(patientFile, PWD_KR_PATIENT);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (PrivateKeyDecryptionException e) {
            e.printStackTrace();
        } catch (BadPassphraseException e) {
            e.printStackTrace();
        }


        ParseActionsToStream ActionsStream = new ParseActionsToStream(ownerKeyring);

        //Need a pump to automate the process
        Pump activator = new Pump(0);
        Connector.connect(ActionsStream, activator);

        Print p = new Print().setSeparator("\n\n");
        Connector.connect(activator, p);
        activator.run();
    }

}
