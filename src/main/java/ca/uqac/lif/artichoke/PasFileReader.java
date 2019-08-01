package ca.uqac.lif.artichoke;


import ca.uqac.lif.cep.Processor;
import ca.uqac.lif.cep.SynchronousProcessor;

import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.util.Queue;

public class PasFileReader extends SynchronousProcessor {
    public PasFileReader() {
        super(0, 1);
    }

    @Override
    protected boolean compute(Object[] objects, Queue<Object[]> queue) {
        String s2 = "";
        Frame fileSelecterFrame = new Frame();

        JFileChooser fc = new JFileChooser();
        int i = fc.showOpenDialog(fileSelecterFrame);
        if (i == JFileChooser.APPROVE_OPTION) {

            File keyringFile = fc.getSelectedFile();
            String filepath = keyringFile.getPath();

            BufferedReader br = null;
            try {
                br = new BufferedReader(new FileReader(filepath));
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            String s1 = "";
            while (true) {
                try {
                    if (!((s1 = br.readLine()) != null)) break;
                } catch (IOException e) {
                    e.printStackTrace();
                }
                s2 += s1 + "\n";
            }
            try {
                br.close();
            } catch (IOException e) {
                e.printStackTrace();
            }


        }


        System.out.println(s2);
        return queue.add(new String[]{s2});

    }

    @Override
    public Processor duplicate(boolean b) {
        return null;
    }
}