import java.io.*;
import java.util.*;

/** 
 * Models a phone inventory of a human language. 
 *
 * <p>
 * Warning: does not perform unicode normalization on input files, files needs to be 
 * normalized first e.g.to NFC form (canonical composition) e.g. using Babelpad.
 * @author Tim Kempton
 * @version 0.1
 */
public class PhoneInventory {

    /** Set of Phones. */
    private Set<Phone> pSet;

    /** Creates a PhoneInventory given the inventory file (.tsv) and the list of feature files (.tsv). */
    public PhoneInventory(String inventoryTsvFilename, List<String> featureTsvFilenameList) throws IOException {
        // load Tsv file into rMap: key = phone name, value = feature values as one long string
        String thisLine = null;
        String [] thisLineArray = null;
        Map<String,String> rMap = new LinkedHashMap<String,String>();
        String featureNames = null;
        boolean isFirstFile = true;
        for(String featureTsvFilename : featureTsvFilenameList) {
            //System.out.println("Constructor: looking at feature file: "+featureTsvFilename);
            BufferedReader tableFile = new BufferedReader(new FileReader(featureTsvFilename));
            boolean isFirstLine = true;
            while ((thisLine = tableFile.readLine()) != null) {
                thisLineArray = thisLine.split("\t",2); // split line into two
                if (isFirstLine && isFirstFile) {
                    featureNames = thisLineArray[1];
                } else if (isFirstLine && !isFirstFile) {
                    if (!featureNames.equals(thisLineArray[1])) {
                        throw new RuntimeException("Different feature headings in file "+featureTsvFilename);
                    } // else do nothing for this first line i.e. ignore feature headings
                } else if (thisLineArray.length > 1) {
                    rMap.put(thisLineArray[0],thisLineArray[1]);
                }
                isFirstLine = false;
            }
            isFirstFile = false;
        }
        // load in phone inventory from tsv file        
        BufferedReader tableFile = new BufferedReader(new FileReader(inventoryTsvFilename));
        thisLine = null;
        thisLineArray = null;
        UniVectorPhone uVPhone= null;
        List<UniVectorPhone> uVPhoneList = null;
        Phone ph = null;
        pSet = new LinkedHashSet<Phone>();
        String phLabel = null;
        LinkedList<String> thisLineList = null;
        while ((thisLine = tableFile.readLine()) != null) {
            thisLineList = new LinkedList<String>(Arrays.asList(thisLine.split("\t")));
            phLabel = thisLineList.remove();
            if (thisLineList.isEmpty()) { // i.e. no component phones are provided by user
            	StringBuffer uVPhLabelTemp = new StringBuffer(phLabel);
            	int highFiveTone = removeHighFiveTone(uVPhLabelTemp);
            	String uVPhLabelToneless = new String(uVPhLabelTemp);
                if(!rMap.containsKey(uVPhLabelToneless)) 
                    System.err.println("Warning: can't find phone:"+uVPhLabelToneless+" in feature table");
                uVPhone = new UniVectorPhone(phLabel,mapFromTsv(featureNames,rMap.get(uVPhLabelToneless)),highFiveTone);
                uVPhoneList = new ArrayList<UniVectorPhone>(1);
                uVPhoneList.add(uVPhone);
            } 
            else { //  i.e. component phones are provided by user in file
                //System.out.println("found component phones: "+thisLineList);
                uVPhoneList = new ArrayList<UniVectorPhone>(thisLineList.size());
                for (String uVPhLabel:thisLineList) {
                	StringBuffer uVPhLabelTemp = new StringBuffer(uVPhLabel);
                	int highFiveTone = removeHighFiveTone(uVPhLabelTemp);
                	String uVPhLabelToneless = new String(uVPhLabelTemp);
                	//System.err.print("looking at uvphone "+uVPhLabel);
                	//System.err.print(" extracted number "+highFiveTone);
                	//System.err.println(" to leave "+uVPhLabelToneless);
                    if (!rMap.containsKey(uVPhLabelToneless))
                        System.err.println("Warning: can't find phone:"+uVPhLabelToneless+" in feature table");
                    uVPhone = new UniVectorPhone(uVPhLabel,mapFromTsv(featureNames,rMap.get(uVPhLabelToneless)),highFiveTone);
                    uVPhoneList.add(uVPhone);
                }
            }
            ph = new Phone(phLabel,uVPhoneList);
            pSet.add(ph); 
        }
    }

    /** Convert a string of keys (feature names) and string of integer values ("1","-1", or "0") to an actual map */
    private static Map<String,Integer> mapFromTsv(String tsvKeys, String tsvValues) {
        Map<String,Integer> fMap = new LinkedHashMap<String,Integer>();
        String [] keyArray = tsvKeys.split("\t");
        String [] valueArray = tsvValues.split("\t");
        for(int i=0;i<keyArray.length;i++) {
            fMap.put(keyArray[i],Integer.valueOf(valueArray[i]));
        }
        return fMap;	
    }

