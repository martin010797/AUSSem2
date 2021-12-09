package Structure;

public class NodeWithKey<T extends  Comparable<T>, V>{
    private BST23Node<T,V> node;
    private T key;

    public NodeWithKey(BST23Node pNode, T pKey){
        node = pNode;
        key = pKey;
    }

    public BST23Node<T, V> getNode() {
        return node;
    }

    public void setNode(BST23Node<T, V> node) {
        this.node = node;
    }

    public T getKey() {
        return key;
    }

    public void setKey(T key) {
        this.key = key;
    }
}
