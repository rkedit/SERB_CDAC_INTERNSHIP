/**
 * <b> in.datatype package </b>:  contains the collection of files created to be used as data type entities
 */
package in.datatype;

/**
 * The <b>StatProp</b> is used as a data type to capture the statistical properties associated with the occurrences 
 * of various programming constructs. The private data members of the class consist of the Double type objects storing the
 * maximum (max), minimum (min), average (avg), and standard deviation (stdDev) values associated with a certain construct type. 
 *<p>
 *The class contains the public member functions (or methods) to access its private data members.
 */
public class StatProp
{
	private Double maxVal;	
	private Double minVal;
	private Double avgVal;
	private Double stdDevVal;
	
	/**
	 * Constructor to initialize the private data members of the class. 
	 * @param maxV  a Double type object specifying the maximum value associated with a construct
	 * @param minV	a Double type object specifying the minimum value associated with a construct
	 * @param avgV	a Double type object specifying the average value associated with a construct
	 * @param stdDevV	a Double type object specifying the standard deviation value associated with a construct
	 */
		public StatProp(Double maxV, Double minV, Double avgV, Double stdDevV) 
		{
			this.maxVal = maxV;
			this.minVal= minV;
			this.avgVal = avgV;
			this.stdDevVal = stdDevV;
		}
		//public member functions
		
		/**
		 * A public member function used to fetch the value of the private data member <b>maxVal</b> of the class.
		 * @return the value of maxVal (private data member) returned as a Double object
		 */
		public Double getMaxValue()
		{
			return this.maxVal;
		}
		
		/**
		 * A public member function used to fetch the value of the private data member <b>minVal</b> of the class.
		 * @return the value of minVal (private data member) returned as a Double object
		 */
		public Double getMinValue()
		{
			return this.minVal;
		}
		
		/**
		 * A public member function used to fetch the value of the private data member <b>avgVal</b> of the class.
		 * @return the value of avgVal (private data member) returned as a Double object
		 */
		public Double getAvgValue()
		{
			return this.avgVal;
		}
		
		/**
		 * A public member function used to fetch the value of the private data member <b>stdDevVal</b> of the class.
		 * @return the value of stdDevVal (private data member) returned as a Double object
		 */
		public Double getStdDevValue()
		{
			return this.stdDevVal;
		}
}
