package ca.uqac.lif.artichoke;


import ca.uqac.lif.cep.*;

import java.util.List;
import java.util.Map;
import java.util.Queue;

public class ParseActionsToStream extends SynchronousProcessor {
    Map<String, byte[]> KeysList;

    public ParseActionsToStream(Map<String, byte[]> keysList) {
        super(1, 1);
        KeysList = keysList;

    }

    @Override
    protected boolean compute(Object[] objects, Queue<Object[]> queue) {

        System.out.println(objects[0].toString());
        String encodedHistory = (String) objects[0];
        History decodedHistory = ca.uqac.lif.artichoke.History.decode(encodedHistory);

        List<WrappedAction> wrappedActionsList = decodedHistory.decrypt(KeysList);
        for (WrappedAction action : wrappedActionsList) {
            queue.add(new Object[]{action});
            //  System.out.println(action);

        }
        System.out.println(queue.toString());
        return true;
    }

    @Override
    public Processor duplicate(boolean b) {
        return null;
    }
}
