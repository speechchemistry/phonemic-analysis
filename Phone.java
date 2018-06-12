import java.io.*;
import java.util.*;

/** 
 * Models a phone (segment) that can be represented by a multiple 
 * vectors of features. Multiple (usually 1 or 2) 
 * UniVectorPhone objects are used.
 * @author Tim Kempton
 * @version 0.1
 */
public class Phone implements Comparable<Phone>{
    /** Phone label in IPA e.g. d */
    private String label;

    /** List of UniVectorPhone objects */
    private List<UniVectorPhone> comp; // component phone(s); usually just 1
 
    /** Create phone from IPA label and a list of UniVectorPhone objects */
    public Phone(String ipaLabel, List<UniVectorPhone> uVPhoneList) {
        label = ipaLabel;
        comp = uVPhoneList;
    }

    /** Gets the IPA label */
    public String getIpaLabel() {
        return label;
    }

    /** Gets the list of UniVectorPhone objects */
    public List<UniVectorPhone> getComponentPhones() {
        return comp;
    }

    /** Produces a standard string representation */
    public String toString() {
        //return "\n"+label+" "+comp;
        return label;
    }

    /** Produces a string representation for the SRILM factored language model */
    public String toSrilmFlmString(){
    	String s = "";
    	for(UniVectorPhone u:comp){
    		s=s+" "+u.toSrilmFlmString();
    	}
    	return s;
    }
    
    /** Produces a string for the -dictionary-align option in SRILM lattice-tool for
     * phone lattices being transformed into sausages. Currently only prints out the
     * first uniVectorPhone which is not ideal but keeps the rest of the processing
     * e.g. srilm mesh generation, simple. 
     */ 
    public String toFirstElementSrilmDictionaryString(){
    	String s = label;
    	s=s+"\t"+comp.get(0).toSrilmDictionaryString();
    	return s;
    }
    
    /* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((comp == null) ? 0 : comp.hashCode());
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
		Phone other = (Phone) obj;
		if (comp == null) {
			if (other.comp != null)
				return false;
		} else if (!comp.equals(other.comp))
			return false;
		if (label == null) {
			if (other.label != null)
				return false;
		} else if (!label.equals(other.label))
			return false;
		return true;
	}

	/** Compare first components. */
	public int compareTo(Phone o) {
		//return this.getComponentPhones().get(0).compareTo(o.getComponentPhones().get(0));
		return this.getIpaLabel().compareTo(o.getIpaLabel());
	}

	/** Get number of features. */
	public int numberOfFeatures() {
        int size = 0;
        int oldSize = 0;
        boolean loopedAlready = false;
        // check number of features match
        for(UniVectorPhone uVPhone : comp) {
            size = uVPhone.size();        
            if (loopedAlready && (size!=oldSize))
                throw new RuntimeException("Number of features different between components of phone "+this);
            oldSize = size;
            loopedAlready = true;
        }
        return size;
    }

    /** Calculate whether this Phone and the other specified Phone have different active articulators.
      * See Kempton(2012) p49 and p59. */
    public boolean hasDifferentArticulators(Phone otherPhone) {
        // get articulator set from this phone
        Set<String> thisArticSet = new HashSet<String>(3);
        for(UniVectorPhone uVPhone : comp) {
            thisArticSet.addAll(uVPhone.getArticSet()); // set union
            if (uVPhone.getFeatureValue("anterior")==-1) thisArticSet.add("pal_area");
        }
        if (thisArticSet.contains("DORSAL")) thisArticSet.add("pal_area");
        //System.out.println("thisArticSet="+thisArticSet);
        // get articulator set from other phone
        Set<String> otherArticSet = new HashSet<String>(3);
        List<UniVectorPhone> otherComp = otherPhone.getComponentPhones();
        for(UniVectorPhone uVPhone : otherComp) {
            otherArticSet.addAll(uVPhone.getArticSet()); // set union
            if (uVPhone.getFeatureValue("anterior")==-1) otherArticSet.add("pal_area");
        }
        if (otherArticSet.contains("DORSAL")) otherArticSet.add("pal_area");
        //System.out.println("otherArticSet="+otherArticSet);
        boolean bothPhonesHaveArtic = (!thisArticSet.isEmpty()) && (!otherArticSet.isEmpty());
        thisArticSet.retainAll(otherArticSet); // do set intersection
        return thisArticSet.isEmpty() && bothPhonesHaveArtic;
    }

    /** This just converts averageFeatureDistance() to a similarity measure. */ 
    public double averageCountOfSameFeatures(Phone otherPhone) {
        int numberOfFeatures = this.numberOfFeatures();
        if (otherPhone.numberOfFeatures() != numberOfFeatures) // haven't managed to test branching code yet
                throw new RuntimeException("Number of features different between phones "+this+" and "+otherPhone);
        return numberOfFeatures - averageFeatureDistance(otherPhone);
    }

