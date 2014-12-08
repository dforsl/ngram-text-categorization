import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * Created by daniel on 2014-12-08.
 */
public class NgramYearPredictor {
    private static final String COMMAND_EXIT = "exit";

    public static void main(String[] args) {

        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));

        String command;
        while (true) {
            System.out.println("Enter file path or exit.");

            try {
                command = reader.readLine();
            } catch (IOException e) {
                System.out.println("ERROR:");
                e.printStackTrace();
                continue;
            }

            if(command.equals(COMMAND_EXIT)) {
                System.out.println("Exiting...");
                break;
            }

            File f = new File(NgramYearPredictor.class.getResource(command).getFile());
            if(f == null || !f.exists() || !f.canRead()) {
                System.out.println("The specified file doesn't exist or cannot be read.");
                continue;
            }

            Profile profile = new Profile(f);
            profile.classify();

            System.out.println("The class is: " + profile.getProfileClass());
        }

        try {
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
