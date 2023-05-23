package io.github.speechchemistry;

import java.io.FileReader;
import java.io.IOException;
import java.io.BufferedReader;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

/** 
 * Models a phone utterance of a human language. 
 *
 * <p>
 * Warning: does not perform unicode normalization on input files, files needs to be 
 * normalized first e.g. to NFC form (canonical composition) e.g. using Babelpad.
 * @author Tim Kempton
 * @version 0.1
 */
public class PhoneUtterance{
        /** A list of phones represents the utterance. */
	private List<Phone> utterance;
        /** The phone inventory of the language. */
	private PhoneInventory inventory;	//if we use the above structure, this might only 
				                // be needed for the constructor

        /** Creates a PhoneUtterance given a string of phone (with spaces in between phones and the phone inventory.*/
	public PhoneUtterance(String utterWithSpaces, PhoneInventory inventory) {
		this.inventory = inventory;
		this.utterance = new LinkedList<Phone>();
		String[] utterArray = utterWithSpaces.split(" ");
		for(String ipaLabel:utterArray){
			utterance.add(this.inventory.getPhone(ipaLabel));
		}
	}

    /** Creates a PhoneUtterance that is empty (but still associated with a particular language i.e. is associated with a phone inventory).*/
    public PhoneUtterance(PhoneInventory inventory) {
        this.inventory = inventory;
        this.utterance =  new LinkedList<Phone>(); // empty list
    }

	/**
	 * @return the utterance as a list of Phones
	 */
	public List<Phone> getPhoneList() {
		return utterance;
	}

    /** Add a phone to the end of the utterance */
	public void addPhoneToEnd(Phone additionalPh) {
		utterance.add(additionalPh);
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "utterance=" + utterance;
	}

	
	// This version returns an array of doubles rather than a String 
        /** Calculates the difference matrix between this utterance and the 
         *  other one that is provided. Each phone of this utterance is 
         *  compared with each phone of the other utterance and the average 
         *  feature distance is calculated. Returns an array of results. 
         */
	public double[][] differenceMatrix(PhoneUtterance other){
		List<Phone> otherPhList = other.getPhoneList();
		int r=utterance.size();
		int c=otherPhList.size();
		double[][] out = new double[r][c];
		int i=0;
        for (Phone iPh:utterance) {
            int j=0;
            for (Phone jPh:otherPhList) {
                out[i][j]=jPh.averageFeatureDistance(iPh);
                j++;
            }
            i++;
        }
		return out;
	}
	
    /** Returns a transliteration based on the supplied PhoneInventory.
     * For each phone in the utterance we replace it with the closest 
     * phone in the supplied inventory */
    public PhoneUtterance transliterate(PhoneInventory foreignPhInv){
        // Start with an empty utterance
        PhoneUtterance translitPhUtt = new PhoneUtterance(foreignPhInv); 
        // Go through each phone in the utterance and find the closest phone in the inventory
        for (Phone iPh:utterance) {
            translitPhUtt.addPhoneToEnd(foreignPhInv.singlePhoneClosest(iPh));
        }
        return translitPhUtt;
        }
	
	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
        String arbitraryTranscription = args[0];
	    //String word2 = args[1];
        // load in hayes features 	
	    LinkedList<String> binaryFeatureFiles = new LinkedList<String>(Arrays.asList(
                "/mnt/c/Users/Tim/Dropbox/#SIL/language_analysis/technology_experiments/Phoible/phoible_segments_no_multivector_no_ambigous_phones_utf8nfc.tsv")); 
        // load in the big phone inventory 
        String bigPhoneComponentsFilename = "/mnt/c/Users/Tim/Dropbox/#SIL/language_analysis/technology_experiments/Phoible/phonelist_no_multivector_no_ambiguous_phones_utf8nfc.txt";
        PhoneInventory bigPhInv = new PhoneInventory(bigPhoneComponentsFilename,binaryFeatureFiles);
        // create the phone inventory for the source language e.g. English
        String englishPhoneComponentsFilename = "/mnt/c/Users/Tim/Dropbox/#SIL/language_analysis/technology_experiments/Phoible/cmuArpabet_components_phoible_compatible_utf8nfc.tsv";
        PhoneInventory englishPhInv = new PhoneInventory(englishPhoneComponentsFilename,binaryFeatureFiles);
        PhoneUtterance arbUtterance = new PhoneUtterance(arbitraryTranscription,bigPhInv);
        System.out.println("Here is the utterance: "+arbUtterance);
        System.out.println("Here is an attempt at transliteration: "+arbUtterance.transliterate(englishPhInv));

	}     
}
