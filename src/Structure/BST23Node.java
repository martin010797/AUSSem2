package Structure;

import java.io.*;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

public class BST23Node<T extends Comparable<T> & IData, V extends IData> implements IRecord{
    private static final int UNDEFINED = -1;

    /*private BST23Node _left1;
    private BST23Node _right1;
    private BST23Node _left2;
    private BST23Node _right2;
    private BST23Node _parent;*/
    //toto bude asi v hlavicke
    private boolean _isThreeNode;
    private boolean _isValid;
    private int _address;
    //private int validRecordsCount;
    private int _left1;
    private int _right1;
    private int _left2;
    private int _right2;
    private int _parent;

    private T _data1;
    private T _data2;
    private V _value1;
    private V _value2;

    private Class<T> classTypeKey;
    private Class<V> classTypeValue;

    //public BST23Node(T pData1, V pValue1, Class pClassTypeKey, Class pClassTypeValue){
    public BST23Node(T pData1, V pValue1){
        _data1 = pData1;
        _value1 = pValue1;
        _data2 = null;
        _isThreeNode = false;
        _left1 = UNDEFINED;
        _right1 = UNDEFINED;
        _left2 = UNDEFINED;
        _right2 = UNDEFINED;
        _parent = UNDEFINED;

        classTypeKey = (Class<T>) pData1.getClass();
        classTypeValue = (Class<V>) pValue1.getClass();
        try {
            _data2 = (T) classTypeKey.newInstance().createClass();
            _value2 = (V) classTypeValue.newInstance().createClass();
        }catch (InstantiationException exception){
            Logger.getLogger(UnsortedFile.class.getName()).log(Level.SEVERE, null, exception);
        }catch (IllegalAccessException exception){
            Logger.getLogger(UnsortedFile.class.getName()).log(Level.SEVERE, null, exception);
        }
        _isValid = true;
        _address = UNDEFINED;
    }

    public BST23Node(Class pClassTypeKey, Class pClassTypeValue){
        _isThreeNode = false;
        _left1 = UNDEFINED;
        _right1 = UNDEFINED;
        _left2 = UNDEFINED;
        _right2 = UNDEFINED;
        _isValid = true;
        _address = UNDEFINED;
        _parent = UNDEFINED;

        classTypeKey = pClassTypeKey;
        classTypeValue = pClassTypeValue;
        try {
            _data1 = (T) classTypeKey.newInstance().createClass();
            _value1 = (V) classTypeValue.newInstance().createClass();
            _data2 = (T) classTypeKey.newInstance().createClass();
            _value2 = (V) classTypeValue.newInstance().createClass();
        }catch (InstantiationException exception){
            Logger.getLogger(UnsortedFile.class.getName()).log(Level.SEVERE, null, exception);
        }catch (IllegalAccessException exception){
            Logger.getLogger(UnsortedFile.class.getName()).log(Level.SEVERE, null, exception);
        }
    }

    public BST23Node(T pData1){
        _data1 = pData1;
        _value1 = null;
        _data2 = null;
        _isThreeNode = false;
        _left1 = UNDEFINED;
        _right1 = UNDEFINED;
        _left2 = UNDEFINED;
        _right2 = UNDEFINED;
    }

    @Override
    public byte[] ToByteArray() {
        ByteArrayOutputStream hlpByteArrayOutputStream= new ByteArrayOutputStream();
        DataOutputStream hlpOutStream = new DataOutputStream(hlpByteArrayOutputStream);
        try {
            //hlavicka bloku
            hlpOutStream.writeBoolean(_isThreeNode);
            hlpOutStream.writeBoolean(_isValid);
            hlpOutStream.writeInt(_left1);
            hlpOutStream.writeInt(_right1);
            hlpOutStream.writeInt(_left2);
            hlpOutStream.writeInt(_right2);
            hlpOutStream.writeInt(_parent);
            hlpOutStream.writeInt(_address);
            //kluce a data
            hlpOutStream.write(_data1.ToByteArray());
            hlpOutStream.write(_value1.ToByteArray());
            hlpOutStream.write(_data2.ToByteArray());
            hlpOutStream.write(_value2.ToByteArray());
            return hlpByteArrayOutputStream.toByteArray();
        }catch (IOException exception){
            throw new IllegalStateException("Error during conversion to byte array.");
        }
    }

