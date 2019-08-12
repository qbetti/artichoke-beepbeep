package ca.uqac.lif.artichoke;

import ca.uqac.lif.artichoke.keyring.Keyring;
import ca.uqac.lif.cep.Connector;
import ca.uqac.lif.cep.Pullable;
import ca.uqac.lif.cep.io.ReadLines;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

import javax.swing.*;
import javax.swing.table.DefaultTableColumnModel;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;
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
    private static Object[][] data = new Object[][]{};
    private static JTable table;
    private static DefaultTableModel myTableModel;

    public static void main(String[] args) {
        String[] tableColumns = new String[]{"Field", "action", "content", "actor", "groupID"};
        myTableModel = new DefaultTableModel(tableColumns, 0);


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


        //Table

        TableColumnModel columnModel = new DefaultTableColumnModel();

        table = new JTable(myTableModel);

        //decrypt PAS button

        JButton decrypnPASButton = new JButton("decrypt sequence");


        //adding the components
        myFrame.add(importFileButton);
        myFrame.add(new JScrollPane(checkBoxList));
        myFrame.add(radButtonsPanel);
        myFrame.add(decrypnPASButton);
        myFrame.add(new JScrollPane(table));


        GridLayout myManager = new GridLayout(5, 0);

        myFrame.setSize(400, 500);//400 width and 500 height
        myFrame.setLayout(myManager);//using no layout managers
        myFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

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
                    CheckSourceOfPAS();
                } catch (IOException ex) {
                    ex.printStackTrace();
                }

            }
        });

    }

    static private void CheckSourceOfPAS() throws IOException {
        if (fromPASFileRadioButton.isSelected()) {
            System.out.println("from pas file");
            DecryptFromDotFASFile();
        } else if (fromMetadataRadioButton.isSelected()) {
            System.out.println("from metadata");
        } else if (fromWebSocketRadioButton.isSelected()) {
            System.out.println("from websocket");
        } else {
            System.out.println("no radio button selected");
        }
    }

    static private void DecryptFromDotFASFile() throws IOException {
        String filepath;
        Map<String, byte[]> selectedKeysMap = new HashMap<>();

        int a = checkboxListModel.getSize();
        for (int i = 0; i < checkboxListModel.getSize(); i++) {
            JCheckBox b = (JCheckBox) checkboxListModel.getElementAt(i);
            if (b.isSelected()) {
                String groupId = b.getText();
                byte[] key = keysMap.get(groupId);
                selectedKeysMap.put(groupId, key);
            }
        }

        Frame fileSelecterFrame = new Frame();

        JFileChooser fc = new JFileChooser();
        int i = fc.showOpenDialog(fileSelecterFrame);
        if (i == JFileChooser.APPROVE_OPTION) {
            File keyringFile = fc.getSelectedFile();
            filepath = keyringFile.getPath();

            ReadLines reader = new ReadLines(new FileInputStream(filepath));
            ParseActionsToStream parser = new ParseActionsToStream(selectedKeysMap);
            Connector.connect(reader, parser);

            Pullable p = parser.getPullableOutput();
            while (p.hasNext()) {
                WrappedAction latestAction = (WrappedAction) p.pull();
                System.out.println(latestAction.toString());
                System.out.println("Before add row");

                myTableModel.addRow(new Object[]{latestAction.getAction().getTarget(), latestAction.getAction().getType(), latestAction.getAction().getValue(), latestAction.getPeer().getName(), latestAction.getGroupId()});
                System.out.println("After add row");
            }
            ((DefaultTableModel) myTableModel).fireTableDataChanged();
            System.out.println("done");

        }
    }

}
