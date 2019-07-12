package ca.uqac.lif.artichoke;

import ca.uqac.lif.cep.GroupProcessor;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

public class ReadActionsFile extends GroupProcessor {
    public ReadActionsFile() {
        super(0, 1);

        // code here
        String PAS_File = "./example/patient_file.pas";

        Scanner scanner = null;
        try {
            scanner = new Scanner(new File(PAS_File));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        String encodedHistory = scanner.nextLine();
        scanner.close();
    }
}
