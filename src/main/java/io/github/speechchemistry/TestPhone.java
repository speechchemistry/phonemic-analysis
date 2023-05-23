package io.github.speechchemistry;

import java.io.*;
import java.util.*;

public class TestPhone {
    public static void main(String[] args) throws IOException {
    	LinkedList<String> bundleList = new LinkedList<String>(Arrays.asList(
        		"../resources/common/hayes_features_utf8nfc.tsv",
        		"../resources/common/extra_and_override_phone_features_utf8nfc.tsv",
        		"../resources/common/extra_auto_generated_utf8nfc.tsv"));
        String phoneComponentsFilename1 = args[0];
        PhoneInventory lang1PhInv = new PhoneInventory(phoneComponentsFilename1,bundleList);
        String phoneComponentsFilename2 = args[1];
        PhoneInventory lang2PhInv = new PhoneInventory(phoneComponentsFilename2,bundleList);

        List<Phone> phList = new LinkedList<Phone>(lang1PhInv.getPhoneSet());
        for(Phone lang1Ph:phList){
        	System.out.println(lang1Ph+"\t"+lang2PhInv.singlePhoneClosest(lang1Ph));
        }
    }
}
