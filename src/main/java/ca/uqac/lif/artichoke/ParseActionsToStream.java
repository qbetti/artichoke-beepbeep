package ca.uqac.lif.artichoke;

import ca.uqac.lif.artichoke.exceptions.BadPassphraseException;
import ca.uqac.lif.artichoke.exceptions.PrivateKeyDecryptionException;
import ca.uqac.lif.artichoke.keyring.Keyring;
import ca.uqac.lif.cep.*;
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

public class ParseActionsToStream extends SynchronousProcessor {
    Map<String, byte[]> KeysList;

    public ParseActionsToStream(Map<String, byte[]> keysList) {
        super(1, 1);
        KeysList = keysList;
        Security.addProvider(new BouncyCastleProvider());
        System.out.println("reached parser constructor");
    }

    @Override
    protected boolean compute(Object[] objects, Queue<Object[]> queue) {
        Security.addProvider(new BouncyCastleProvider());

        System.out.println("reached parser compute fcn");
        String encodedHistory = (String) objects[0];
        History decodedHistory = ca.uqac.lif.artichoke.History.decode(encodedHistory);

        List<WrappedAction> wrappedActionsList = decodedHistory.decrypt(KeysList);
        for (WrappedAction action : wrappedActionsList) {
            queue.add(new Object[]{action});
            System.out.println(action);
        }
        return false;
    }

    @Override
    public Processor duplicate(boolean b) {
        return null;
    }
}
