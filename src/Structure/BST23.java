package Structure;

import java.util.ArrayList;

public class BST23<T extends  Comparable<T>, V> {

    private BST23Node<T,V> _root;

    public BST23(){
        _root = null;
    }

    //robene podla prednaskoveho pseudkokodu v kombinacii s poskytnutou strankou z elearningu na vizualizaciu
    public boolean delete(BST23Node pNode){
        BST23Node deletedNode = find(pNode);
        if (deletedNode != null){
            if (deletedNode == _root && isLeaf(deletedNode) && !deletedNode.isThreeNode()){
                //jediny prvok v strome tak sa zmaze referencia na root
                _root = null;
                return true;
            }
            if (deletedNode.isThreeNode() && isLeaf(deletedNode)){
                if (pNode.get_data1().compareTo(deletedNode.get_data1()) == 0){
                    deletedNode.set_data1(deletedNode.get_data2());
                    deletedNode.set_value1(deletedNode.get_value2());
                    deletedNode.set_data2(null);
                    deletedNode.set_value2(null);
                    deletedNode.set_isThreeNode(false);
                    return true;
                }else if (pNode.get_data1().compareTo(deletedNode.get_data2()) == 0){
                    deletedNode.set_data2(null);
                    deletedNode.set_value2(null);
                    deletedNode.set_isThreeNode(false);
                    return true;
                }
            }
            BST23Node alterNode = findInOrderNode(deletedNode, pNode);
            if (alterNode != null && alterNode.isThreeNode()){
                //jeho nasledovnik je 3 vrchol
                if (pNode.get_data1().compareTo(deletedNode.get_data1()) == 0){
                    deletedNode.set_data1(alterNode.get_data1());
                    deletedNode.set_value1(alterNode.get_value1());
                    alterNode.set_data1(alterNode.get_data2());
                    alterNode.set_value1(alterNode.get_value2());
                    alterNode.set_data2(null);
                    alterNode.set_value2(null);
                    alterNode.set_isThreeNode(false);
                    return true;
                }else if (pNode.get_data1().compareTo(deletedNode.get_data2()) == 0){
                    deletedNode.set_data2(alterNode.get_data1());
                    deletedNode.set_value2(alterNode.get_value1());
                    alterNode.set_data1(alterNode.get_data2());
                    alterNode.set_value1(alterNode.get_value2());
                    alterNode.set_data2(null);
                    alterNode.set_value2(null);
                    alterNode.set_isThreeNode(false);
                    return true;
                }
            }
            //vymazanie prvku a tym vytvorenie prazdneho miesta v alter node
            if (pNode.get_data1().compareTo(deletedNode.get_data1()) == 0){
                deletedNode.set_data1(alterNode.get_data1());
                deletedNode.set_value1(alterNode.get_value1());
            }else if (pNode.get_data1().compareTo(deletedNode.get_data2()) == 0){
                deletedNode.set_data2(alterNode.get_data1());
                deletedNode.set_value2(alterNode.get_value1());
            }
            while (alterNode != null){
                if(alterNode == _root){
                    if (alterNode.get_left1() == null && alterNode.get_right1() == null){
                        _root = null;
                        return true;
                    }else if (alterNode.get_left1() != null && alterNode.get_right1() == null){
                        _root = alterNode.get_left1();
                        _root.set_parent(null);
                        return true;
                    }else if(alterNode.get_left1() == null && alterNode.get_right1() != null){
                        _root = alterNode.get_right1();
                        _root.set_parent(null);
                        return true;
                    }
                }
                BST23Node brotherNode = findBrother(alterNode);
                if (brotherNode.isThreeNode()){
                    //Ko presuniem na prazdne miesto a z brata presuniem do otca
                    if (alterNode.get_parent().get_left1() == alterNode){
                        //prazdne miesto je vlavo od otca takze sa vykonava lava rotacia
                        alterNode.set_data1(alterNode.get_parent().get_data1());
                        alterNode.set_value1(alterNode.get_parent().get_value1());
                        alterNode.get_parent().set_data1(brotherNode.get_data1());
                        alterNode.get_parent().set_value1(brotherNode.get_value1());

                        brotherNode.set_data1(brotherNode.get_data2());
                        brotherNode.set_value1(brotherNode.get_value2());
                        brotherNode.set_data2(null);
                        brotherNode.set_value2(null);
                        brotherNode.set_isThreeNode(false);

                        //nastavenie referencii
                        if ((alterNode.get_left1() != null || alterNode.get_right1() != null) &&
                                (brotherNode.get_left1() != null)) {
                            /*if (alterNode.get_right1() != null && alterNode.get_left1() == null){
                                System.out.println("chyba ");
                            }*/
                            //ak maju v aj jeho brat synov tak uprav referecnie
                            alterNode.set_right1(brotherNode.get_left1());
                            brotherNode.get_left1().set_parent(alterNode);
                            brotherNode.set_left1(brotherNode.get_right1());
                            brotherNode.set_right1(brotherNode.get_right2());
                            brotherNode.set_right2(null);
                            brotherNode.set_left2(null);
                        }
                        return true;
                    }
                    if(alterNode.get_parent().get_right2() != null && alterNode.get_parent().get_right2() == alterNode){
                        //prazdne miesto je napravo od otca takze sa vykonava prava rotacia
                        alterNode.set_data1(alterNode.get_parent().get_data2());
                        alterNode.set_value1(alterNode.get_parent().get_value2());
                        alterNode.get_parent().set_data2(brotherNode.get_data2());
                        alterNode.get_parent().set_value2(brotherNode.get_value2());

                        brotherNode.set_data2(null);
                        brotherNode.set_value2(null);
                        brotherNode.set_isThreeNode(false);

                        //nastavenie referencii
                        if ((alterNode.get_left1() != null || alterNode.get_right1() != null) &&
                                (brotherNode.get_left1() != null)) {
                            if (alterNode.get_left1() != null && alterNode.get_right1() == null){
                                alterNode.set_right1(alterNode.get_left1());
                                alterNode.set_left1(null);
                            }
                            //ak maju v aj jeho brat synov tak uprav referecnie
                            alterNode.set_left1(brotherNode.get_right2());
                            brotherNode.get_right2().set_parent(alterNode);
                            brotherNode.set_right2(null);
                            brotherNode.set_left2(null);
                        }
                        return true;
                    }
                    if (alterNode.get_parent().get_left1() == brotherNode){
                        //brat je nalavo od otca, cize prazdne miesto je v strede takze sa vykonava prava rotacia
                        alterNode.set_data1(alterNode.get_parent().get_data1());
                        alterNode.set_value1(alterNode.get_parent().get_value1());
                        alterNode.get_parent().set_data1(brotherNode.get_data2());
                        alterNode.get_parent().set_value1(brotherNode.get_value2());

                        brotherNode.set_data2(null);
                        brotherNode.set_value2(null);
                        brotherNode.set_isThreeNode(false);

                        //nastavenie referencii
                        if ((alterNode.get_left1() != null || alterNode.get_right1() != null) &&
                                (brotherNode.get_left1() != null)){
                            //ak maju v aj jeho brat synov tak uprav referecnie
                            if (alterNode.get_left1() != null && alterNode.get_right1() == null){
                                alterNode.set_right1(alterNode.get_left1());
                                alterNode.set_left1(null);
                            }
                            alterNode.set_left1(brotherNode.get_right2());
                            brotherNode.get_right2().set_parent(alterNode);
                            brotherNode.set_right2(null);
                            brotherNode.set_left2(null);
                        }
                        return true;
                    }
                    if (alterNode.get_parent().get_right2() != null && alterNode.get_parent().get_right2() == brotherNode){
                        //brat je napravo od otcovho druheho prvku, cize prazdne miesto je v strede takze sa vykonava lava rotacia
                        alterNode.set_data1(alterNode.get_parent().get_data2());
                        alterNode.set_value1(alterNode.get_parent().get_value2());
                        alterNode.get_parent().set_data2(brotherNode.get_data1());
                        alterNode.get_parent().set_value2(brotherNode.get_value1());

                        brotherNode.set_data1(brotherNode.get_data2());
                        brotherNode.set_value1(brotherNode.get_value2());
                        brotherNode.set_data2(null);
                        brotherNode.set_value2(null);
                        brotherNode.set_isThreeNode(false);

                        //nastavenie referencii
                        if ((alterNode.get_left1() != null || alterNode.get_right1() != null) &&
                                (brotherNode.get_left1() != null)){
                            //ak maju v aj jeho brat synov tak uprav referecnie
                            if (alterNode.get_right1() != null && alterNode.get_left1() == null){
                                alterNode.set_left1(alterNode.get_right1());
                                alterNode.set_right1(null);
                            }
                            alterNode.set_right1(brotherNode.get_left1());
                            brotherNode.get_left1().set_parent(alterNode);
                            brotherNode.set_left1(brotherNode.get_right1());
                            brotherNode.set_right1(brotherNode.get_right2());
                            brotherNode.set_right2(null);
                            brotherNode.set_left2(null);
                        }
                        return true;
                    }
                }else {
                    //brat je len dvojvrchol
                    if (!alterNode.get_parent().isThreeNode()) {
                        //otec je dvojvrchol
                        if (alterNode.get_parent().get_right1() == brotherNode){
                            //v' je pravy syn
                            brotherNode.set_data2(brotherNode.get_data1());
                            brotherNode.set_value2(brotherNode.get_value1());
                            brotherNode.set_data1(alterNode.get_parent().get_data1());
                            brotherNode.set_value1(alterNode.get_parent().get_value1());
                            brotherNode.set_isThreeNode(true);
                            alterNode.set_data1(null);
                            alterNode.set_value1(null);
                            alterNode.get_parent().set_data1(null);
                            alterNode.get_parent().set_value1(null);
                            alterNode.get_parent().set_left1(brotherNode);
                            alterNode.get_parent().set_right1(null);

                            if (!isLeaf(alterNode)){
                                //upravy referencii ak nie su listy
                                brotherNode.set_right2(brotherNode.get_right1());
                                brotherNode.set_left2(brotherNode.get_left1());
                                brotherNode.set_right1(brotherNode.get_left1());
                                if (alterNode.get_left1() != null){
                                    brotherNode.set_left1(alterNode.get_left1());
                                }else {
                                    brotherNode.set_left1(alterNode.get_right1());
                                }
                                if (brotherNode.get_left1() != null){
                                    brotherNode.get_left1().set_parent(brotherNode);
                                }
                                if (brotherNode.get_parent().get_parent() == null){
                                    //prazdne miesto je v koreni
                                    _root = brotherNode;
                                    brotherNode.set_parent(null);
                                    return true;
                                }
                            }
                        }else {
                            //v' je lavy syn
                            brotherNode.set_data2(alterNode.get_parent().get_data1());
                            brotherNode.set_value2(alterNode.get_parent().get_value1());
                            brotherNode.set_isThreeNode(true);
                            alterNode.set_data1(null);
                            alterNode.set_value1(null);
                            alterNode.get_parent().set_data1(null);
                            alterNode.get_parent().set_value1(null);
                            alterNode.get_parent().set_right1(null);
                            alterNode.get_parent().set_left1(brotherNode);

                            if (!isLeaf(alterNode)){
                                //upravy referencii ak nie su listy
                                brotherNode.set_left2(brotherNode.get_right1());
                                if (alterNode.get_left1() != null){
                                    brotherNode.set_right2(alterNode.get_left1());
                                }else {
                                    brotherNode.set_right2(alterNode.get_right1());
                                }
                                if (brotherNode.get_right2() != null){
                                    brotherNode.get_right2().set_parent(brotherNode);
                                }
                                if (brotherNode.get_parent().get_parent() == null){
                                    //prazdne miesto je v koreni
                                    _root = brotherNode;
                                    brotherNode.set_parent(null);
                                    return true;
                                }
                            }
                        }
                        alterNode = alterNode.get_parent();
                    }else {
                        //otec je trojvrchol
                        if (alterNode.get_parent().get_right1() == alterNode ||
                                alterNode.get_parent().get_right2() == alterNode){
                            //v je v strede alebo uplne napravo
                            if (alterNode.get_parent().get_right2() == brotherNode){
                                //v' je uplne napravo
                                brotherNode.set_data2(brotherNode.get_data1());
                                brotherNode.set_value2(brotherNode.get_value1());
                                brotherNode.set_data1(alterNode.get_parent().get_data2());
                                brotherNode.set_value1(alterNode.get_parent().get_value2());
                                brotherNode.set_isThreeNode(true);
                                alterNode.get_parent().set_data2(null);
                                alterNode.get_parent().set_value2(null);
                                alterNode.get_parent().set_isThreeNode(false);
                                alterNode.get_parent().set_right1(brotherNode);
                                alterNode.get_parent().set_left2(null);
                                alterNode.get_parent().set_right2(null);
                                if (!isLeaf(alterNode)){
                                    //upravy referencii ak nie su listy
                                    brotherNode.set_right2(brotherNode.get_right1());
                                    brotherNode.set_left2(brotherNode.get_left1());
                                    brotherNode.set_right1(brotherNode.get_left1());
                                    if (alterNode.get_left1() != null){
                                        brotherNode.set_left1(alterNode.get_left1());
                                    }else {
                                        brotherNode.set_left1(alterNode.get_right1());
                                    }
                                    if (brotherNode.get_left1() != null){
                                        brotherNode.get_left1().set_parent(brotherNode);
                                    }
                                }
                                return true;
                            }else {
                                //v' je v strede
                                brotherNode.set_data2(alterNode.get_parent().get_data2());
                                brotherNode.set_value2(alterNode.get_parent().get_value2());
                                brotherNode.set_isThreeNode(true);
                                alterNode.get_parent().set_data2(null);
                                alterNode.get_parent().set_value2(null);
                                alterNode.get_parent().set_isThreeNode(false);
                                alterNode.get_parent().set_left2(null);
                                alterNode.get_parent().set_right2(null);
                                if (!isLeaf(alterNode)){
                                    //upravy referencii ak nie su listy
                                    brotherNode.set_left2(brotherNode.get_right1());
                                    if (alterNode.get_left1() != null){
                                        brotherNode.set_right2(alterNode.get_left1());
                                    }else {
                                        brotherNode.set_right2(alterNode.get_right1());
                                    }
                                    if (brotherNode.get_right2() != null){
                                        brotherNode.get_right2().set_parent(brotherNode);
                                    }
                                }
                                return true;
                            }
                        }else {
                            //v je uplne nalavo
                            brotherNode.set_data2(brotherNode.get_data1());
                            brotherNode.set_value2(brotherNode.get_value1());
                            brotherNode.set_data1(alterNode.get_parent().get_data1());
                            brotherNode.set_value1(alterNode.get_parent().get_value1());
                            brotherNode.set_isThreeNode(true);
                            alterNode.get_parent().set_data1(alterNode.get_parent().get_data2());
                            alterNode.get_parent().set_value1(alterNode.get_parent().get_value2());
                            alterNode.get_parent().set_data2(null);
                            alterNode.get_parent().set_value2(null);
                            alterNode.get_parent().set_isThreeNode(false);
                            alterNode.get_parent().set_left1(brotherNode);
                            alterNode.get_parent().set_right1(alterNode.get_parent().get_right2());
                            alterNode.get_parent().set_left2(null);
                            alterNode.get_parent().set_right2(null);
                            if (!isLeaf(alterNode)){
                                //upravy referencii ak nie su listy
                                brotherNode.set_right2(brotherNode.get_right1());
                                brotherNode.set_left2(brotherNode.get_left1());
                                brotherNode.set_right1(brotherNode.get_left1());
                                if (alterNode.get_left1() != null){
                                    brotherNode.set_left1(alterNode.get_left1());
                                }else {
                                    brotherNode.set_left1(alterNode.get_right1());
                                }
                                if (brotherNode.get_left1() != null){
                                    brotherNode.get_left1().set_parent(brotherNode);
                                }
                            }
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    public BST23Node findBrother(BST23Node node){
        if (node.get_parent().get_left1() == node){
            //node pre ktoreho hladam brata je uplne lavym synom jeho otca
            if (node.get_parent().get_right1() != null){
                return node.get_parent().get_right1();
            }
        }else if(node.get_parent().get_right1() == node){
            //node pre ktoreho hladam brata je pravym synom pre prvy prvok v node jeho otca
            if (node.get_parent().get_right2() == null){
                //otec ma len dvoch synov
                return node.get_parent().get_left1();
            }else {
                //otec ma troch synov cize mam na vyber dvoch bratov
                //uprednostnujem 3 vrchol
                if (node.get_parent().get_left1().isThreeNode()){
                    return node.get_parent().get_left1();
                }else{
                    return node.get_parent().get_right2();
                }
            }
        }else if(node.get_parent().get_right2() != null && node.get_parent().get_right2() == node){
            //node pre ktoreho hladam brata je pravym synom pre druhy prvok v node jeho otca
            if (node.get_parent().get_left2() != null){
                return node.get_parent().get_left2();
            }
        }
        return null;
    }

    public BST23Node findInOrderNode(BST23Node node, BST23Node nodeData){
        if (isLeaf(node)){
            return node;
        }
        if (nodeData.get_data1().compareTo(node.get_data1()) == 0){
            //data ktore mazem su nalavo
            BST23Node temp = node.get_right1();
            while (!isLeaf(temp)){
                temp = temp.get_left1();
            }
            return temp;
        }else if(nodeData.get_data1().compareTo(node.get_data2()) == 0){
            //data ktore mazem su napravo
            BST23Node temp = node.get_right2();
            while (!isLeaf(temp)){
                temp = temp.get_left1();
            }
            return temp;
        }
        return null;
    }

    public boolean isLeaf(BST23Node node){
        if (node.get_left1() == null && node.get_right1() == null && node.get_right2() == null){
            return true;
        }
        return false;
    }

    //taktiez robene podla prednaskoveho pseudkokodu v kombinacii s poskytnutou strankou z elearningu
    public boolean insert(BST23Node pNode){
        if (_root == null){
            _root = pNode;
            return true;
        }else if(find(pNode) == null) {
            BST23Node leaf = findLeafForInsert(pNode);
            if (leaf != null){
                if (!leaf.isThreeNode()){
                    //vkladanie pokial ma list len jeden kluc(data1)
                    leaf.set_isThreeNode(true);
                    if (leaf.get_data1().compareTo(pNode.get_data1()) > 0){
                        leaf.set_data2(pNode.get_data1());
                        leaf.set_value2(pNode.get_value1());
                        return true;
                    }else if(leaf.get_data1().compareTo(pNode.get_data1()) < 0){
                        leaf.set_data2(leaf.get_data1());
                        leaf.set_value2(leaf.get_value1());
                        leaf.set_data1(pNode.get_data1());
                        leaf.set_value1(pNode.get_value1());
                        return true;
                    }
                }else {
                    //list je trojvrchol
                    while (leaf != null){
                        T min = getMin(leaf, pNode);
                        V minValue= getMinValue(leaf, pNode);
                        T max = getMax(leaf, pNode);
                        V maxValue = getMaxValue(leaf, pNode);
                        T middle = getMiddle(leaf, pNode);
                        V middleValue = getMiddleValue(leaf, pNode);
                        if (leaf == _root){
                            //ked je node korenom
                            BST23Node newRoot = new BST23Node(middle, middleValue);
                            BST23Node newRightSon = new BST23Node(max, maxValue);

                            newRoot.set_left1(leaf);
                            newRoot.set_right1(newRightSon);

                            newRightSon.set_left1(leaf.get_left2());
                            newRightSon.set_right1(leaf.get_right2());
                            newRightSon.set_parent(newRoot);

                            leaf.set_parent(newRoot);
                            leaf.set_isThreeNode(false);
                            leaf.set_data2(null);
                            leaf.set_value2(null);
                            leaf.set_data1(min);
                            leaf.set_value1(minValue);
                            if (leaf.get_right2() != null && leaf.get_left2() != null){
                                leaf.get_right2().set_parent(newRightSon);
                                leaf.get_left2().set_parent(newRightSon);
                            }
                            leaf.set_left2(null);
                            leaf.set_right2(null);

                            _root = newRoot;
                            return true;
                        }else {
                            //node nie je korenom
                            if (!leaf.get_parent().isThreeNode()){
                                //pokial je otec dvojvrchol
                                if (leaf.get_parent().get_left1() == leaf){
                                    //ak je lavy potomok otca
                                    BST23Node newNode = new BST23Node(max, maxValue);
                                    newNode.set_parent(leaf.get_parent());
                                    newNode.set_left1(leaf.get_left2());
                                    newNode.set_right1(leaf.get_right2());
                                    if (leaf.get_left2() != null && leaf.get_right2() != null){
                                        leaf.get_left2().set_parent(newNode);
                                        leaf.get_right2().set_parent(newNode);
                                    }
                                    leaf.set_left2(null);
                                    leaf.set_right2(null);

                                    leaf.get_parent().set_data2(leaf.get_parent().get_data1());
                                    leaf.get_parent().set_value2(leaf.get_parent().get_value1());
                                    leaf.get_parent().set_data1(middle);
                                    leaf.get_parent().set_value1(middleValue);
                                    leaf.get_parent().set_isThreeNode(true);
                                    leaf.get_parent().set_right2(leaf.get_parent().get_right1());
                                    leaf.get_parent().set_right1(newNode);
                                    leaf.get_parent().set_left2(newNode);

                                    leaf.set_data2(null);
                                    leaf.set_value2(null);
                                    leaf.set_data1(min);
                                    leaf.set_value1(minValue);
                                    leaf.set_isThreeNode(false);
                                    return true;
                                }else{
                                    //ak je pravy potomok
                                    BST23Node newNode = new BST23Node(min, minValue);
                                    newNode.set_parent(leaf.get_parent());
                                    newNode.set_left1(leaf.get_left1());
                                    newNode.set_right1(leaf.get_right1());
                                    if (leaf.get_left1() != null && leaf.get_right1() != null){
                                        leaf.get_left1().set_parent(newNode);
                                        leaf.get_right1().set_parent(newNode);
                                    }

                                    leaf.get_parent().set_data2(middle);
                                    leaf.get_parent().set_value2(middleValue);
                                    leaf.get_parent().set_isThreeNode(true);
                                    leaf.get_parent().set_right2(leaf.get_parent().get_right1());
                                    leaf.get_parent().set_right1(newNode);
                                    leaf.get_parent().set_left2(newNode);
                                    leaf.set_left1(leaf.get_left2());
                                    leaf.set_right1(leaf.get_right2());
                                    leaf.set_left2(null);
                                    leaf.set_right2(null);

                                    leaf.set_data2(null);
                                    leaf.set_value2(null);
                                    leaf.set_data1(max);
                                    leaf.set_value1(maxValue);
                                    leaf.set_isThreeNode(false);
                                    return true;
                                }
                            }else {
                                //pokial je otec trojvrchol(doslo by k preteceniu)
                                if(leaf.get_parent().get_right2() == leaf){
                                    //leaf je pravy potomok
                                    BST23Node newNode = new BST23Node(min, minValue);
                                    newNode.set_parent(leaf.get_parent());
                                    newNode.set_left1(leaf.get_left1());
                                    newNode.set_right1(leaf.get_right1());
                                    if (leaf.get_left1() != null && leaf.get_right1() != null){
                                        leaf.get_left1().set_parent(newNode);
                                        leaf.get_right1().set_parent(newNode);
                                    }

                                    leaf.get_parent().set_left2(newNode);
                                    leaf.set_left1(leaf.get_left2());
                                    leaf.set_right1(leaf.get_right2());
                                    leaf.set_left2(null);
                                    leaf.set_right2(null);

                                    leaf.set_data2(null);
                                    leaf.set_value2(null);
                                    leaf.set_data1(max);
                                    leaf.set_value1(maxValue);
                                    leaf.set_isThreeNode(false);

                                    leaf = leaf.get_parent();
                                    pNode.set_data1(middle);
                                    pNode.set_value1(middleValue);
                                }else if(leaf.get_parent().get_right1() == leaf){
                                    //leaf je v strede
                                    BST23Node newNode = new BST23Node(max, maxValue);
                                    newNode.set_parent(leaf.get_parent());
                                    newNode.set_left1(leaf.get_left2());
                                    newNode.set_right1(leaf.get_right2());
                                    if (leaf.get_left2() != null && leaf.get_right2() != null){
                                        leaf.get_left2().set_parent(newNode);
                                        leaf.get_right2().set_parent(newNode);
                                    }
                                    leaf.set_left2(null);
                                    leaf.set_right2(null);

                                    leaf.get_parent().set_left2(newNode);
                                    leaf.set_data2(null);
                                    leaf.set_value2(null);
                                    leaf.set_data1(min);
                                    leaf.set_value1(minValue);
                                    leaf.set_isThreeNode(false);

                                    leaf = leaf.get_parent();
                                    pNode.set_data1(middle);
                                    pNode.set_value1(middleValue);
                                }else {
                                    //leaf je lavy potomok
                                    BST23Node newNode = new BST23Node(max, maxValue);
                                    newNode.set_parent(leaf.get_parent());
                                    newNode.set_left1(leaf.get_left2());
                                    newNode.set_right1(leaf.get_right2());
                                    if (leaf.get_left2() != null && leaf.get_right2() != null){
                                        leaf.get_left2().set_parent(newNode);
                                        leaf.get_right2().set_parent(newNode);
                                    }
                                    leaf.set_left2(null);
                                    leaf.set_right2(null);

                                    leaf.get_parent().set_right1(newNode);
                                    leaf.set_data2(null);
                                    leaf.set_value2(null);
                                    leaf.set_data1(min);
                                    leaf.set_value1(minValue);
                                    leaf.set_isThreeNode(false);

                                    leaf = leaf.get_parent();
                                    pNode.set_data1(middle);
                                    pNode.set_value1(middleValue);
                                }
                            }
                        }
                    }
                }
            }
        }
        return false;
    }

    private boolean areNodesEqual(BST23Node node1, BST23Node node2){
        if (node1.get_left1() != node2.get_left1()){
            return false;
        }
        if (node1.get_right1() != node2.get_right1()){
            return false;
        }
        if (node1.get_left2() != node2.get_left2()){
            return false;
        }
        if (node1.get_right2() != node2.get_right2()){
            return false;
        }
        if (node1.get_parent() != node2.get_parent()){
            return false;
        }
        if (node1.isThreeNode() != node2.isThreeNode()){
            return false;
        }
        if ((node1.get_data1() != null) && (node2.get_data1() != null)){
            if (node1.get_data1().compareTo(node2.get_data1()) != 0){
                return false;
            }
        }else {
            return false;
        }
        if ((node1.get_data2() != null) && (node2.get_data2() != null)){
            if (node1.get_data2().compareTo(node2.get_data2()) != 0){
                return false;
            }
        }else {
            if (node1.get_data2() != null){
                return false;
            }else if (node2.get_data2() != null){
                return false;
            }
        }
        return true;
    }

    private T getMin(BST23Node threeNode, BST23Node addedNode){
        T min;
        if (threeNode.get_data1().compareTo(threeNode.get_data2()) > 0){
            //overit spravnost pretypovania
            min = (T) threeNode.get_data1();
        }else {
            min = (T) threeNode.get_data2();
        }
        if (addedNode.get_data1().compareTo(min) > 0){
            min = (T) addedNode.get_data1();
        }
        return min;
    }

    private V getMinValue(BST23Node threeNode, BST23Node addedNode){
        T minKey;
        V min;
        if (threeNode.get_data1().compareTo(threeNode.get_data2()) > 0){
            //overit spravnost pretypovania
            min = (V) threeNode.get_value1();
            minKey = (T) threeNode.get_data1();
        }else {
            min = (V) threeNode.get_value2();
            minKey = (T) threeNode.get_data2();
        }
        if (addedNode.get_data1().compareTo(minKey) > 0){
            min = (V) addedNode.get_value1();
        }
        return min;
    }

    private T getMax(BST23Node threeNode, BST23Node addedNode){
        T max;
        if (threeNode.get_data1().compareTo(threeNode.get_data2()) < 0){
            //overit spravnost pretypovania
            max = (T) threeNode.get_data1();
        }else {
            max = (T) threeNode.get_data2();
        }
        if (addedNode.get_data1().compareTo(max) < 0){
            max = (T) addedNode.get_data1();
        }
        return max;
    }
    private V getMaxValue(BST23Node threeNode, BST23Node addedNode){
        V max;
        T maxKey;
        if (threeNode.get_data1().compareTo(threeNode.get_data2()) < 0){
            //overit spravnost pretypovania
            max = (V) threeNode.get_value1();
            maxKey = (T) threeNode.get_data1();
        }else {
            max = (V) threeNode.get_value2();
            maxKey = (T) threeNode.get_data2();
        }
        if (addedNode.get_data1().compareTo(maxKey) < 0){
            max = (V) addedNode.get_value1();
        }
        return max;
    }

    private T getMiddle(BST23Node threeNode, BST23Node addedNode){
        T smaller;
        T bigger;
        if (threeNode.get_data1().compareTo(threeNode.get_data2()) > 0){
            //overit spravnost pretypovania
            smaller = (T) threeNode.get_data1();
            bigger = (T) threeNode.get_data2();
        }else {
            smaller = (T) threeNode.get_data2();
            bigger = (T) threeNode.get_data1();
        }
        if (addedNode.get_data1().compareTo(smaller) < 0){
            if(addedNode.get_data1().compareTo(bigger) > 0){
                return (T) addedNode.get_data1();
            }else {
                return bigger;
            }
        }else {
            return smaller;
        }
    }

    private V getMiddleValue(BST23Node threeNode, BST23Node addedNode){
        V smaller;
        T smallerKey;
        V bigger;
        T biggerKey;
        if (threeNode.get_data1().compareTo(threeNode.get_data2()) > 0){
            //overit spravnost pretypovania
            smaller = (V) threeNode.get_value1();
            smallerKey = (T) threeNode.get_data1();
            bigger = (V) threeNode.get_value2();
            biggerKey = (T) threeNode.get_data2();
        }else {
            smaller = (V) threeNode.get_value2();
            smallerKey = (T) threeNode.get_data2();
            bigger = (V) threeNode.get_value1();
            biggerKey = (T) threeNode.get_data1();
        }
        if (addedNode.get_data1().compareTo(smallerKey) < 0){
            if(addedNode.get_data1().compareTo(biggerKey) > 0){
                return (V) addedNode.get_value1();
            }else {
                return bigger;
            }
        }else {
            return smaller;
        }
    }

    private BST23Node findLeafForInsert(BST23Node pNode){
        if (_root != null){
            //if (_root.get_left() == null && _root.get_right() == null){
            if (_root.get_left1() == null &&
                    _root.get_right1() == null &&
                    _root.get_right2() == null){
                //nema synov
                //overovanie ci 2 vrchol alebo 3 sa bude robit az v insert
                return _root;
            }else {
                BST23Node prev = null;
                BST23Node temp = _root;
                while (temp != null){
                    //if (temp.get_left() == null && temp.get_right() == null){
                    if (temp.get_left1() == null &&
                            temp.get_right1() == null &&
                            temp.get_right2() == null){
                        return temp;
                    }else {
                        if (temp.isThreeNode()){
                            //v pnode je zase data len data1
                            if(temp.get_data1().compareTo(pNode.get_data1()) < 0){
                                prev = temp;
                                //temp = prev.get_left();
                                temp = prev.get_left1();
                            }else if(temp.get_data2().compareTo(pNode.get_data1()) > 0){
                                prev = temp;
                                //temp = prev.get_right();
                                temp = prev.get_right2();
                            }else if ((temp.get_data1().compareTo(pNode.get_data1()) > 0) &&
                                    (temp.get_data2().compareTo(pNode.get_data1()) < 0)){
                                prev = temp;
                                //temp = prev.get_middle();
                                temp = prev.get_right1();
                            }
                        }else {
                            if (temp.get_data1().compareTo(pNode.get_data1()) < 0){
                                prev = temp;
                                //temp = prev.get_left();
                                temp = prev.get_left1();
                            }else if (temp.get_data1().compareTo(pNode.get_data1()) > 0){
                                prev = temp;
                                //temp = prev.get_right();
                                temp = prev.get_right1();
                            }
                        }
                    }
                }
            }
        }
        return null;
    }

    //podla kodu na find pre klasicky BVS na internete
    public BST23Node find(BST23Node pNode){
        //pomocne vytvoreny pNode kty je posielany ako parameter bude mat hladany kluc v data1
        if (_root == null ||
                (pNode.get_data1().compareTo(_root.get_data1()) == 0) ||
                ((_root.get_data2() != null) && (pNode.get_data1().compareTo(_root.get_data2()) == 0))){
            return _root;
        }else {
            BST23Node prev = null;
            BST23Node temp = _root;
            while (temp != null){
                if (temp.isThreeNode()){
                    if (temp.get_data1().compareTo(pNode.get_data1()) < 0){
                        //hladany kluc je mensi ako lavy vrchol(teda data1)
                        prev = temp;
                        temp = prev.get_left1();
                    }else if (temp.get_data2().compareTo(pNode.get_data1()) > 0){
                        //hladany kluc je vacsi ako pravy vrchol
                        prev = temp;
                        temp = prev.get_right2();
                    }else if ((temp.get_data1().compareTo(pNode.get_data1()) > 0) &&
                            (temp.get_data2().compareTo(pNode.get_data1()) < 0)){
                        //hladany kluc je medzi pravym a lavym klucom
                        prev = temp;
                        temp = prev.get_right1();
                    }else if ((temp.get_data1().compareTo(pNode.get_data1()) == 0) ||
                            (temp.get_data2().compareTo(pNode.get_data1()) == 0)){
                        //hladany kluc je jeden z klucov dvoch vrcholov
                        return temp;
                    }
                }else {
                    if (temp.get_data1().compareTo(pNode.get_data1()) > 0){
                        //ak pnode data1 je vacsie ako tempdata
                        prev = temp;
                        temp = prev.get_right1();
                    }else if(temp.get_data1().compareTo(pNode.get_data1()) < 0){
                        //ak pnode data1 je mensie ako tempdata
                        prev = temp;
                        temp = prev.get_left1();
                    }
                    if (temp != null){
                        if (temp.get_data1().compareTo(pNode.get_data1()) == 0){
                            return temp;
                        }
                    }
                }
            }
            return null;
        }
    }

    //ziadna predloha(robene intuitivne)
    public ArrayList<BST23Node> intervalSearch(BST23Node minNode, BST23Node maxNode){
        ArrayList<BST23Node> listOfFoundNodes = new ArrayList<>();
        BST23Node prev = null;
        T prevKey = null;
        BST23Node temp = _root;
        if (temp == null){
            return listOfFoundNodes;
        }
        while (temp != null){
            if (temp.isThreeNode()){
                if (temp.get_data1().compareTo(minNode.get_data1()) < 0){
                    //hladany kluc je mensi ako lavy vrchol(teda data1)
                    prev = temp;
                    prevKey = (T) prev.get_data1();
                    temp = prev.get_left1();
                }else if (temp.get_data2().compareTo(minNode.get_data1()) > 0){
                    //hladany kluc je vacsi ako pravy vrchol
                    prev = temp;
                    prevKey = (T) prev.get_data2();
                    temp = prev.get_right2();
                }else if ((temp.get_data1().compareTo(minNode.get_data1()) > 0) &&
                        (temp.get_data2().compareTo(minNode.get_data1()) < 0)){
                    //hladany kluc je medzi pravym a lavym klucom
                    prev = temp;
                    prevKey = (T) prev.get_data1();
                    temp = prev.get_right1();
                }else if(temp.get_data1().compareTo(minNode.get_data1()) == 0){
                    //najdeny minimalny node v datach 1
                    prev = temp;
                    prevKey = (T) prev.get_data1();
                    break;
                }else if (temp.get_data2().compareTo(minNode.get_data1()) == 0){
                    //najdeny minimalny node v datach 2
                    prev = temp;
                    prevKey = (T) prev.get_data2();
                    break;
                }
            }else {
                if (temp.get_data1().compareTo(minNode.get_data1()) > 0){
                    //ak min data1 je vacsie ako tempdata
                    prev = temp;
                    prevKey = (T) prev.get_data1();
                    temp = prev.get_right1();
                }else if(temp.get_data1().compareTo(minNode.get_data1()) < 0){
                    //ak min data1 je mensie ako tempdata
                    prev = temp;
                    prevKey = (T) prev.get_data1();
                    temp = prev.get_left1();
                }
                if (temp != null){
                    if (temp.get_data1().compareTo(minNode.get_data1()) == 0){
                        //najdeny minimalny node
                        prev = temp;
                        prevKey = (T) prev.get_data1();
                        break;
                    }
                }
            }
        }
        if (belongsToInterval(minNode, maxNode, prevKey)){
            //kluc patri do intervalu
            if (prev.get_data1().compareTo(prevKey) == 0){
                BST23Node newNode = new BST23Node(prev.get_data1(), prev.get_value1());
                listOfFoundNodes.add(newNode);
            }else {
                BST23Node newNode = new BST23Node(prev.get_data2(), prev.get_value2());
                listOfFoundNodes.add(newNode);
            }

        }
        NodeAndKey inOrderNode = findInOrderIntervalSearch(prev, prevKey);
        while (inOrderNode != null){
            if (belongsToInterval(minNode, maxNode, (T) inOrderNode.getKey())){
                if (inOrderNode.getNode().isThreeNode()){
                    if (inOrderNode.getNode().get_data1().compareTo((T) inOrderNode.getKey()) == 0){
                        //prve data
                        BST23Node newNode =
                                new BST23Node(inOrderNode.getNode().get_data1(), inOrderNode.getNode().get_value1());
                        listOfFoundNodes.add(newNode);
                        inOrderNode = findInOrderIntervalSearch(
                                inOrderNode.getNode(),
                                (T) inOrderNode.getNode().get_data1());
                    }else if (inOrderNode.getNode().get_data2().compareTo((T) inOrderNode.getKey()) == 0){
                        //druhe data
                        BST23Node newNode =
                                new BST23Node(inOrderNode.getNode().get_data2(), inOrderNode.getNode().get_value2());
                        listOfFoundNodes.add(newNode);
                        inOrderNode = findInOrderIntervalSearch(
                                inOrderNode.getNode(),
                                (T) inOrderNode.getNode().get_data2());
                    }
                }else {
                    if (inOrderNode.getNode().get_data1().compareTo((T) inOrderNode.getKey()) == 0){
                        BST23Node newNode =
                                new BST23Node(inOrderNode.getNode().get_data1(), inOrderNode.getNode().get_value1());
                        listOfFoundNodes.add(newNode);
                        inOrderNode = findInOrderIntervalSearch(
                                inOrderNode.getNode(),
                                (T) inOrderNode.getNode().get_data1());
                    }
                }
            }else {
                break;
            }
        }
        return listOfFoundNodes;
    }

    private NodeAndKey findInOrderIntervalSearch(BST23Node node, T key){
        if (!node.isThreeNode()){
            if (node.get_right1() != null){
                BST23Node temp = node.get_right1();
                while (!isLeaf(temp)){
                    temp = temp.get_left1();
                }
                NodeAndKey nodeAndKey = new NodeAndKey(temp, temp.get_data1());
                return nodeAndKey;
            }else {
                //nema syna tak pojde po parentoch
                BST23Node temp = node;
                while (temp != null){
                    if (temp.get_parent() != null){
                        if (temp.get_parent().isThreeNode()){
                            if (key.compareTo((T) temp.get_parent().get_data1()) > 0){
                                NodeAndKey nodeAndKey = new NodeAndKey(temp.get_parent(), temp.get_parent().get_data1());
                                return nodeAndKey;
                            }
                            if ((key.compareTo((T) temp.get_parent().get_data1()) < 0) &&
                                    (key.compareTo((T) temp.get_parent().get_data2()) > 0)){
                                NodeAndKey nodeAndKey = new NodeAndKey(temp.get_parent(), temp.get_parent().get_data2());
                                return nodeAndKey;
                            }
                        }else {
                            if (key.compareTo((T) temp.get_parent().get_data1()) > 0){
                                NodeAndKey nodeAndKey = new NodeAndKey(temp.get_parent(), temp.get_parent().get_data1());
                                return nodeAndKey;
                            }
                        }
                    }else {
                        if (!temp.isThreeNode()){
                            if (key.compareTo((T) temp.get_data1()) > 0){
                                NodeAndKey nodeAndKey = new NodeAndKey(temp, temp.get_data1());
                                return nodeAndKey;
                            }else {
                                return null;
                            }
                        }else {
                            if (key.compareTo((T) temp.get_data1()) > 0){
                                NodeAndKey nodeAndKey = new NodeAndKey(temp, temp.get_data1());
                                return nodeAndKey;
                            }else if ((key.compareTo((T) temp.get_data1()) < 0) &&
                                    (key.compareTo((T) temp.get_data2()) > 0)){
                                NodeAndKey nodeAndKey = new NodeAndKey(temp, temp.get_data2());
                                return nodeAndKey;
                            }else {
                                return null;
                            }
                        }
                    }
                    temp = temp.get_parent();
                }
            }
        }else {
            //node je 3 vrchol
            if (key.compareTo((T) node.get_data1()) == 0){
                if (node.get_right1() == null){
                    //nema stredneho syna tak nema kam vliezt tak vracia data 2
                    NodeAndKey nodeAndKey = new NodeAndKey(node, node.get_data2());
                    return nodeAndKey;
                }else {
                    //ma stredneho syna tak vlezie do neho
                    BST23Node temp = node.get_right1();
                    while (!isLeaf(temp)){
                        //lez stale dolava az kym nenarazis na list
                        temp = temp.get_left1();
                    }
                    NodeAndKey nodeAndKey = new NodeAndKey(temp, temp.get_data1());
                    return nodeAndKey;
                }
            }else if(key.compareTo((T) node.get_data2()) == 0){
                //hladame nasledovnika 2. kluca
                if (node.get_right2() != null){
                    //ma praveho syna tak vlezie do neho
                    BST23Node temp = node.get_right2();
                    while (!isLeaf(temp)){
                        //lez stale dolava az kym nenarazis na list
                        temp = temp.get_left1();
                    }
                    NodeAndKey nodeAndKey = new NodeAndKey(temp, temp.get_data1());
                    return nodeAndKey;
                }else {
                    BST23Node temp = node;
                    while (temp != null){
                        if (temp.get_parent() != null){
                            if (temp.get_parent().isThreeNode()){
                                if (key.compareTo((T) temp.get_parent().get_data1()) > 0){
                                    NodeAndKey nodeAndKey = new NodeAndKey(temp.get_parent(), temp.get_parent().get_data1());
                                    return nodeAndKey;
                                }
                                if ((key.compareTo((T) temp.get_parent().get_data1()) < 0) &&
                                        (key.compareTo((T) temp.get_parent().get_data2()) > 0)){
                                    NodeAndKey nodeAndKey = new NodeAndKey(temp.get_parent(), temp.get_parent().get_data2());
                                    return nodeAndKey;
                                }
                            }else {
                                if (key.compareTo((T) temp.get_parent().get_data1()) > 0){
                                    NodeAndKey nodeAndKey = new NodeAndKey(temp.get_parent(), temp.get_parent().get_data1());
                                    return nodeAndKey;
                                }
                            }
                        }else {
                            if (!temp.isThreeNode()){
                                if (key.compareTo((T) temp.get_data1()) > 0){
                                    NodeAndKey nodeAndKey = new NodeAndKey(temp, temp.get_data1());
                                    return nodeAndKey;
                                }else {
                                    return null;
                                }
                            }else {
                                if (key.compareTo((T) temp.get_data1()) > 0){
                                    NodeAndKey nodeAndKey = new NodeAndKey(temp, temp.get_data1());
                                    return nodeAndKey;
                                }else if ((key.compareTo((T) temp.get_data1()) < 0) &&
                                        (key.compareTo((T) temp.get_data2()) > 0)){
                                    NodeAndKey nodeAndKey = new NodeAndKey(temp, temp.get_data2());
                                    return nodeAndKey;
                                }else {
                                    return null;
                                }
                            }
                        }
                        temp = temp.get_parent();
                    }
                }
            }
        }
        return null;
    }

    public ArrayList<BST23Node> inOrder(){
        ArrayList<BST23Node> listOfFoundNodes = new ArrayList<>();
        BST23Node temp = _root;
        if (_root == null){
            return listOfFoundNodes;
        }
        while (!isLeaf(temp)){
            temp = temp.get_left1();
        }
        BST23Node newNode = new BST23Node(temp.get_data1(), temp.get_value1());
        listOfFoundNodes.add(newNode);
        //pouziviame hladanie in order nasledovnika z intervaloveho hladania
        NodeAndKey inOrderNode = findInOrderIntervalSearch(temp, (T) temp.get_data1());
        while (inOrderNode != null){
            if (inOrderNode.getNode().isThreeNode()){
                if (inOrderNode.getNode().get_data1().compareTo((T) inOrderNode.getKey()) == 0){
                    //prve data
                    BST23Node new_Node =
                            new BST23Node(inOrderNode.getNode().get_data1(), inOrderNode.getNode().get_value1());
                    listOfFoundNodes.add(new_Node);
                    inOrderNode = findInOrderIntervalSearch(
                            inOrderNode.getNode(),
                            (T) inOrderNode.getNode().get_data1());
                }else if (inOrderNode.getNode().get_data2().compareTo((T) inOrderNode.getKey()) == 0){
                    //druhe data
                    BST23Node new_Node =
                            new BST23Node(inOrderNode.getNode().get_data2(), inOrderNode.getNode().get_value2());
                    listOfFoundNodes.add(new_Node);
                    inOrderNode = findInOrderIntervalSearch(
                            inOrderNode.getNode(),
                            (T) inOrderNode.getNode().get_data2());
                }
            }else {
                if (inOrderNode.getNode().get_data1().compareTo((T) inOrderNode.getKey()) == 0){
                    BST23Node new_Node =
                            new BST23Node(inOrderNode.getNode().get_data1(), inOrderNode.getNode().get_value1());
                    listOfFoundNodes.add(new_Node);
                    inOrderNode = findInOrderIntervalSearch(
                            inOrderNode.getNode(),
                            (T) inOrderNode.getNode().get_data1());
                }
            }
        }
        return listOfFoundNodes;
    }

    public NodeWithKey getFirst(){
        BST23Node temp = _root;
        if (_root == null){
            return null;
        }
        while (!isLeaf(temp)){
            temp = temp.get_left1();
        }
        return new NodeWithKey(temp, temp.get_data1());
    }

    public NodeWithKey getNext(BST23Node pNode, T key){
        NodeAndKey temp = findInOrderIntervalSearch(pNode, key);
        if (temp == null){
            return null;
        }else {
            NodeWithKey nodeWithKey = new NodeWithKey(temp.getNode(),temp.getKey());
            return nodeWithKey;
        }
    }

    private boolean belongsToInterval(BST23Node minNode, BST23Node maxNode, T key){
        if (key.compareTo((T) minNode.get_data1()) <= 0 &&
                key.compareTo((T) maxNode.get_data1()) >= 0){
            return true;
        }else
            return false;
    }

    public BST23Node<T,V> get_root() {
        return _root;
    }
}

class NodeAndKey<T extends  Comparable<T>, V>{
    private BST23Node<T,V> node;
    private T key;

    public NodeAndKey(BST23Node pNode, T pKey){
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
