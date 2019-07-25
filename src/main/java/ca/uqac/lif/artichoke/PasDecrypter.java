package ca.uqac.lif.artichoke;

import ca.uqac.lif.artichoke.keyring.Keyring;
import ca.uqac.lif.cep.GroupProcessor;

public class PasDecrypter extends GroupProcessor {
    public PasDecrypter(Keyring keyring) {
        super(1, 1);
    }
}