    /** Remove any Chao tone superscript numbers from the IPA label, but return the actual number it was as an integer */
    private static int removeHighFiveTone(StringBuffer str) { // Should be bufferedString
    	final String[] superNum = new String[] {"\u00b9","\u00b2","\u00b3","\u2074","\u2075"};//superscript 1,2,3,4,5 could make this a class final
    	int res=0;
    	for (int i=0;i<superNum.length;i++) {
    		int index=str.indexOf(superNum[i]);
    		if (index>=0) { // if one of the unicode superscript numbers exist in the string  
    			str.delete(index,index+superNum[i].length()); // remove it
    			res=i+1; // break; // and make a record of which number it refers to
    			break;
    		}
    	}
    	return res;
    }

    /** Get the Phone set */
    public Set<Phone> getPhoneSet() {
        return pSet;
    }

    /** Get the sorted Phone list */
    public List<Phone> getSortedPhoneList() {
    	List<Phone> phList = new LinkedList<Phone>(pSet);
    	//System.out.println(phList);
    	Collections.sort(phList);
    	//System.out.println(phList);
    	return phList;
    }


    /** Get the Phone list */
    public List<Phone> getPhoneList() {
    	List<Phone> phList = new LinkedList<Phone>(pSet);
    	return phList;
    }

    /** Get a particular Phone by specifying it's IPA label */
    public Phone getPhone(String ipaLabel){
    	Phone outPh = null;
    	for(Phone ph:pSet){
    		if(ph.getIpaLabel().equals(ipaLabel)) {
    			outPh =ph;
    			break;
    		}
    	}
    	if(outPh==null)
    		throw new RuntimeException("Phone label "+ipaLabel+" not found in phone inventory");
    	return outPh;
	}

    /** When given two Phones, check that no Phone in the Phone inventory is phonetically between them.
      * This is the relative minimal 
      * difference heuristic as described in Kempton (2012) p43-44, p58, in turn based on 
      * Pepperkamp et al. (2006). */
    public boolean minDistance(Phone phoneA, Phone phoneB) {
    	boolean isNothingBetween = true;
    	for(Phone ph:pSet) {
    		if(!(ph.equals(phoneA))&&(!(ph.equals(phoneB)))) { //we don't check if it matches the two phones 
    			isNothingBetween = isNothingBetween && !(ph.isAllBetweenOrOnBoundary(phoneA, phoneB));
    		}
    	}
    	return isNothingBetween;	
    }

    /** This is like the method minDistance(Phone phoneA, Phone phoneB), but this time it returns the actual Phones
      * that are between phoneA and phoneB */ 
    public List<Phone> phonesBetween(Phone phoneA, Phone phoneB) {
    	List<Phone> phList = new LinkedList<Phone>();
    	for(Phone ph:pSet) {
    		if(!(ph.equals(phoneA))&&(!(ph.equals(phoneB)))) { //we don't check if it matches the two phones 
    			if  (ph.isAllBetweenOrOnBoundary(phoneA, phoneB))
    				phList.add(ph);
    		}
    	}
    	return phList;	
    }

    /** This returns a list of Phones in the inventory that come joint closest to the Phone provided */
    public List<Phone> phonesClosest(Phone phoneN) {
    	double closestDist = 9999; // high number 
    	List<Phone> phList = new LinkedList<Phone>();
    	for(Phone ph:pSet) {
    		double currentDist = phoneN.averageFeatureDistance(ph);
    		//System.out.print(ph+"="+currentDist+" ");
    		if(currentDist<closestDist) {
    			phList.clear();
    			phList.add(ph);
    			closestDist = currentDist;
    		}
    		else if (currentDist==closestDist) {
    			phList.add(ph);
    		}
    	}
    	//System.out.print("closestDist=\t"+closestDist+"\t");
    	return phList;
    }

    /** This is like the method phonesClosest(Phone phoneN), except only one Phone is returned.
      *  The result is deterministic because it is based on a Unicode sort. */ 
    public Phone singlePhoneClosest(Phone phoneN) {
    	Phone phOut;
    	List<Phone> phList = new LinkedList<Phone>();
    	phList = phonesClosest(phoneN);
    	//System.out.print("singlePhoneClosest: list = "+phList);
    	if (phList.size()>1){ // if there are multiple close phones 
    		phList.add(phoneN);
    		//System.out.print("singlePhoneClosest: insert but not sorted = "+phList);
    		Collections.sort(phList); // find a close phone in a unicode string sort
    		//System.out.print("singlePhoneClosest: insert then sorted list = "+phList);
    		int i = phList.indexOf(phoneN);
    		if (i==0) { // if special case of phone at beginning of sorted list
    			phOut=phList.get(1); // return RHS
    		}
    		else {
    			phOut=phList.get(i-1); // else return LHS
    		}
    	}
    	else {phOut=phList.get(0);}
    	return phOut;
    }

