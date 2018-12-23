import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.IOException;

/**
 * Description MainUI is a class that creates a JFrame and interacts with the Assembler Class
 * Date: 3/28/2018
 *
 * @author Dylan Richards
 * @version 1.0
 */
public class MainUI extends JFrame {
    private JPanel contentPane;
    private JButton openButton;
    private JButton saveButton;
    private JProgressBar progressBar1;
    private JCheckBox defaultLocationCheckBox;
    private JLabel label;

    private File asmFile, hackFile;

    private static boolean defaultLocation = false;

    /**
     * MainUI creates a JFrame and sets up the GUI
     */
    public MainUI() {
        super("Hack Assembler");
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setPreferredSize(new Dimension(350, 250));
        setContentPane(contentPane);

        progressBar1.setMaximum(100);

        openButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                progressBar1.setValue(0);
                label.setText("No File Loaded");

                asmFile = getFile(true);

                if(asmFile.isFile()){
                    progressBar1.setValue(10);
                    label.setText(asmFile.getName());
                }

            }
        });
        saveButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    createHackFile();

                    new Assembler(asmFile, hackFile);

                    progressBar1.setValue(100);
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
        });
        defaultLocationCheckBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if(defaultLocationCheckBox.isSelected()){
                    defaultLocation = true;
                }else {
                    defaultLocation = false;
                }

            }
        });
    }

    /**
     * Main method initialized the assembler tables and kicks off the GUI
     * @param args
     */
    public static void main(String[] args) {
        Assembler.initializeCPUTables();

        MainUI dialog = new MainUI();
        dialog.pack();
        dialog.setVisible(true);

    }

    /**
     * getFile creates a GUI to find and save files
     * @param   openDialog if true it will be used to open ASM file. Else it will be used to save HACK file
     * @return  ASM File
     */
    private static File getFile(boolean openDialog) {
        JFileChooser mJFileChooser = new JFileChooser();
        JFrame mFrame = new JFrame();

        mJFileChooser.setCurrentDirectory(new File(System.getProperty("user.dir")));

        int result;
        if(openDialog){
            mJFileChooser.setFileFilter(new FileNameExtensionFilter("Assembly Level Source File", "asm"));
            mJFileChooser.setAcceptAllFileFilterUsed(false);
            result = mJFileChooser.showOpenDialog(mFrame);
        }else{
            mJFileChooser.setCurrentDirectory(new File(System.getProperty("user.dir")));
            mJFileChooser.setFileFilter(new FileNameExtensionFilter("HACK Files", "hack"));
            mJFileChooser.setAcceptAllFileFilterUsed(false);
            result = mJFileChooser.showSaveDialog(mFrame);
        }


        File selectedFile = null;
        if (result == JFileChooser.APPROVE_OPTION) {
            selectedFile = mJFileChooser.getSelectedFile();
            System.out.println("Selected file: " + selectedFile.getAbsolutePath());
        }
        return selectedFile;
    }

    /**
     * Create Hack file creates a new file in the users choice of directory or in the directory of the ASM File
     * If one already exist it will delete it
     * @throws  IOException
     */
    private void createHackFile() throws IOException {
        String hackFileDirectory;
        if(defaultLocation){
            hackFileDirectory = getHackFileName(asmFile.getAbsolutePath());
        }else{
            hackFileDirectory = getHackFileName(getFile(false).getAbsolutePath());
        }

        hackFile = new File(hackFileDirectory);

        if(hackFile.exists() && hackFile.isFile()){
            hackFile.delete();
        }
        hackFile.createNewFile();

        System.out.println(hackFile.getAbsolutePath());

    }

    /**
     * getHackFileName: 	get output hack file name from asm file name
     * @param   asmFile
     * @return  hackFile
     */
    private String getHackFileName(String asmFile) {
        //If there was no file extension append one
        if(asmFile.indexOf('.') == -1){
            asmFile = asmFile + ".hack";
        }else{
            asmFile = asmFile.substring(0, asmFile.indexOf('.')) + ".hack";
        }
        return asmFile;
    }

}
