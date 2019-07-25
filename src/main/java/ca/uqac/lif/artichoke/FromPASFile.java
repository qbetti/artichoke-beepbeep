package ca.uqac.lif.artichoke;

import ca.uqac.lif.artichoke.keyring.Keyring;
import ca.uqac.lif.cep.GroupProcessor;
import ca.uqac.lif.cep.tmf.QueueSource;

import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.util.Scanner;

public class FromPASFile extends GroupProcessor {
    public FromPASFile(String s2) {
        super(0, 1);



        QueueSource sourceFile = new QueueSource().setEvents(s2);
        sourceFile.loop(false);

        this.addProcessors(sourceFile);
        this.associateOutput(0, sourceFile, 0);
    }
}
