import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/** 
 * Dynamic Time Warping using an algorithm from Daniel P. W. Ellis.
 * This Java code is derived from the original Matlab code. 
 * @author Tim Kempton, Dan Ellis
 * @version 0.1
 
This code is derived from:

Dynamic Time Warp in Matlab - code for aligning temporal sequences
Copyright (C) 2003 Daniel P. W. Ellis <dpwe@ee.columbia.edu>

This program is free software; you can redistribute it and/or
modify it under the terms of the GNU General Public License
as published by the Free Software Foundation; either version 2
of the License, or (at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You can view the GNU General Public License at:
  http://www.gnu.org/copyleft/gpl.html
or by writing to the Free Software Foundation, Inc., 51 Franklin
Street, Fifth Floor, Boston, MA 02110-1301, USA.

(So note that this particular class is licenced under the GNU GPL)
(See original code at http://www.ee.columbia.edu/~dpwe/resources/matlab/dtw/ )
*/

public class DanEllisDtw {

        /** The cost matrix. */
	private double[][] d; 
        /** The direction matrix. */
	private int[][] phi;
        /** The state sequence of the first dimension. (Rows?) */
	private List<Integer> p; 
        /** The state sequence of the second dimension. (Columns?) */
	private List<Integer> q; 
	
	/** Create and calculate the Dynamic Time Warp from a 2D array of numbers. */
	public DanEllisDtw(double[][] m) {
		int r=m.length;
		int c=m[0].length;
		//System.out.print("r="+r+" c="+c);
		
		// initialise cost matrix
		d = new double[r+1][c+1];
		d[0][0] = 0;
		for(int i=1;i<r+1;i++) d[i][0]=Double.NaN;
		for(int j=1;j<c+1;j++) d[0][j]=Double.NaN;
		
		for(int i=1;i<r+1;i++) {
			for(int j=1;j<c+1;j++) 
				d[i][j] = m[i-1][j-1];
		}
		//System.out.println(Arrays.deepToString(d));
		
		phi = new int[r][c]; // initialise to zero
		//System.out.println(Arrays.deepToString(phi));
		
		// forward pass 
		for(int i=0;i<r;i++){
			for(int j=0;j<c;j++){
				ArrayList<Double> threePaths = new ArrayList<Double>(Arrays.asList(d[i][j],d[i][j+1],d[i+1][j]));
				double dm = Collections.min(threePaths);
				int tb = threePaths.indexOf(dm)+1;
				d[i+1][j+1]=d[i+1][j+1] + dm;
				phi[i][j]=tb;
				//System.out.println("threePaths="+threePaths+" dm="+dm+" tb="+tb);
			}
		}
		
		//System.out.println(Arrays.deepToString(d));
		//System.out.println(Arrays.deepToString(phi));
		
		// traceback
		int i = r-1;
		int j = c-1;
		p=new LinkedList<Integer>();
		q=new LinkedList<Integer>();
		p.add(0,i);
		q.add(0,j);
		while(i>0 && j>0){
			int tb = phi[i][j];
			if (tb == 1) {
				i=i-1;
				j=j-1;
			}else if (tb == 2) {
				i=i-1;
			}else if (tb == 3) {
				j=j-1;
			}else throw new RuntimeException("found invalid direction code during traceback; perhaps forward scan was incomplete");
			p.add(0,i);
			q.add(0,j);
		}
		
		double[][] dTmp = new double[r][c];
		for(i=0;i<r;i++)
			System.arraycopy(d[i+1], 1, dTmp[i], 0, c);
		//System.out.println("d="+Arrays.deepToString(d));
		//System.out.println("dTmp="+Arrays.deepToString(dTmp));
		d=dTmp;
		//System.out.println("q="+q);
	
		//** now need to trim d **
		//for(int i=0;i<r;i++){
		//	for(int j=0;j<c;j++){
				
	}

	/**
	 * @return the state sequence P
	 */
	public List<Integer> getP() {
		return p;
	}

	/**
	 * @return the state sequence Q
	 */
	public List<Integer> getQ() {
		return q;
	}

	/**
	 * @return the cost array
	 */
	public double[][] getD() {
		return d;
	}

	/**
	 * @return total cost
	 */
	public double getTotalCost() {
                double[] lastRow = d[d.length - 1];
                double lastItem = lastRow[lastRow.length - 1];
		return lastItem;
	}

	/**
	 * Test the Dynamic Time Warp algorithm with a sample matrix.
	 */
	public static void main(String[] args) {
                System.out.println("Sample test - the final cost (final figure below) should be 4.0");
		double[][] smallArray = {{1,2,3,4},{1,1,1,8},{3,2,1,1}};
		DanEllisDtw dtw = new DanEllisDtw(smallArray);
		System.out.println(dtw.getP());
		System.out.println(dtw.getQ());
		System.out.println("d="+Arrays.deepToString(dtw.getD()));
                System.out.println("i.e. total cost ="+dtw.getTotalCost());
	}

}
