package ca.uqac.lif.artichoke;

import ca.uqac.lif.artichoke.exceptions.BadPassphraseException;
import ca.uqac.lif.artichoke.exceptions.GroupIdException;
import ca.uqac.lif.artichoke.exceptions.PrivateKeyDecryptionException;
import ca.uqac.lif.artichoke.keyring.Keyring;
import ca.uqac.lif.cep.Connector;
import ca.uqac.lif.cep.io.Print;
import ca.uqac.lif.cep.tmf.Pump;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

import java.io.File;
import java.io.IOException;
import java.security.Security;
import java.util.HashMap;
import java.util.Map;


public class Main {
    public static void main(String[] args) {

       // ParseUsingHashMap();
        ParseUsingSingleKey();

    }

    private static void ParseUsingHashMap() {
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
        try {
            keysMap.put("insurance", insuranceKeyring.retrieveGroupKey("insurance"));
        } catch (GroupIdException e) {
            e.printStackTrace();
        } catch (PrivateKeyDecryptionException e) {
            e.printStackTrace();
        } catch (BadPassphraseException e) {
            e.printStackTrace();
        }

        try {
            keysMap.put("employees", nurseKeyring.retrieveGroupKey("employees"));
        } catch (GroupIdException e) {
            e.printStackTrace();
        } catch (PrivateKeyDecryptionException e) {
            e.printStackTrace();
        } catch (BadPassphraseException e) {
            e.printStackTrace();
        }

        ParseActionsToStream ActionsStream = new ParseActionsToStream(keysMap);


        //Need a pump to automate the process
        Pump activator = new Pump(0);
        Connector.connect(ActionsStream, activator);

        Print p = new Print().setSeparator("\n\n");
        Connector.connect(activator, p);
        activator.run();
    }

    public static void ParseUsingSingleKey()
    {

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
