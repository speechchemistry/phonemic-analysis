/** 
 * this class represents a phone (segment) that can be represented by a single 
 * vector of features. This is true for most phones. Currently feature values are
 * represented by integers e.g. 1,-1 for binary features with zero representing 
 * the special value 'don't care' (undefined). It might be better to use a data 
 * structure that has special value.
 */

import java.io.*;
import java.util.*;

public class UniVectorPhone implements Comparable<UniVectorPhone>{
    private String label;
    private Map<String,Integer> features;
    public final static Set<String> articUnivSet =
        new HashSet<String>(Arrays.asList("LABIAL","CORONAL","DORSAL","nasal")); // active articulator universe set

    public UniVectorPhone(String ipaLabel,Map<String,Integer> featureMap) {
        label = ipaLabel;
        features = featureMap;
    }
    
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

    public String getIpaLabel() {
        return label;
    }

    public int size() {
        return features.size();
    }
    
    public Integer getFeatureValue(String feature) {
        if (!features.containsKey(feature))  
           System.err.println("Warning: feature "+feature+" not found in uVPhone "+label+" returning null value");
        return features.get(feature);
    }

    public Set<String> getArticSet() {
        Set<String> thisSet = new HashSet<String>(3);
        for (String el : articUnivSet) {
            if (getFeatureValue(el)==1) {thisSet.add(el);}
        }
        return thisSet;
    }

    /** calculates (2x) feature difference, assumes both phones use same feature system. */
    public int doubleFeatureDiff(UniVectorPhone otherUVPhone) {
        Set<String> featureSet = features.keySet();
        int cumDiff = 0;
        for (String el : featureSet) {
            cumDiff += Math.abs(getFeatureValue(el) - otherUVPhone.getFeatureValue(el));
        }
        return cumDiff;
    }

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

    private static boolean featureValueIsBetweenOrEqual(int a, int b, int n) {
        boolean result = ((a<=n) && (n<=b)) || ((b<=n) && (n<=a));
        result = result || (a==0) || (b==0) || (n==0); // zero treated as 'don't care'
        return result;
    }

    public int compareTo(UniVectorPhone other) {
		return this.getFeatureValue("syllabic").compareTo(other.getFeatureValue("syllabic"));
    }
    
    public String toString() {
        //return label+" "+features;
        return label;
    }

    public String toSrilmFlmString(){
    	String s = label;
    	for(Map.Entry<String,Integer> entry:features.entrySet()){
    		s=s+":"+entry.getKey()+"-="+entry.getValue();
    	}
    	return s;
    }
    
    /** Produces a string for the -dictionary-align option in SRILM lattice-tool for
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
    
    /** Produces a string displaying feature values in tsv format */
    /* efficiency saving: could use values rather than entry */
    public String toTsvBodyString(){
    	String s = label;
    	for(Map.Entry<String,Integer> entry:features.entrySet()){
    		s=s+"\t"+entry.getValue();
    	}
    	return s;
    }
    /* (non-Javadoc)
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

	/* (non-Javadoc)
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
    }
}