    /** Calculates the binary feature edits per phone (BFEPP) measure. See Kempton(2012) p51, p55-58.
      *  The implementation here for multiple feature vectors has an emphasis on simplicity and speed, so
      *  dynamic programming is not fully implemented,  but rather an approximation is made.
      *  For 99% of phones,  the results should be identical (this still needs to be fully verified) */
    // can be thought of aligning two sequences (with uniform costs - so nothing complicted 
    // like DP) and then working out which elements 
    // are associated with each other to determin the everage distance.
    // Alignment is like drawing a diaganal line through a rectangle.
    public double averageFeatureDistance(Phone otherPhone) {
        List<UniVectorPhone> otherComp = otherPhone.getComponentPhones();
        int thisSize = comp.size(); // x axis length
        int otherSize = otherComp.size(); // y axis length 
        double m = (1.0*otherSize)/thisSize; // gradiant of line
        int dblFeatDiff = 0; //running total of 2x feature difference
        int calcCount = 0; //count of distances calculated (poss could be calc'd directly)
        UniVectorPhone thisUVPhone = null;
        UniVectorPhone otherUVPhone = null;
        for(int i=0;i<thisSize;i++) {
            for(int j=0;j<otherSize;j++) {
                if (isOverlappingInterval(j,j+1,i*m,(i+1)*m)) { // if line goes through cell
                    thisUVPhone = comp.get(i);
                    otherUVPhone = otherComp.get(j);
                    //System.out.print(" "+thisUVPhone+" and "+otherUVPhone);
                    dblFeatDiff += thisUVPhone.doubleFeatureDiff(otherUVPhone);
                    //System.out.println(" dist="+(0.5*thisUVPhone.doubleFeatureDiff(otherUVPhone)));
                    calcCount++;
                }
            }
        } 
        //System.out.print(this+" and "+otherPhone);
        //System.out.println(" average dist="+((0.5*dblFeatDiff)/calcCount));
        return (0.5*dblFeatDiff)/calcCount;
    }

    /** Check to see whether interval 1 overlaps with interval 2 */
    private boolean isOverlappingInterval(double s1,double e1,double s2,double e2) {
        return (s2 < e1) && (s1 < e2);
    }

    /** Check to see if all components of this phone are between all 
      * possible component associations of phoneA and phoneB. This is the relative minimal 
      * difference heuristic as described in Kempton (2012) p43-44, p58, in turn based on 
      * Pepperkamp et al. (2006). */
    public boolean isAllBetweenOrOnBoundary(Phone phoneA, Phone phoneB) {
        Set<UniVectorPhone> thisSet = new HashSet<UniVectorPhone>(this.getComponentPhones());
        Set<UniVectorPhone> aSet = new HashSet<UniVectorPhone>(phoneA.getComponentPhones());
        Set<UniVectorPhone> bSet = new HashSet<UniVectorPhone>(phoneB.getComponentPhones());
        boolean isAll = true;
        for(UniVectorPhone n : thisSet) {
            for(UniVectorPhone a : aSet) {
                for(UniVectorPhone b : bSet) {
                        isAll = isAll && n.isBetweenOrOnBoundary(a,b);
                }
            }
        }
        return isAll;
    }

    /** Demonstration and test */
    public static void main(String[] args) throws IOException {
        Map<String,Integer> fMap = new LinkedHashMap<String,Integer>();
        fMap.put("DORSAL",1);
        fMap.put("high",-1);
        fMap.put("LABIAL",-1);
        fMap.put("CORONAL",-1);
        fMap.put("nasal",-1);
        fMap.put("anterior",0);
        UniVectorPhone fPhone = new UniVectorPhone("a",fMap);
        Map<String,Integer> sMap = new LinkedHashMap<String,Integer>();
        sMap.put("DORSAL",1);
        sMap.put("high",1);
        sMap.put("LABIAL",-1);
        sMap.put("CORONAL",-1);
        sMap.put("nasal",-1);
        sMap.put("anterior",0);
        UniVectorPhone sPhone = new UniVectorPhone("i",sMap);
        ArrayList<UniVectorPhone> uVPhoneList = new ArrayList<UniVectorPhone>();
        uVPhoneList.add(fPhone);
        uVPhoneList.add(sPhone);        
        uVPhoneList.add(fPhone);
        Phone ph = new Phone("aia",uVPhoneList);
        System.out.println(ph);
        System.out.println("numberOfFeatures="+ph.numberOfFeatures());

        Map<String,Integer> tMap = new LinkedHashMap<String,Integer>();
        tMap.put("DORSAL",-1);
        tMap.put("high",0);
        tMap.put("LABIAL",-1);
        tMap.put("CORONAL",1);
        tMap.put("nasal",-1);
        tMap.put("anterior",-1);
        UniVectorPhone tPhone = new UniVectorPhone("S",tMap);

        Map<String,Integer> foMap = new LinkedHashMap<String,Integer>();
        foMap.put("DORSAL",-1);
        foMap.put("high",0);
        foMap.put("LABIAL",-1);
        foMap.put("CORONAL",1);
        foMap.put("nasal",1);
        foMap.put("anterior",1);
        UniVectorPhone foPhone = new UniVectorPhone("n",foMap);
        ArrayList<UniVectorPhone> uVPhoneListB = new ArrayList<UniVectorPhone>();
        uVPhoneListB.add(tPhone);
        uVPhoneListB.add(foPhone);
        Phone phB = new Phone("Sn",uVPhoneListB);
        System.out.println(phB);
        System.out.println("hasDifferentArticulators="+ph.hasDifferentArticulators(phB));
        double distTest = ph.averageFeatureDistance(phB);
        System.out.println("distTest="+distTest);
        System.out.println("sameTest="+ph.averageCountOfSameFeatures(phB));
        OutputStreamWriter out = new OutputStreamWriter(new ByteArrayOutputStream());
        System.out.println(out.getEncoding());
        System.out.println(phB.toSrilmFlmString());
        System.out.println(phB.toFirstElementSrilmDictionaryString());
        System.out.println("The above output is for demonstration and test purposes only and can be ignored.");
        System.out.println("See the javadoc or source code API on how to use this class in other java programs.");
    }

}

