package io.github.speechchemistry;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/** BatchFindAndReplace.java
 * When given an input file and a mapping file (in tab separated value format) this program creates a new 
 * file with all the appropriate find and replace mappings done together in one batch simultaneously. 
 * If there is a sequence to be found that is a subset of another sequence, the longer sequences takes priority.
 * It is particularly helpful when dealing with unicode text.
 * <p>
 * Warning: this class assumes all Unicode text has the same normalisation e.g. NFC. 
 * <p>
 * Example usage (main method): java BatchFindAndReplace helloWorldInTimitAscii.txt timit2ipa_utf8nfc.txt
 *
 * @author Timothy Kempton
 * @version 0.1
 */
/* in the future, this class may sort out normalisation itself once java 1.6 
  becomes more widespread */
public class BatchFindAndReplace {
        /**  Mapping from string keys to string values */
	Map<String, String> rMap;

	/**  Create BatchFindAndReplace object from existing string to string mapping. */
	public BatchFindAndReplace(Map<String,String> inMap) {
		rMap = inMap;
	}

	/**  Create BatchFindAndReplace object from TSV file. */
	public BatchFindAndReplace(String mapFile) throws IOException {
		BufferedReader tableFile = new BufferedReader(new FileReader(mapFile));
		String thisLine = null;
		String[] thisLineArray = null;
		rMap = new LinkedHashMap<String, String>();// linked version to keep order and therefore any bugs deterministic
													// otherwise could just be HashMap
		// load tsv file into map
		while ((thisLine = tableFile.readLine()) != null) {
			thisLineArray = thisLine.split("\\t",2);
			if (thisLineArray.length > 1) {
				rMap.put(thisLineArray[0], thisLineArray[1]);
			}
		}
	}
	
	/** Finds the earliest occurring search string (as defined in the objects find/replace table) in the input string.
	 * If there are more than one match, this method returns the longest string.*/
	public String earliestLongestMatch(String in,int fromIndex){
		Set<String> findSet = rMap.keySet();
		int minStart = in.length();
		//search for all words and get initial positions
		for(String f:findSet){
			if (f.equals(""))
				System.err.println("Warning: found empty search string; ignoring a search/replace pair "+rMap.get(f));
			else {
				int s = in.indexOf(f,fromIndex);
				if (s > -1) minStart=Math.min(minStart,s);  
				//System.out.println("word="+f+" index="+s);
			}
		}
		// find earliest word(s)
		// efficiency potential: no need for this second loop if first loop 
		// was more clever with a Map<Integer,List<String>> 
		// structure that stored indexes and associated words
		List<String> commonStartWords = new LinkedList<String>();
		for(String f:findSet){
			if (in.indexOf(f,fromIndex)==minStart) commonStartWords.add(f);
		}
		//System.out.println("earliest word found at "+minStart+":"+commonStartWords);
		//now find the longest word (if more than one word with common starting point)
		String longestStr = "";
		int lengthMax = 0;
		for(String c:commonStartWords){
			if (c.length()>lengthMax) {
				longestStr=c;
				lengthMax=c.length();
			}
		}
		//System.out.println("longest word found:"+longestStr);
		return longestStr;
	}
	
	/** Performs a batch search and replace (all) process on the input string. */
	public String processString(String in) {
		String out="";
		int beginning = 0;
		int newBeginning = 0;
		int matchStart = 0;
		while (beginning<in.length()) {
			String match=earliestLongestMatch(in,beginning);
			if (match.length()>0) { // if there is a match
				//matchStart = in.indexOf(match);
				matchStart = in.indexOf(match,beginning);
				//System.out.println("before substring: beginning="+beginning+" matchStart="+matchStart);
				out=out+in.substring(beginning, matchStart);
				out=out+rMap.get(match);
				newBeginning=matchStart+match.length();
			}
			else { // if there are no more matches
				out=out+in.substring(beginning);
				newBeginning=in.length();
			}
			//System.out.println("processString: beginning="+beginning+" matchStart="+matchStart+" match="+match+" out="+out+" newBeginning="+newBeginning);
			beginning=newBeginning;
		}
		return out;
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "BatchFindAndReplace [rMap=" + rMap + "]";
	}

        /** First parameter is the text file and the second parameter is the mapping file */
	public static void main(String[] args) throws IOException {
		String textFile = args[0];
		String replaceTsvFile = args[1];
		BatchFindAndReplace p = new BatchFindAndReplace(replaceTsvFile);
		BufferedReader textReader = new BufferedReader(new FileReader(textFile));
		String thisLine = "";
		while ((thisLine = textReader.readLine()) != null) {
			System.out.println(p.processString(thisLine));
		}
	}
}