    /** Prints a "phone relationship chart" for active articulators.
      * This is a two-dimensional matrix in CSV format, showing every 
      * combination of phone in the inventory and whether the two phones 
      * share active articulators. 0 means yes, 1 means no. These numbers 
      * fit in with the original approach of "phone relationship charts" 
      * where a higher number means the two phones are more likely to be 
      * allophones (See Kempton(2012) p45-50 */  
    public void printArticulatorTruth() {
    	//List<Phone> phList = getSortedPhoneList();
    	List<Phone> phList = getPhoneList();
    	System.out.print(" ");
        for (Phone iPh:phList) {
            System.out.print(","+iPh);
        }
        System.out.println();
        for (Phone jPh:phList) {
            System.out.print(jPh);
            for (Phone iPh:phList) {
                if (jPh.hasDifferentArticulators(iPh)) 
                    System.out.print(",0");
                else System.out.print(",1");
            }
            System.out.println();
        }
    }

    /** Prints a "phone relationship chart" for phonetic similarity.
      * This is a two-dimensional matrix in CSV format, showing for every 
      * combination of phone in the inventory and the number of features that match 
      * To get BFEPP, minus this similarity number from the total number of features.
      * These numbers 
      * fit in with the original approach of "phone relationship charts" 
      * where a higher number means the two phones are more likely to be 
      * allophones See Kempton(2012) p51 (BFEPP measure shown). */  
    public void printSimilarity() {
        System.out.print("phoneticSimilarity");
        for (Phone iPh:pSet) {
            System.out.print(","+iPh);
        }
        System.out.println();
        for (Phone jPh:pSet) {
            System.out.print(jPh);
            for (Phone iPh:pSet) {
                System.out.print(","+iPh.averageCountOfSameFeatures(jPh));
            }
            System.out.println();
        }
    }

    /** This is like the method printMinDistance() but this time it returns the actual 
      * in between Phones (in TSV format) */
    public void printPhonesInBetween() {
        System.out.print("phonesInBetween");
        for (Phone iPh:pSet) {
             System.out.print("\t"+iPh);
        }
        System.out.println();
        for (Phone jPh:pSet) {
            System.out.print(jPh);
            for (Phone iPh:pSet) {
            	System.out.print("\t"+this.phonesBetween(iPh, jPh));
            }
            System.out.println();
        }
    }

    /** Prints a "phone relationship chart" for relative minimal distance.
      * This is a two-dimensional matrix in CSV format, showing for every 
      * combination of phone in the inventory  whether they are minimally close
      * 1 means minimally close, 0 means not. These numbers 
      * fit in with the original approach of "phone relationship charts" 
      * where a higher number means the two phones are more likely to be 
      * allophones. See Kempton(2012) p47 */
    public void printMinDistance() {
        //List<Phone> phList = getSortedPhoneList();
        List<Phone> phList = getPhoneList();
    	System.out.print(" ");
        for (Phone iPh:phList) {
            System.out.print(","+iPh);
        }
        System.out.println();
        for (Phone jPh:phList) {
            System.out.print(jPh);
            for (Phone iPh:phList) {
            	if (this.minDistance(iPh, jPh)) 
            		System.out.print(",1");
            	else System.out.print(",0");
            }
            System.out.println();
        }
    }

    /** Produces a standard string representation */
    public String toString() {
        return pSet.toString();
    }

    /** Produces a string representation for the SRILM factored language model.
      * this is done indirectly by producing a TSV mapping that can be processed
      * with BatchFindAndReplace.java . */
    public String toSrilmFlmFindAndReplaceTsv(){
    	String s = "";
    	for(Phone p:pSet){
    		s=s+p+"\t"+p.toSrilmFlmString()+"\n";
    	}
    	return s;
    }

    /** Produces a string for the -dictionary-align option in SRILM lattice-tool for
     * phone lattices being transformed into sausages. Currently only prints out the
     * first uniVectorPhone of each Phone which is not ideal but keeps the rest of the processing
     * e.g. srilm mesh generation, simple. 
     */ 
    public String toSrilmDictionaryString(){
    	String s = "";
    	for(Phone p:pSet){
    		s=s+p.toFirstElementSrilmDictionaryString()+"\n";
    	}
    	return s;
    }

    /** Demonstration and test 
      * arg1: the phone inventory with any components defined after the phone in tsv format
      * additional args: feature files, the later files have priority of overiding the first
      * stdout: filter tsv output */
    public static void main(String[] args) throws IOException {
    	String[] newArgs = {"../resources/fra/frenchPeperkampConsonantAllophones_utf8nfc.txt",
    			"../resources/common/hayes_features_utf8nfc.tsv",
    			"../resources/common/extra_and_override_phone_features_utf8nfc.tsv",
    			"../resources/common/extra_auto_generated_utf8nfc.tsv"};
        LinkedList<String> argList = new LinkedList<String>(Arrays.asList(newArgs));
        String phoneComponentsFilename = argList.remove();
        PhoneInventory phInv = new PhoneInventory(phoneComponentsFilename,argList);
        //System.out.println(phInv);
        //System.out.println(phInv.getPhone("p"));
        //System.out.print(phInv.toSrilmDictionaryString());
        //System.out.print(phInv.toSrilmFlmFindAndReplaceTsv());
        //System.out.println();
        //phInv.printArticulatorTruth();
        phInv.printSimilarity();
        //phInv.printMinDistance();
        //phInv.printPhonesInBetween();
        //System.out.println(phInv.getPhoneSet());
    }
}
