import java.io.*;
import java.util.*;

/** 
 * Models a phone (segment) that can be represented by a single 
 * vector of features. This is true for most phones. Currently feature values are
 * represented by integers e.g. 1,-1 for binary features with zero representing 
 * the special value 'don't care' (undefined).
 * @author Tim Kempton
 * @version 0.1
 */
// It might be better to use a data structure for binary features e.g. that explicitly handles the special value.
public class UniVectorPhone implements Comparable<UniVectorPhone>{
    /**
     * Phone label in IPA e.g. d
     */
    private String label;

    /**
     * Binary features e.g.  voice=1,  nasal=-1, ...
     */
    private Map<String,Integer> features;

    /**
     * The universe set of active articulators: {LABIAL,CORONAL,DORSAL,nasal}
     */
    public final static Set<String> articUnivSet =
        new HashSet<String>(Arrays.asList("LABIAL","CORONAL","DORSAL","nasal")); // active articulator universe set

    /**
     * Create the UniVectorPhone with IPA label and binary features
     */
    public UniVectorPhone(String ipaLabel,Map<String,Integer> featureMap) {
        label = ipaLabel;
        features = featureMap;
    }

    /**
     * Create the UniVectorPhone with IPA label and binary features and supplying the Chao tone number (1-5 with 5 as high)
     */
    public UniVectorPhone(String ipaLabel,Map<String,Integer> featureMap, int highFiveTone) {
    	this(ipaLabel,featureMap);
    	int vhigh=-1; int high=-1; int low=-1; int vlow=-1; // together this is the default for tone level 3
    	if (highFiveTone>4) vhigh = 1;
    	if (highFiveTone>3) high = 1;
    	if (highFiveTone<3) low = 1;
    	if (highFiveTone<2) vlow = 1;
    	if (highFiveTone==0) {vhigh=0;high=0;low=0;vlow=0;} // zero denotes don't care
    	features.put("tone_vhigh", vhigh); // put is optional for Maps so might cause probs for some Maps?
    	features.put("tone_high", high);
    	features.put("tone_low", low);
    	features.put("tone_vlow", vlow);
    }

   /**
    * Gets the IPA label.
    * @return this UniVectorPhone's IPA label.
    */
    public String getIpaLabel() {
        return label;
    }

    /** Get number of features in this UniVectorPhone */
    public int size() {
        return features.size();
    }

    /** Get the value of a particular feature */
    public Integer getFeatureValue(String feature) {
        if (!features.containsKey(feature))  
           System.err.println("Warning: feature "+feature+" not found in uVPhone "+label+" returning null value");
        return features.get(feature);
    }

    /** Get the active articulator set of this UniVectorPhone. */
    public Set<String> getArticSet() {
        Set<String> thisSet = new HashSet<String>(3);
        for (String el : articUnivSet) {
            if (getFeatureValue(el)==1) {thisSet.add(el);}
        }
        return thisSet;
    }

    /** Calculates (2x) feature difference, assumes both phones use same feature system. */
    public int doubleFeatureDiff(UniVectorPhone otherUVPhone) {
        Set<String> featureSet = features.keySet();
        int cumDiff = 0;
        for (String el : featureSet) {
            cumDiff += Math.abs(getFeatureValue(el) - otherUVPhone.getFeatureValue(el));
        }
        return cumDiff;
    }

    /** Calculates whether this UniVectorPhone is between two other UniVectorPhones in feature space. */
    public boolean isBetweenOrOnBoundary(UniVectorPhone uVPhoneA, UniVectorPhone uVPhoneB) {
        Set<String> featureSet = features.keySet();
        boolean isAllFeaturesBetween = true;
        for (String feature : featureSet) {
            int a = uVPhoneA.getFeatureValue(feature);
            int b = uVPhoneB.getFeatureValue(feature);
            int n = getFeatureValue(feature);
            isAllFeaturesBetween = isAllFeaturesBetween && featureValueIsBetweenOrEqual(a,b,n);
        }
        return isAllFeaturesBetween;
    }

    /** Calculates whether a feature value is between two other feature value. */
    private static boolean featureValueIsBetweenOrEqual(int a, int b, int n) {
        boolean result = ((a<=n) && (n<=b)) || ((b<=n) && (n<=a));
        result = result || (a==0) || (b==0) || (n==0); // zero treated as 'don't care'
        return result;
    }

    /** Comparisons are based on whether the UniVectorPhone is syllabic or not (usually to distinguish vowels and consononants). */
    public int compareTo(UniVectorPhone other) {
		return this.getFeatureValue("syllabic").compareTo(other.getFeatureValue("syllabic"));
    }

    /** Produces a standard string representation. */
    public String toString() {
        //return label+" "+features;
        return label;
    }

    /** Produces a string representation for the SRILM factored language model. */
    public String toSrilmFlmString(){
    	String s = label;
    	for(Map.Entry<String,Integer> entry:features.entrySet()){
    		s=s+":"+entry.getKey()+"-="+entry.getValue();
    	}
    	return s;
    }
    
