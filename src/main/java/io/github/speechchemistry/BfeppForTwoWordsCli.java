package io.github.speechchemistry;

import java.io.FileReader;
import java.io.IOException;
import java.io.BufferedReader;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Callable;
import java.text.Normalizer;

import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

/** 
 * Gives a simple Binary Feature Error Per Phone (BFEPP) measure for two words
 *
 * <p>
 * Warning: does not perform unicode normalization on input files, files needs to be 
 * normalized first e.g.to NFC form (canonical composition) e.g. using Babelpad.
 * @author Tim Kempton
 * @version 0.1
 */
@Command(name = "BfeppForTwoWordsCli", version = "BfeppForTwoWordsCli 0.2", mixinStandardHelpOptions = true)
public class BfeppForTwoWordsCli implements Callable{
 
    @Parameters(paramLabel = "word1", description = "Word 1 phonetic transcription in IPA (no tie bars)")
    String word1;
    @Parameters(paramLabel = "word2", description = "Word 2 phonetic transcription in IPA (no tie bars). This should be the reference transcription / ground-truth. If there is no reference transcription then this should be the longest transcription")
    String word2;
    @Option(names = { "-f", "--feature-framework" }, defaultValue = "phoible", description = "Feature framework to use: phoible (default) or hayes")
    String feature_framework = "phoible";

    @Override
    public Integer call() throws IOException {
        ///String word1 = args[0];
        ///String word2 = args[1];
        // load in hayes features
        String word1_nfc = Normalizer.normalize(word1, Normalizer.Form.NFC);
        String word2_nfc = Normalizer.normalize(word2, Normalizer.Form.NFC);
        LinkedList<String> hayesFiles;
        String phoneComponentsFilename;
        if (feature_framework.equals("hayes")) {
            hayesFiles = new LinkedList<String>(Arrays.asList(
                "/common/hayes/hayes_features_utf8nfc.tsv",
                "/common/hayes/extra_and_override_phone_features_utf8nfc.tsv",
                "/common/hayes/extra_auto_generated_utf8nfc.tsv"));
            phoneComponentsFilename = "/common/hayes/combined_phone_list_utf8nfc.txt";
        }
        else {
            hayesFiles = new LinkedList<String>(Arrays.asList(
                    "/common/phoible/phoible_segments_no_multivector_no_ambigous_phones_utf8nfc.tsv"));
            phoneComponentsFilename = "/common/phoible/phonelist_no_multivector_no_ambiguous_phones_utf8nfc.txt";
        }
        // load in the big phone inventory 
        
        //String phoneComponentsFilename = "/common/hayes/combined_phone_list_utf8nfc.txt";
        PhoneInventory lang1PhInv = new PhoneInventory(phoneComponentsFilename,hayesFiles);
        PhoneUtterance lang1PhUtt = new PhoneUtterance(word1_nfc, lang1PhInv);
        // create the same big phone inventory for language 2
        PhoneInventory lang2PhInv = new PhoneInventory(phoneComponentsFilename,hayesFiles);
        PhoneUtterance lang2PhUtt = new PhoneUtterance(word2_nfc, lang2PhInv);

        //Do a dynamic time warp and print the state transitions
        double[][] diffArray = lang1PhUtt.differenceMatrix(lang2PhUtt);
        DanEllisDtw dtw = new DanEllisDtw(diffArray);
        List<Integer> lang1DtwIndexList = dtw.getP();
        List<Phone> lang1PhoneList = lang1PhUtt.getPhoneList();
        double totalFeatureErrors = dtw.getTotalCost();
        // most accurate way to calculate number of phones is to count the spaces and add 1
        int lengthOfWord1 = word1_nfc.length() - word1_nfc.replaceAll(" ", "").length() +1;
        int lengthOfWord2 = word2_nfc.length() - word2_nfc.replaceAll(" ", "").length() +1;
        // to calculate BFEPP we divide by the reference string (Kempton 2012, p69) which 
        // should be the second string in this case. If there is no reference string the 
        // second string should be longest string (Kempton 2012, p56)
        double bfepp = 0;
        bfepp = totalFeatureErrors/lengthOfWord2;  
        System.out.println(word1_nfc+"\t"+word2_nfc+"\t"+bfepp);
        return 0;
    }
    
    public static void main(String[] args) {
        int exitCode = new CommandLine(new BfeppForTwoWordsCli()).execute(args); 
        System.exit(exitCode);
    }      
}
