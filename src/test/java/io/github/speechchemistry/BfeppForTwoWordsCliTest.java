package io.github.speechchemistry;

import java.io.*;
import java.util.*;
import java.text.Normalizer;

import static org.junit.Assert.*;
import org.junit.Test;

import io.github.speechchemistry.UniVectorPhone;


/**
 * Unit test for simple App.
 */
public class BfeppForTwoWordsCliTest
{
    /**
     * Rigorous Test :-)
     */
    @Test
    public void shouldAnswerWithTrue() throws IOException
    {
        String word1 = "x e";
        String word2 = "k ã";
        String word1_nfc = Normalizer.normalize(word1, Normalizer.Form.NFC);
        String word2_nfc = Normalizer.normalize(word2, Normalizer.Form.NFC);
        LinkedList<String> hayesFiles = new LinkedList<String>(Arrays.asList(
                "/common/hayes/hayes_features_utf8nfc.tsv",
                "/common/hayes/extra_and_override_phone_features_utf8nfc.tsv",
                "/common/hayes/extra_auto_generated_utf8nfc.tsv"));
        // load in the big phone inventory

        String phoneComponentsFilename = "/common/hayes/combined_phone_list_utf8nfc.txt";
        PhoneInventory lang1PhInv = new PhoneInventory(phoneComponentsFilename, hayesFiles);
        PhoneUtterance lang1PhUtt = new PhoneUtterance(word1_nfc, lang1PhInv);
        // create the same big phone inventory for language 2
        PhoneInventory lang2PhInv = new PhoneInventory(phoneComponentsFilename, hayesFiles);
        PhoneUtterance lang2PhUtt = new PhoneUtterance(word2_nfc, lang2PhInv);

        // Do a dynamic time warp and print the state transitions
        double[][] diffArray = lang1PhUtt.differenceMatrix(lang2PhUtt);
        DanEllisDtw dtw = new DanEllisDtw(diffArray);
        List<Integer> lang1DtwIndexList = dtw.getP();
        List<Phone> lang1PhoneList = lang1PhUtt.getPhoneList();
        double totalFeatureErrors = dtw.getTotalCost();
        // most accurate way to calculate number of phones is to count the spaces and
        // add 1
        int lengthOfWord1 = word1_nfc.length() - word1_nfc.replaceAll(" ", "").length() + 1;
        int lengthOfWord2 = word2_nfc.length() - word2_nfc.replaceAll(" ", "").length() + 1;
        // to calculate BFEPP we divide by the longest string (see Kempton(2012) p65)
        double bfepp = 0;
        bfepp = totalFeatureErrors / lengthOfWord2;
        assertEquals(bfepp,2.75,0.1);
    }
}
