package ca.uqac.lif.artichoke;

import ca.uqac.lif.artichoke.exceptions.BadPassphraseException;
import ca.uqac.lif.artichoke.exceptions.PrivateKeyDecryptionException;
import ca.uqac.lif.artichoke.keyring.Keyring;
import ca.uqac.lif.cep.Connector;
import ca.uqac.lif.cep.GroupProcessor;
import ca.uqac.lif.cep.functions.ApplyFunction;
import ca.uqac.lif.cep.functions.UnaryFunction;
import ca.uqac.lif.cep.tmf.QueueSource;
import ca.uqac.lif.cep.util.Lists;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.Key;
import java.security.Security;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Scanner;

public class ParseActionsToStream extends GroupProcessor {

    public ParseActionsToStream(Keyring ownerKeyring) {
        super(1, 1);


        //this.associateInput(1, 1);
        History decodedHistory = ca.uqac.lif.artichoke.History.decode(/*encodedHistory*/ "wasd");

        List<WrappedAction> wrappedActionsList = decodedHistory.decrypt(ownerKeyring);

        QueueSource listToUnpack = new QueueSource().setEvents(wrappedActionsList);
        listToUnpack.loop(false);

        Lists.Unpack unpackInstance = new Lists.Unpack();
        Connector.connect(listToUnpack, unpackInstance);

        this.addProcessors(listToUnpack, unpackInstance);
        this.associateOutput(0, unpackInstance, 0);


    }
/*
    public ParseActionsToStream(Map<String, byte[]> keysiLst) {
        super(1, 1);
        String PAS_File = "./example/patient_file.pas";

        Scanner scanner = null;
        try {
            scanner = new Scanner(new File(PAS_File));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        String encodedHistory = scanner.nextLine();
        scanner.close();
        History decodedHistory = ca.uqac.lif.artichoke.History.decode(encodedHistory);

        List<WrappedAction> wrappedActionsList = decodedHistory.decrypt(keysList);

        QueueSource listToUnpack = new QueueSource().setEvents(wrappedActionsList);
        listToUnpack.loop(false);

        Lists.Unpack unpackInstance = new Lists.Unpack();
        Connector.connect(listToUnpack, unpackInstance);

        this.addProcessors(listToUnpack, unpackInstance);
        this.associateOutput(0, unpackInstance, 0);
    }
*/
    private class DecodeActions extends UnaryFunction {
        public DecodeActions() {
            super(String.class, History.class);
        }

        @Override
        public Object getValue(Object o) {
            return null;
        }
    }
}