    @Override
    public void FromByteArray(byte[] pArray) {
        ByteArrayInputStream hlpByteArrayInputStream = new ByteArrayInputStream(pArray);
        DataInputStream hlpInStream = new DataInputStream(hlpByteArrayInputStream);
        try {
            _isThreeNode = hlpInStream.readBoolean();
            _isValid = hlpInStream.readBoolean();
            _left1 = hlpInStream.readInt();
            _right1 = hlpInStream.readInt();
            _left2 = hlpInStream.readInt();
            _right2 = hlpInStream.readInt();
            _parent = hlpInStream.readInt();
            _address = hlpInStream.readInt();

            //nacitanie tried
            //kluc 1
            //preskakuju sa byty v ktorych su hlavickove udaje(cize udaje co sa citaju vyssie)
            int numberOfSkippedByteArrays = 2 + (6 * Integer.BYTES);
            int sizeOfClass = _data1.getSize();
            byte[] partOfByteArray = Arrays.copyOfRange(
                    pArray,
                    numberOfSkippedByteArrays,
                    numberOfSkippedByteArrays + sizeOfClass);
            _data1.FromByteArray(partOfByteArray);

            //hodnota 1
            numberOfSkippedByteArrays += sizeOfClass;
            sizeOfClass = _value1.getSize();
            partOfByteArray = Arrays.copyOfRange(
                    pArray,
                    numberOfSkippedByteArrays,
                    numberOfSkippedByteArrays + sizeOfClass);
            _value1.FromByteArray(partOfByteArray);

            //kluc 2
            numberOfSkippedByteArrays += sizeOfClass;
            sizeOfClass = _data2.getSize();
            partOfByteArray = Arrays.copyOfRange(
                    pArray,
                    numberOfSkippedByteArrays,
                    numberOfSkippedByteArrays + sizeOfClass);
            _data2.FromByteArray(partOfByteArray);

            //hodnota 2
            numberOfSkippedByteArrays += sizeOfClass;
            sizeOfClass = _value2.getSize();
            partOfByteArray = Arrays.copyOfRange(
                    pArray,
                    numberOfSkippedByteArrays,
                    numberOfSkippedByteArrays + sizeOfClass);
            _value2.FromByteArray(partOfByteArray);
        }catch (IOException exception){
            Logger.getLogger(UnsortedFile.class.getName()).log(Level.SEVERE, null, exception);
        }
    }

    @Override
    public int getSize() {
        try {
            return (2 +
                    (6 * Integer.BYTES) +
                    (2 * classTypeKey.newInstance().getSize()) +
                    (2 * classTypeValue.newInstance().getSize()));
        }catch (InstantiationException exception){
            Logger.getLogger(UnsortedFile.class.getName()).log(Level.SEVERE, null, exception);
        }catch (IllegalAccessException exception){
            Logger.getLogger(UnsortedFile.class.getName()).log(Level.SEVERE, null, exception);
        }
        return 0;
    }

    @Override
    public boolean isValid() {
        return _isValid;
    }

    @Override
    public void setValid(boolean pValid) {
        _isValid = pValid;
    }

    public int get_address() {
        return _address;
    }

    public void set_address(int _address) {
        this._address = _address;
    }

    public int get_parent() {
        return _parent;
    }

    public void set_parent(int _parent) {
        this._parent = _parent;
    }

    public T get_data1() {
        return _data1;
    }

    public void set_data1(T _data1) {
        if (_data1 == null){
            //this._data1.setValid(false);
            try {
                this._data1 = (T) classTypeKey.newInstance().createClass();
            }catch (InstantiationException exception){
                Logger.getLogger(UnsortedFile.class.getName()).log(Level.SEVERE, null, exception);
            }catch (IllegalAccessException exception){
                Logger.getLogger(UnsortedFile.class.getName()).log(Level.SEVERE, null, exception);
            }
        }else {
            this._data1 = _data1;
        }
    }

    public T get_data2() {
        return _data2;
    }

    public void set_data2(T _data2) {
        if (_data2 == null){
            //this._data2.setValid(false);
            try {
                this._data2 = (T) classTypeKey.newInstance().createClass();
            }catch (InstantiationException exception){
                Logger.getLogger(UnsortedFile.class.getName()).log(Level.SEVERE, null, exception);
            }catch (IllegalAccessException exception){
                Logger.getLogger(UnsortedFile.class.getName()).log(Level.SEVERE, null, exception);
            }
        }else {
            this._data2 = _data2;
        }
    }

    public boolean isThreeNode() {
        return _isThreeNode;
    }

    public void set_isThreeNode(boolean _isThreeNode) {
        this._isThreeNode = _isThreeNode;
    }

    public int get_left1() {
        return _left1;
    }

    public void set_left1(int _left1) {
        this._left1 = _left1;
    }

    public int get_right1() {
        return _right1;
    }

    public void set_right1(int _right1) {
        this._right1 = _right1;
    }

    public int get_left2() {
        return _left2;
    }

    public void set_left2(int _left2) {
        this._left2 = _left2;
    }

    public int get_right2() {
        return _right2;
    }

    public void set_right2(int _right2) {
        this._right2 = _right2;
    }

    public V get_value1() {
        return _value1;
    }

    public void set_value1(V _value1) {
        if (_value1 == null){
            //this._value1.setValid(false);
            try {
                this._value1 = (V) classTypeValue.newInstance().createClass();
            }catch (InstantiationException exception){
                Logger.getLogger(UnsortedFile.class.getName()).log(Level.SEVERE, null, exception);
            }catch (IllegalAccessException exception){
                Logger.getLogger(UnsortedFile.class.getName()).log(Level.SEVERE, null, exception);
            }
        }else {
            this._value1 = _value1;
        }
    }

    public V get_value2() {
        return _value2;
    }

    public void set_value2(V _value2) {
        if (_value2 == null){
            //this._value2.setValid(false);
            try {
                this._value2 = (V) classTypeValue.newInstance().createClass();
            }catch (InstantiationException exception){
                Logger.getLogger(UnsortedFile.class.getName()).log(Level.SEVERE, null, exception);
            }catch (IllegalAccessException exception){
                Logger.getLogger(UnsortedFile.class.getName()).log(Level.SEVERE, null, exception);
            }
        }else {
            this._value2 = _value2;
        }
    }
}
