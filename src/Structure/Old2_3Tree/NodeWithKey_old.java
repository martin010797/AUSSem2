package Structure.Old2_3Tree;

public class NodeWithKey_old<T extends  Comparable<T>, V>{
    private BST23Node_old<T,V> node;
    private T key;

    public NodeWithKey_old(BST23Node_old pNode, T pKey){
        node = pNode;
        key = pKey;
    }

    public BST23Node_old<T, V> getNode() {
        return node;
    }

    public void setNode(BST23Node_old<T, V> node) {
        this.node = node;
    }

    public T getKey() {
        return key;
    }

    public void setKey(T key) {
        this.key = key;
    }
}
