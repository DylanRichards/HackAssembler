import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Scanner;


/**
 * Description: Assembler is a class that converts an ASM file to a HACK file
 * Date: 3/28/2018
 *
 * @author Dylan Richards
 * @version 1.0
 */
public class Assembler {

    private static final String SYMOBOL_FILE_NAME = "symbols.txt";
    private static final String DEST_FILE_NAME = "dest.txt";
    private static final String COMP_FILE_NAME = "comp.txt";
    private static final String JUMP_FILE_NAME = "jump.txt";

    private HashMap<String, String> symbolTable = new HashMap<String, String>();
    private static HashMap<String, String> destTable = new HashMap<String, String>();
    private static HashMap<String, String> compTable = new HashMap<String, String>();
    private static HashMap<String, String> jumpTable = new HashMap<String, String>();

    private ArrayList<String> rom = new ArrayList<String>();//An arraylist acting as the instruction memory

    /**
     * When an Assembler object is initialized the hackFile is populated from the ASM data
     *
     * @param asmFile
     * @param hackFile
     * @throws IOException
     */
    public Assembler(File asmFile, File hackFile) throws IOException {
        //initialize symbol table, comp, dest, jump tables
        initTable(SYMOBOL_FILE_NAME, symbolTable);

        //first pass: generate pure code and labels
        fistPass(asmFile);

        //second pass: translate line by line and save to the output file
        secondPass(hackFile);
    }

    /**
     * initializeCPUTables: initialize comp, dest, and jump tables
     */
    public static void initializeCPUTables() {
        initTable(DEST_FILE_NAME, destTable);
        initTable(COMP_FILE_NAME, compTable);
        initTable(JUMP_FILE_NAME, jumpTable);
    }


    /**
     * initTable: initialize the given HashMap with contents from the given file
     * The file should have two column. The first column is the key,
     * second column is the value
     *
     * @param   fileName    String
     * @param   map         HashMap<String, String>
     */
    private static void initTable(String fileName, HashMap<String, String> map) {
        InputStream inputStream = Assembler.class.getResourceAsStream(fileName);
        Scanner sc = new Scanner(inputStream);
        while (sc.hasNext()) {
            map.put(sc.next(), sc.next());
        }
    }

    /**
     * printTable: print the hashmap
     * only used for debugging
     *
     * @param   map
     */
    private void printTable(HashMap<String, String> map) {
        Iterator<String> itr = map.keySet().iterator();
        String key = "";
        while (itr.hasNext()) {
            key = itr.next();
            System.out.println(key + "\t" + map.get(key));
        }
    }

    /**
     * fistPass: 	read the asm file. Ignore all non-codes.
     * Populate the instruction memory (the arrayList rom) with pure instructions
     * Also extract labels and add to the symbol table
     *
     * @param   asmFile
     * @throws  IOException
     */
    private void fistPass(File asmFile) throws IOException {
        FileReader mFileReader = new FileReader(asmFile);
        BufferedReader mBufferedReader = new BufferedReader(mFileReader);

        String fileLine;
        int labelNum = 0;

        while ((fileLine = mBufferedReader.readLine()) != null) {
            //Remove whitespace and newline
            fileLine = fileLine.replaceAll("\\s+", "");

            //Remove comments
            if (fileLine.contains("//")) {
                int inlineComment = fileLine.indexOf("//");
                fileLine = fileLine.substring(0, inlineComment);
            }


            //Trim the whitespace between the characters
            if (fileLine.trim().length() > 0) {

                if (fileLine.startsWith("(")) {
                    int startLabel = fileLine.indexOf("(") + 1;
                    int endLable = fileLine.indexOf(")");
                    String labelName = fileLine.substring(startLabel, endLable);
                    symbolTable.put(labelName, String.valueOf(labelNum));
                } else {
                    rom.add(fileLine);
                    labelNum++;
                }
            }
        }

        mBufferedReader.close();
    }


    /**
     * secondPass: 	Translate the instructions in the rom one by one and save to the outFile
     *
     * @param   hackFile
     * @throws  IOException
     */
    private void secondPass(File hackFile) throws IOException {

        FileWriter mFileWriter = new FileWriter(hackFile);
        BufferedWriter mBufferedWriter = new BufferedWriter(mFileWriter);

        //Store variables in RAM starting with 16
        int varData = 16;

        for (String line : rom) {
            String outputLine;

            //If A instruction
            if (line.startsWith("@")) {
                int value = -1;
                String aInstruct = line.substring(1, line.length());

                //try parsing the string to see if it is an integer
                try {
                    value = Integer.parseInt(aInstruct);
                } catch (NumberFormatException nfe) {
                    //If it is not a number check the symbol table
                    if (symbolTable.containsKey(aInstruct)) {
                        value = Integer.parseInt(symbolTable.get(aInstruct));
                    } else {
                        //Add to the symbol table
                        value = varData;
                        symbolTable.put(aInstruct, String.valueOf(varData++));
                    }
                }

                outputLine = Integer.toBinaryString(value);

                //outputLine needs to be a binary with 16 bits so we will pad it with 0's
                String padding = "0000000000000000";
                outputLine = padding.substring(0, 16 - outputLine.length()) + outputLine;

            } else {
                //C instruction
                int equalSign = line.indexOf("=");
                int semicolon = line.indexOf(";");


                String destinationKey;
                String computeKey;
                String jumpKey;

                //If there is no equal sign (E.G. 0;JMP) set Destination to NUL and there must be a comp and jump
                if (equalSign == -1) {
                    destinationKey = "NUL";
                    computeKey = line.substring(0, semicolon);
                    jumpKey = line.substring(semicolon + 1, line.length());
                } else {
                    //There is a destination - find the equal sign adn check if there is a jump
                    destinationKey = line.substring(0, equalSign);

                    //iF there is no semicolon (E.G. M=D)  set jump to NUL otherwise cut compute off at the semicolon
                    if (semicolon == -1) {
                        computeKey = line.substring(equalSign + 1, line.length());
                        jumpKey = "NUL";
                    } else {
                        computeKey = line.substring(equalSign + 1, semicolon);
                        jumpKey = line.substring(semicolon + 1, line.length());
                    }

                }

                //Set binary representation for the comp, dest, and jump
                String destinationValue = destTable.get(destinationKey);
                String computeValue = compTable.get(computeKey);
                String jumpValue = jumpTable.get(jumpKey);

                outputLine = "111" + computeValue + destinationValue + jumpValue;
            }

            mBufferedWriter.append(outputLine);
            mBufferedWriter.newLine();
        }

        mBufferedWriter.close();
    }
}
