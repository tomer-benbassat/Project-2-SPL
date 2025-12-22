package memory;

import java.util.concurrent.locks.ReadWriteLock;

public class SharedVector {
    //@INV:vector!=null && orientation!=null && lock!=null

    private double[] vector;
    private VectorOrientation orientation;
    //may be used to protect concurrent access to its underlying array when necessary:
    private ReadWriteLock lock = new java.util.concurrent.locks.ReentrantReadWriteLock();

    
    public SharedVector(double[] vector, VectorOrientation orientation) {
        if(vector==null || orientation == null ){
            throw new IllegalArgumentException("null parmaters");
        }
        this.vector = vector;
        this.orientation = orientation;
    }

    public double get(int index) { 
        //basic query
        //@PRE: index is valid -> aka -> 0 <= index < vector.length()
        //@POST:None
        //@Return:element at index
        //Using Read Lock to ensure the data isnt changing while retrieving it
        if(vector == null || index<0 || index>=vector.length){
            throw new IndexOutOfBoundsException("illegal index");
        }
        readLock();
        try{
            return vector[index];
        }finally{
            readUnlock();
        }
    }

    public int length() {
        //basic query
        //@PRE:None
        //@POST:None
        //@Return:Vector length
        //Using Read Lock to ensure the length isnt changging while retrieving it
        readLock();
        try{
            return vector.length;
        }finally{
            readUnlock();
        }
    }


    public VectorOrientation getOrientation() {
        //basic query 
        //@PRE:None
        //@POST:None
        //@Return:Orientation
        //Using Read Lock to ensure the orientation isnt changing while retrieving it
        readLock();
        try{
            return orientation;
        }finally{
            readUnlock();
        }
    }

    public void writeLock() {
        this.lock.writeLock().lock();
    }

    public void writeUnlock() {
        this.lock.writeLock().unlock();
    }

    public void readLock() {
        this.lock.readLock().lock();
    }

    public void readUnlock() {
        this.lock.readLock().unlock();
    }

    
    public void transpose() {
        //basic command
        //@PRE:None
        //@POST:getOrientation() != PRE.Orientation
        //Using Write Lock to ensure no one read incorrect data / write while im writing
        writeLock();
        try{
        if(this.orientation==VectorOrientation.ROW_MAJOR){
            this.orientation = VectorOrientation.COLUMN_MAJOR;
        }
        else{
            this.orientation = VectorOrientation.ROW_MAJOR;
        }
        }finally{
        writeUnlock();
        }
    }

    //
    public void add(SharedVector other) {
        //basic command
        //@PRE:this.length() == other.length()
        //@POST:this.get(i) == (PRE.get(i) + other.get(i)) for each 0<=i<length()
        //Using Write Lock to ensure no one read incorrect data / write while im writing
        writeLock();
        try{
        if(other.length()!=this.length()){
            throw new IllegalArgumentException("lengths does not match");
        }
        for(int i=0; i < this.vector.length; i++){
            double temp = vector[i];
            this.vector[i] = temp + other.get(i);
        }
        }finally{
        writeUnlock();
        }
    }

    public void negate() {
        //basic command
        //@PRE:None
        //@POST:this.get(i) == (PRE.get(i) * (-1)) for each 0<=i<length()
        //Using Write Lock to ensure no one read incorrect data / write while im writing
        writeLock();
        try{
        for(int i=0; i< this.vector.length ; i++){
            this.vector[i] = this.vector[i] * (-1);
        }
        }finally{
        writeUnlock();
        }
    }

    public double dot(SharedVector other) {
        //derived Query(uses get and does not change the structure)
        //@PRE:this.length() == other.length()
        //@POST:uses get() to calculate and RETURN this · other (let's say this is ROW_MAJOR and other COLUMN_MAJOR, otherwise they can be transposed)
        //Using Read Lock to ensure data isnt changing while retrieving it 
        readLock();
        double sum = 0; 
        try{
        if(other.length()!=this.length()){
            throw new IllegalArgumentException("lengths does not match");
        }
        for(int i=0; i < this.vector.length; i++){
            //notice how thread the gets this task will lock vector but also other.get will lock other.vector
            double temp = vector[i]*other.get(i);
            sum = sum + temp;
        }
        return sum;
        }finally{
        readUnlock();
        }
    }
        

    public void vecMatMul(SharedMatrix matrix) {
        // TODO: compute row-vector × matrix
        //derived command(uses matrix getters + dot)
        //@PRE:
            // 0.matrix!=null && matrix.length!=0;
            // 1.matrix.orientation == COLUMN_MAJOR 
            // 2.this.vecotr.length() == matrix.get(0).length() aka number of lines of the matrix
        //@POST:this.vector = vector x matrix
        //Using Write Lock ensure no one read incorrect data / write while im writing
        writeLock();
        try{
        if(matrix==null || matrix.length()==0){
            throw new IllegalArgumentException("matrix is null or empty");
        }
        if(matrix.getOrientation()!=VectorOrientation.COLUMN_MAJOR){
            throw new IllegalArgumentException("require COLUMN_MAJOR matrix");
        }
        if(this.vector.length!=matrix.get(0).length()){
            throw new IllegalArgumentException("dimensions doest not match");
        }
        double[] result = new double[matrix.length()]; //matrix.length() == number of columns
        for(int i=0;i<matrix.length();i++){
            result[i]=this.dot(matrix.get(i));
        }
        vector = result;
        }finally{
            writeUnlock();
        }
    }
}