    /** Produces a string representation for the -dictionary-align option in SRILM lattice-tool for
      * phone lattices being transformed into sausages.
      */ 
    public String toSrilmDictionaryString(){
    	String s = "";
    	for(Map.Entry<String,Integer> entry:features.entrySet()){
    		int v = entry.getValue();
    		if (v==-1) s=s+"- - ";
    		else if (v==1) s=s+"+ + ";
    		else s=s+"+ - "; // i.e. if v==0
    		s=s+entry.getKey()+" , ";
    	}
    	return s;
    }

    /** Produces a string displaying feature values in TSV format */
    /* efficiency saving: could use values rather than entry */
    public String toTsvBodyString(){
    	String s = label;
    	for(Map.Entry<String,Integer> entry:features.entrySet()){
    		s=s+"\t"+entry.getValue();
    	}
    	return s;
    }
    /* (non-Javadoc) Overide "hashCode" (probably done by Eclipse automatically)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((features == null) ? 0 : features.hashCode());
		result = prime * result + ((label == null) ? 0 : label.hashCode());
		return result;
	}

	/* (non-Javadoc) Overide "equals" (probably done by Eclipse automatically)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		UniVectorPhone other = (UniVectorPhone) obj;
		if (features == null) {
			if (other.features != null)
				return false;
		} else if (!features.equals(other.features))
			return false;
		if (label == null) {
			if (other.label != null)
				return false;
		} else if (!label.equals(other.label))
			return false;
		return true;
	}

        /** Demonstration and test */
	public static void main(String[] args) throws IOException {
        Map<String,Integer> fMap = new LinkedHashMap<String,Integer>();
        fMap.put("syllabic",-1);
        fMap.put("DORSAL",-1);
        fMap.put("LABIAL",1);
        fMap.put("CORONAL",-1);
        fMap.put("nasal",-1);
        UniVectorPhone fPhone = new UniVectorPhone("f",fMap,4);
        Map<String,Integer> wMap = new LinkedHashMap<String,Integer>();
        wMap.put("syllabic",-1);
        wMap.put("DORSAL",1);
        wMap.put("LABIAL",1);
        wMap.put("CORONAL",-1);
        wMap.put("nasal",-1);
        UniVectorPhone wPhone = new UniVectorPhone("w",wMap,0);

        wMap = new LinkedHashMap<String,Integer>();
        wMap.put("syllabic",1);
        wMap.put("DORSAL",1);
        wMap.put("LABIAL",1);
        wMap.put("CORONAL",-1);
        wMap.put("nasal",-1);
        wMap.put("high",1);
        wMap.put("low",-1);
        wMap.put("front",-1);
        wMap.put("back",1);
        wMap.put("round",1);
        UniVectorPhone uph1 = new UniVectorPhone("u",wMap);

        wMap = new LinkedHashMap<String,Integer>();
        wMap.put("syllabic",1);
        wMap.put("DORSAL",1);
        wMap.put("LABIAL",-1);
        wMap.put("CORONAL",-1);
        wMap.put("nasal",-1);
        wMap.put("high",1);
        wMap.put("low",-1);
        wMap.put("front",1);
        wMap.put("back",-1);
        wMap.put("round",-1);
        UniVectorPhone uph2 = new UniVectorPhone("i",wMap);

        wMap = new LinkedHashMap<String,Integer>();
        wMap.put("syllabic",1);
        wMap.put("DORSAL",1);
        wMap.put("LABIAL",-1);
        wMap.put("CORONAL",-1);
        wMap.put("nasal",-1);
        wMap.put("high",1);
        wMap.put("low",-1);
        wMap.put("front",-1);
        wMap.put("back",-1);
        wMap.put("round",-1);
        UniVectorPhone uph3 = new UniVectorPhone("barred i",wMap);

        System.out.println(fPhone+"\n"+wPhone);
        System.out.println("size for "+wPhone+"="+wPhone.size());
        System.out.println("artic set for "+wPhone.getIpaLabel()+"="+wPhone.getArticSet());
        System.out.println("diff: "+wPhone.doubleFeatureDiff(fPhone));

        System.out.println(uph3+" is between "+uph1+" and "+uph2+"? ="+uph3.isBetweenOrOnBoundary(uph1,uph2));
        System.out.println(uph2+" is between "+uph1+" and "+uph3+"? ="+uph2.isBetweenOrOnBoundary(uph1,uph3));
        System.out.println(uph1+" is between "+uph3+" and "+uph2+"? ="+uph1.isBetweenOrOnBoundary(uph3,uph2));

        List<UniVectorPhone> uvphList = new LinkedList<UniVectorPhone>(Arrays.asList(fPhone,uph1,wPhone));
        Collections.sort(uvphList);
        System.out.println(uvphList);
        System.out.println(uph3.toSrilmFlmString());
        System.out.println(uph3.toSrilmDictionaryString());
        System.out.println("The above output is for demonstration and test purposes only and can be ignored.");
        System.out.println("See the javadoc or source code API on how to use this class in other java programs.");

    }
}
