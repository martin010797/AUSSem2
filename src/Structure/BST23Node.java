package Structure;

public class BST23Node<T extends Comparable<T>, V> {
    private BST23Node _left1;
    private BST23Node _right1;
    private BST23Node _left2;
    private BST23Node _right2;
    private BST23Node _parent;
    private T _data1;
    private T _data2;
    private V _value1;
    private V _value2;
    private boolean _isThreeNode;

    public BST23Node(T pData1, V pValue1){
        _data1 = pData1;
        _value1 = pValue1;
        _data2 = null;
        _isThreeNode = false;
        _left1 = null;
        _right1 = null;
        _left2 = null;
        _right2 = null;
    }

    public BST23Node(T pData1){
        _data1 = pData1;
        _value1 = null;
        _data2 = null;
        _isThreeNode = false;
        _left1 = null;
        _right1 = null;
        _left2 = null;
        _right2 = null;
    }

    public BST23Node get_parent() {
        return _parent;
    }

    public void set_parent(BST23Node _parent) {
        this._parent = _parent;
    }

    public T get_data1() {
        return _data1;
    }

    public void set_data1(T _data1) {
        this._data1 = _data1;
    }

    public T get_data2() {
        return _data2;
    }

    public void set_data2(T _data2) {
        this._data2 = _data2;
    }

    public boolean isThreeNode() {
        return _isThreeNode;
    }

    public void set_isThreeNode(boolean _isThreeNode) {
        this._isThreeNode = _isThreeNode;
    }

    public BST23Node get_left1() {
        return _left1;
    }

    public void set_left1(BST23Node _left1) {
        this._left1 = _left1;
    }

    public BST23Node get_right1() {
        return _right1;
    }

    public void set_right1(BST23Node _right1) {
        this._right1 = _right1;
    }

    public BST23Node get_left2() {
        return _left2;
    }

    public void set_left2(BST23Node _left2) {
        this._left2 = _left2;
    }

    public BST23Node get_right2() {
        return _right2;
    }

    public void set_right2(BST23Node _right2) {
        this._right2 = _right2;
    }

    public V get_value1() {
        return _value1;
    }

    public void set_value1(V _value1) {
        this._value1 = _value1;
    }

    public V get_value2() {
        return _value2;
    }

    public void set_value2(V _value2) {
        this._value2 = _value2;
    }
}
