package Structure;

import java.io.*;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.PriorityQueue;
import java.util.logging.Level;
import java.util.logging.Logger;

public class BST23<T extends  Comparable<T> & IData, V extends IData> {
    private static final int UNDEFINED = -1;
    private static final int HEADER_SIZE = 12;

    private RandomAccessFile fileOfRecords;
    private int numberOfBlocks;
    private int nextAddress;
    //front s prazdnymi adresami
    private PriorityQueue<Integer> invalidRecordsAdrIncr;
    private PriorityQueue<Integer> invalidRecordsAdrDecr;
    private String nameOfFileOfRecords;

    //private BST23Node<T,V> _root;
    private int _root;

    private Class<T> classTypeKey;
    private Class<T> classTypeValue;

    public BST23(String pFileName, Class pClassTypeKey, Class pClassTypeValue){
        nameOfFileOfRecords = pFileName;
        classTypeKey = pClassTypeKey;
        classTypeValue = pClassTypeValue;

        _root = UNDEFINED;

        //otvorenie suboru
        if (!(new File(pFileName)).exists()){
            setInitialHeader(pFileName);
        }else {
            try {
                fileOfRecords = new RandomAccessFile(pFileName, "rw");
                loadHeader();
            }catch (FileNotFoundException exception){
                Logger.getLogger(UnsortedFile.class.getName()).log(Level.SEVERE, null, exception);
            }
        }

        invalidRecordsAdrIncr = new PriorityQueue<>();
        invalidRecordsAdrDecr = new PriorityQueue<>(Comparator.reverseOrder());
        loadPriorityQueuesFromFile();
    }

    private boolean setInitialHeader(String pFileName){
        ByteArrayOutputStream hlpByteArrayOutputStream= new ByteArrayOutputStream();
        DataOutputStream hlpOutStream = new DataOutputStream(hlpByteArrayOutputStream);

        try{
            //v hlavicke pocet zaznamov v subore(do poctu rata aj prazdne miesta)
            // a dalsia adresa ak sa nevklada do prazdnych miest
            hlpOutStream.writeInt(0);
            hlpOutStream.writeInt(3*Integer.BYTES);
            hlpOutStream.writeInt(UNDEFINED);
        }catch (IOException e){
            return false;
        }

        //vytvorenie suboru a zapisanie inicializacnych udajov pre hlavicku
        try {
            fileOfRecords = new RandomAccessFile(pFileName, "rw");
            fileOfRecords.seek(0);
            fileOfRecords.write(hlpByteArrayOutputStream.toByteArray());
        } catch (IOException exception) {
            Logger.getLogger(UnsortedFile.class.getName()).log(Level.SEVERE, null, exception);
            return false;
        }

        numberOfBlocks = 0;
        nextAddress = HEADER_SIZE;
        return true;
    }

    private boolean loadHeader(){
        //nacitanie udajov pre hlavicku zo suboru do pola bytov
        byte[] arrayOfHeaderBytes = new byte[HEADER_SIZE];
        try {
            fileOfRecords.seek(0);
            fileOfRecords.read(arrayOfHeaderBytes);
        }catch (IOException exception){
            return false;
        }

        ByteArrayInputStream hlpByteArrayInputStream = new ByteArrayInputStream(arrayOfHeaderBytes);
        DataInputStream hlpInStream = new DataInputStream(hlpByteArrayInputStream);

        //priradenie hodnot hlavickovych dat
        try {
            numberOfBlocks = hlpInStream.readInt();
            nextAddress = hlpInStream.readInt();
            _root = hlpInStream.readInt();
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    private boolean saveHeader(){
        ByteArrayOutputStream hlpByteArrayOutputStream= new ByteArrayOutputStream();
        DataOutputStream hlpOutStream = new DataOutputStream(hlpByteArrayOutputStream);

        //vytvorenie pola bytov pre hlavicku
        try{
            hlpOutStream.writeInt(numberOfBlocks);
            hlpOutStream.writeInt(nextAddress);
            hlpOutStream.writeInt(_root);
        }catch (IOException e){
            return false;
        }

        //vytvorenie suboru a zapisanie udajov pre hlavicku
        try {
            fileOfRecords.seek(0);
            fileOfRecords.write(hlpByteArrayOutputStream.toByteArray());
        } catch (IOException exception) {
            Logger.getLogger(UnsortedFile.class.getName()).log(Level.SEVERE, null, exception);
            return false;
        }
        return true;
    }

    private boolean savePriorityQueuestoFile(){
        FileWriter csvWriter = null;
        Iterator incrIterator = invalidRecordsAdrIncr.iterator();
        Iterator decrIterator = invalidRecordsAdrDecr.iterator();
        try {
            csvWriter = new FileWriter(nameOfFileOfRecords + "incr.csv");

            while (incrIterator.hasNext()){
                csvWriter.append(""+ incrIterator.next());
                csvWriter.append("\n");
            }
            csvWriter.flush();
            csvWriter.close();

            csvWriter = new FileWriter(nameOfFileOfRecords + "decr.csv");
            while (decrIterator.hasNext()){
                csvWriter.append(""+ decrIterator.next());
                csvWriter.append("\n");
            }
            csvWriter.flush();
            csvWriter.close();
        }catch (IOException exception){
            return false;
        }
        return true;
    }

    private boolean loadPriorityQueuesFromFile(){
        try {
            BufferedReader incrReader = new BufferedReader(new FileReader(nameOfFileOfRecords + "incr.csv"));
            String row;
            while ((row = incrReader.readLine()) != null) {
                String[] data = row.split(",");
                if (data[0] != null){
                    invalidRecordsAdrIncr.add(Integer.parseInt(data[0]));
                }
            }
            incrReader.close();

            BufferedReader decrReader = new BufferedReader(new FileReader(nameOfFileOfRecords + "decr.csv"));
            while ((row = decrReader.readLine()) != null) {
                String[] data = row.split(",");
                if (data[0] != null){
                    invalidRecordsAdrDecr.add(Integer.parseInt(data[0]));
                }
            }
            decrReader.close();
        }catch (IOException exception){
            return false;
        }
        return true;
    }

    private BST23Node getNodeForAddress(int pAddress){
        if (pAddress<0){
            return null;
        }
        BST23Node node = new BST23Node(classTypeKey, classTypeValue);
        //nacitanie pola bytov zo suboru
        byte[] arrayOfDataBytes = new byte[node.getSize()];
        try {
            fileOfRecords.seek(pAddress);
            fileOfRecords.read(arrayOfDataBytes);
        }catch (IOException exception){
            return null;
        }
        //nacitanie hodnot pre node z pola bytov
        node.FromByteArray(arrayOfDataBytes);
        //kontrola platnosti nodu
        if (node.isValid()){
            return node;
        }else {
            return null;
        }
    }

    public boolean endWorkWithFile(){
        saveHeader();
        savePriorityQueuestoFile();
        try {
            fileOfRecords.close();
        }catch (IOException exception){
            return false;
        }
        return true;
    }

    private boolean invalidateNode(BST23Node pNode){
        pNode.setValid(false);
        pNode.set_left1(UNDEFINED);
        pNode.set_right1(UNDEFINED);
        pNode.set_left2(UNDEFINED);
        pNode.set_right2(UNDEFINED);
        pNode.set_data1(null);
        pNode.set_data2(null);
        pNode.set_value1(null);
        pNode.set_value2(null);
        pNode.set_isThreeNode(false);
        long lengthOfFile = 0;
        try {
            lengthOfFile = fileOfRecords.length();
        }catch (IOException exception){
            return false;
        }
        //ak je na konci suboru tak skratit subor
        if(pNode.get_address() == (lengthOfFile - pNode.getSize())){
            int numberOfEmptySpaces = 1;
            Integer unusedAddress = invalidRecordsAdrDecr.peek();
            while (unusedAddress != null && unusedAddress == (pNode.get_address() - pNode.getSize() * numberOfEmptySpaces)){
                invalidRecordsAdrIncr.remove(unusedAddress);
                invalidRecordsAdrDecr.poll();
                numberOfEmptySpaces++;
                unusedAddress = invalidRecordsAdrDecr.peek();
            }
            try {
                fileOfRecords.setLength(lengthOfFile - (numberOfEmptySpaces * pNode.getSize()));
            }catch (IOException exception){
                return false;
            }
            numberOfBlocks -= numberOfEmptySpaces;
            nextAddress = numberOfBlocks * pNode.getSize() + HEADER_SIZE;
            return true;
        }else {
            //pridat do prioritneho frontu nepouzitu adresu
            invalidRecordsAdrIncr.add(pNode.get_address());
            invalidRecordsAdrDecr.add(pNode.get_address());
            //ulozenie upravenych dat(upravena validita zaznamu)
            byte[] arrayOfDeletedBytes = pNode.ToByteArray();
            try {
                fileOfRecords.seek(pNode.get_address());
                fileOfRecords.write(arrayOfDeletedBytes);
            }catch (IOException exception){
                return false;
            }
            return true;
        }
    }

    private boolean setTreeEmpty(){
        try {
            fileOfRecords.setLength(HEADER_SIZE);
            nextAddress = HEADER_SIZE;
            numberOfBlocks = 0;
            _root = UNDEFINED;
            invalidRecordsAdrIncr.clear();
            invalidRecordsAdrDecr.clear();
            return true;
        }catch (IOException exception){
            return false;
        }
    }

    //robene podla prednaskoveho pseudkokodu v kombinacii s poskytnutou strankou z elearningu na vizualizaciu
    public boolean delete(BST23Node pNode){
        BST23Node deletedNode = find(pNode);
        if (deletedNode != null){
            if (deletedNode.get_address() == _root && isLeaf(deletedNode) && !deletedNode.isThreeNode()){
                //jediny prvok v strome tak sa zmaze referencia na root
                //deleteDataFromFile(_root,FIRST_DATA_IN_NODE);
                setTreeEmpty();
                //_root = null;
                return true;
            }
            if (deletedNode.isThreeNode() && isLeaf(deletedNode)){
                if (pNode.get_data1().compareTo(deletedNode.get_data1()) == 0){
                    deletedNode.set_data1(deletedNode.get_data2());
                    deletedNode.set_value1(deletedNode.get_value2());
                    deletedNode.set_data2(null);
                    deletedNode.set_value2(null);
                    deletedNode.set_isThreeNode(false);
                    insertToFileOnAddress(deletedNode,deletedNode.get_address());
                    return true;
                }else if (pNode.get_data1().compareTo(deletedNode.get_data2()) == 0){
                    deletedNode.set_data2(null);
                    deletedNode.set_value2(null);
                    deletedNode.set_isThreeNode(false);
                    insertToFileOnAddress(deletedNode,deletedNode.get_address());
                    return true;
                }
            }
            BST23Node<T,V> alterNode = findInOrderNode(deletedNode, pNode);
            if (alterNode != null && alterNode.isThreeNode()){
                //jeho nasledovnik je 3 vrchol
                if (pNode.get_data1().compareTo(deletedNode.get_data1()) == 0){
                    deletedNode.set_data1(alterNode.get_data1());
                    deletedNode.set_value1(alterNode.get_value1());
                    insertToFileOnAddress(deletedNode,deletedNode.get_address());
                    alterNode.set_data1(alterNode.get_data2());
                    alterNode.set_value1(alterNode.get_value2());
                    alterNode.set_data2(null);
                    alterNode.set_value2(null);
                    alterNode.set_isThreeNode(false);
                    insertToFileOnAddress(alterNode,alterNode.get_address());
                    return true;
                }else if (pNode.get_data1().compareTo(deletedNode.get_data2()) == 0){
                    deletedNode.set_data2(alterNode.get_data1());
                    deletedNode.set_value2(alterNode.get_value1());
                    insertToFileOnAddress(deletedNode,deletedNode.get_address());
                    alterNode.set_data1(alterNode.get_data2());
                    alterNode.set_value1(alterNode.get_value2());
                    alterNode.set_data2(null);
                    alterNode.set_value2(null);
                    alterNode.set_isThreeNode(false);
                    insertToFileOnAddress(alterNode,alterNode.get_address());
                    return true;
                }
            }
            //vymazanie prvku a tym vytvorenie prazdneho miesta v alter node
            if (pNode.get_data1().compareTo(deletedNode.get_data1()) == 0){
                deletedNode.set_data1(alterNode.get_data1());
                deletedNode.set_value1(alterNode.get_value1());
                insertToFileOnAddress(deletedNode,deletedNode.get_address());
            }else if (pNode.get_data1().compareTo(deletedNode.get_data2()) == 0){
                deletedNode.set_data2(alterNode.get_data1());
                deletedNode.set_value2(alterNode.get_value1());
                insertToFileOnAddress(deletedNode,deletedNode.get_address());
            }
            while (alterNode != null){
                if(alterNode.get_address() == _root){
                    if (alterNode.get_left1() == UNDEFINED && alterNode.get_right1() == UNDEFINED){
                        setTreeEmpty();
                        //_root = null;
                        return true;
                    }else if (alterNode.get_left1() != UNDEFINED && alterNode.get_right1() == UNDEFINED){
                        _root = alterNode.get_left1();
                        invalidateNode(alterNode);
                        BST23Node rootNode = getNodeForAddress(_root);
                        rootNode.set_parent(UNDEFINED);
                        insertToFileOnAddress(rootNode,rootNode.get_address());
                        return true;
                    }else if(alterNode.get_left1() == UNDEFINED && alterNode.get_right1() != UNDEFINED){
                        _root = alterNode.get_right1();
                        invalidateNode(alterNode);
                        BST23Node rootNode = getNodeForAddress(_root);
                        rootNode.set_parent(UNDEFINED);
                        insertToFileOnAddress(rootNode,rootNode.get_address());
                        return true;
                    }
                }
                BST23Node<T,V> brotherNode = findBrother(alterNode);
                if (brotherNode.isThreeNode()){
                    BST23Node<T,V> alterNodeParentNode = getNodeForAddress(alterNode.get_parent());
                    //Ko presuniem na prazdne miesto a z brata presuniem do otca
                    if (alterNodeParentNode.get_left1() == alterNode.get_address()){
                        //prazdne miesto je vlavo od otca takze sa vykonava lava rotacia
                        alterNode.set_data1(alterNodeParentNode.get_data1());
                        alterNode.set_value1(alterNodeParentNode.get_value1());
                        alterNodeParentNode.set_data1(brotherNode.get_data1());
                        alterNodeParentNode.set_value1(brotherNode.get_value1());

                        brotherNode.set_data1(brotherNode.get_data2());
                        brotherNode.set_value1(brotherNode.get_value2());
                        brotherNode.set_data2(null);
                        brotherNode.set_value2(null);
                        brotherNode.set_isThreeNode(false);

                        //nastavenie referencii
                        if ((alterNode.get_left1() != UNDEFINED || alterNode.get_right1() != UNDEFINED) &&
                                (brotherNode.get_left1() != UNDEFINED)) {
                            //ak maju v aj jeho brat synov tak uprav referecnie
                            alterNode.set_right1(brotherNode.get_left1());
                            BST23Node<T,V> brotherNodeLeftNode = getNodeForAddress(brotherNode.get_left1());
                            brotherNodeLeftNode.set_parent(alterNode.get_address());
                            brotherNode.set_left1(brotherNode.get_right1());
                            brotherNode.set_right1(brotherNode.get_right2());
                            brotherNode.set_right2(UNDEFINED);
                            brotherNode.set_left2(UNDEFINED);

                            insertToFileOnAddress(brotherNodeLeftNode,brotherNodeLeftNode.get_address());
                        }
                        insertToFileOnAddress(brotherNode,brotherNode.get_address());
                        insertToFileOnAddress(alterNode,alterNode.get_address());
                        insertToFileOnAddress(alterNodeParentNode,alterNodeParentNode.get_address());
                        return true;
                    }
                    if(alterNodeParentNode.get_right2() != UNDEFINED && alterNodeParentNode.get_right2() == alterNode.get_address()){
                        //prazdne miesto je napravo od otca takze sa vykonava prava rotacia
                        alterNode.set_data1(alterNodeParentNode.get_data2());
                        alterNode.set_value1(alterNodeParentNode.get_value2());
                        alterNodeParentNode.set_data2(brotherNode.get_data2());
                        alterNodeParentNode.set_value2(brotherNode.get_value2());

                        brotherNode.set_data2(null);
                        brotherNode.set_value2(null);
                        brotherNode.set_isThreeNode(false);

                        //nastavenie referencii
                        if ((alterNode.get_left1() != UNDEFINED || alterNode.get_right1() != UNDEFINED) &&
                                (brotherNode.get_left1() != UNDEFINED)) {
                            if (alterNode.get_left1() != UNDEFINED && alterNode.get_right1() == UNDEFINED){
                                alterNode.set_right1(alterNode.get_left1());
                                alterNode.set_left1(UNDEFINED);
                            }
                            //ak maju v aj jeho brat synov tak uprav referecnie
                            alterNode.set_left1(brotherNode.get_right2());
                            BST23Node<T,V> brotherNodeRight2Node = getNodeForAddress(brotherNode.get_right2());
                            brotherNodeRight2Node.set_parent(alterNode.get_address());
                            brotherNode.set_right2(UNDEFINED);
                            brotherNode.set_left2(UNDEFINED);

                            insertToFileOnAddress(brotherNodeRight2Node,brotherNodeRight2Node.get_address());
                        }
                        //update alternode, alternodeparentnode, brothernode
                        insertToFileOnAddress(alterNode,alterNode.get_address());
                        insertToFileOnAddress(alterNodeParentNode,alterNodeParentNode.get_address());
                        insertToFileOnAddress(brotherNode,brotherNode.get_address());
                        return true;
                    }
                    if (alterNodeParentNode.get_left1() == brotherNode.get_address()){
                        //brat je nalavo od otca, cize prazdne miesto je v strede takze sa vykonava prava rotacia
                        alterNode.set_data1(alterNodeParentNode.get_data1());
                        alterNode.set_value1(alterNodeParentNode.get_value1());
                        alterNodeParentNode.set_data1(brotherNode.get_data2());
                        alterNodeParentNode.set_value1(brotherNode.get_value2());

                        brotherNode.set_data2(null);
                        brotherNode.set_value2(null);
                        brotherNode.set_isThreeNode(false);

                        //nastavenie referencii
                        if ((alterNode.get_left1() != UNDEFINED || alterNode.get_right1() != UNDEFINED) &&
                                (brotherNode.get_left1() != UNDEFINED)){
                            //ak maju v aj jeho brat synov tak uprav referecnie
                            if (alterNode.get_left1() != UNDEFINED && alterNode.get_right1() == UNDEFINED){
                                alterNode.set_right1(alterNode.get_left1());
                                alterNode.set_left1(UNDEFINED);
                            }
                            alterNode.set_left1(brotherNode.get_right2());
                            BST23Node<T,V> brotherNodeRight2Node = getNodeForAddress(brotherNode.get_right2());
                            brotherNodeRight2Node.set_parent(alterNode.get_address());
                            brotherNode.set_right2(UNDEFINED);
                            brotherNode.set_left2(UNDEFINED);

                            insertToFileOnAddress(brotherNodeRight2Node,brotherNodeRight2Node.get_address());
                        }
                        insertToFileOnAddress(alterNode,alterNode.get_address());
                        insertToFileOnAddress(alterNodeParentNode,alterNodeParentNode.get_address());
                        insertToFileOnAddress(brotherNode,brotherNode.get_address());
                        return true;
                    }
                    if (alterNodeParentNode.get_right2() != UNDEFINED && alterNodeParentNode.get_right2() == brotherNode.get_address()){
                        //brat je napravo od otcovho druheho prvku, cize prazdne miesto je v strede takze sa vykonava lava rotacia
                        alterNode.set_data1(alterNodeParentNode.get_data2());
                        alterNode.set_value1(alterNodeParentNode.get_value2());
                        alterNodeParentNode.set_data2(brotherNode.get_data1());
                        alterNodeParentNode.set_value2(brotherNode.get_value1());

                        brotherNode.set_data1(brotherNode.get_data2());
                        brotherNode.set_value1(brotherNode.get_value2());
                        brotherNode.set_data2(null);
                        brotherNode.set_value2(null);
                        brotherNode.set_isThreeNode(false);

                        //nastavenie referencii
                        if ((alterNode.get_left1() != UNDEFINED || alterNode.get_right1() != UNDEFINED) &&
                                (brotherNode.get_left1() != UNDEFINED)){
                            //ak maju v aj jeho brat synov tak uprav referecnie
                            if (alterNode.get_right1() != UNDEFINED && alterNode.get_left1() == UNDEFINED){
                                alterNode.set_left1(alterNode.get_right1());
                                alterNode.set_right1(UNDEFINED);
                            }
                            alterNode.set_right1(brotherNode.get_left1());
                            BST23Node<T,V> brotherNodeLeft1Node = getNodeForAddress(brotherNode.get_left1());
                            brotherNodeLeft1Node.set_parent(alterNode.get_address());
                            brotherNode.set_left1(brotherNode.get_right1());
                            brotherNode.set_right1(brotherNode.get_right2());
                            brotherNode.set_right2(UNDEFINED);
                            brotherNode.set_left2(UNDEFINED);

                            insertToFileOnAddress(brotherNodeLeft1Node,brotherNodeLeft1Node.get_address());
                        }
                        insertToFileOnAddress(alterNode,alterNode.get_address());
                        insertToFileOnAddress(alterNodeParentNode,alterNodeParentNode.get_address());
                        insertToFileOnAddress(brotherNode,brotherNode.get_address());
                        return true;
                    }
                }else {
                    BST23Node<T,V> alterNodeParentNode = getNodeForAddress(alterNode.get_parent());
                    //brat je len dvojvrchol
                    if (!alterNodeParentNode.isThreeNode()) {
                        //otec je dvojvrchol
                        if (alterNodeParentNode.get_right1() == brotherNode.get_address()){
                            //v' je pravy syn
                            brotherNode.set_data2(brotherNode.get_data1());
                            brotherNode.set_value2(brotherNode.get_value1());
                            brotherNode.set_data1(alterNodeParentNode.get_data1());
                            brotherNode.set_value1(alterNodeParentNode.get_value1());
                            brotherNode.set_isThreeNode(true);
                            alterNode.set_data1(null);
                            alterNode.set_value1(null);
                            alterNodeParentNode.set_data1(null);
                            alterNodeParentNode.set_value1(null);
                            alterNodeParentNode.set_left1(brotherNode.get_address());
                            alterNodeParentNode.set_right1(UNDEFINED);

                            insertToFileOnAddress(alterNodeParentNode,alterNodeParentNode.get_address());
                            if (!isLeaf(alterNode)){
                                //upravy referencii ak nie su listy
                                brotherNode.set_right2(brotherNode.get_right1());
                                brotherNode.set_left2(brotherNode.get_left1());
                                brotherNode.set_right1(brotherNode.get_left1());
                                if (alterNode.get_left1() != UNDEFINED){
                                    brotherNode.set_left1(alterNode.get_left1());
                                }else {
                                    brotherNode.set_left1(alterNode.get_right1());
                                }
                                if (brotherNode.get_left1() != UNDEFINED){
                                    BST23Node<T,V> brotherNodeLeft1Node = getNodeForAddress(brotherNode.get_left1());
                                    brotherNodeLeft1Node.set_parent(brotherNode.get_address());
                                    insertToFileOnAddress(brotherNodeLeft1Node,brotherNodeLeft1Node.get_address());
                                }
                                if (getNodeForAddress(brotherNode.get_parent()).get_parent() == UNDEFINED){
                                    //prazdne miesto je v koreni
                                    invalidateNode(alterNode);
                                    invalidateNode(getNodeForAddress(_root));
                                    _root = brotherNode.get_address();
                                    brotherNode.set_parent(UNDEFINED);
                                    insertToFileOnAddress(brotherNode,brotherNode.get_address());
                                    return true;
                                }
                            }
                            insertToFileOnAddress(brotherNode,brotherNode.get_address());
                        }else {
                            //v' je lavy syn
                            brotherNode.set_data2(alterNodeParentNode.get_data1());
                            brotherNode.set_value2(alterNodeParentNode.get_value1());
                            brotherNode.set_isThreeNode(true);
                            alterNode.set_data1(null);
                            alterNode.set_value1(null);
                            alterNodeParentNode.set_data1(null);
                            alterNodeParentNode.set_value1(null);
                            alterNodeParentNode.set_right1(UNDEFINED);
                            alterNodeParentNode.set_left1(brotherNode.get_address());

                            insertToFileOnAddress(alterNodeParentNode,alterNodeParentNode.get_address());
                            if (!isLeaf(alterNode)){
                                //upravy referencii ak nie su listy
                                brotherNode.set_left2(brotherNode.get_right1());
                                if (alterNode.get_left1() != UNDEFINED){
                                    brotherNode.set_right2(alterNode.get_left1());
                                }else {
                                    brotherNode.set_right2(alterNode.get_right1());
                                }
                                if (brotherNode.get_right2() != UNDEFINED){
                                    BST23Node<T,V> brotherNodeRight2Node = getNodeForAddress(brotherNode.get_right2());
                                    brotherNodeRight2Node.set_parent(brotherNode.get_address());
                                    insertToFileOnAddress(brotherNodeRight2Node,brotherNodeRight2Node.get_address());
                                }
                                if (getNodeForAddress(brotherNode.get_parent()).get_parent() == UNDEFINED){
                                    //prazdne miesto je v koreni
                                    invalidateNode(alterNode);
                                    invalidateNode(getNodeForAddress(_root));
                                    _root = brotherNode.get_address();
                                    brotherNode.set_parent(UNDEFINED);
                                    insertToFileOnAddress(brotherNode,brotherNode.get_address());
                                    return true;
                                }
                            }
                            insertToFileOnAddress(brotherNode,brotherNode.get_address());
                        }
                        invalidateNode(alterNode);
                        alterNode = alterNodeParentNode;
                    }else {
                        //otec je trojvrchol
                        if (alterNodeParentNode.get_right1() == alterNode.get_address() ||
                                alterNodeParentNode.get_right2() == alterNode.get_address()){
                            //v je v strede alebo uplne napravo
                            if (alterNodeParentNode.get_right2() == brotherNode.get_address()){
                                //v' je uplne napravo
                                brotherNode.set_data2(brotherNode.get_data1());
                                brotherNode.set_value2(brotherNode.get_value1());
                                brotherNode.set_data1(alterNodeParentNode.get_data2());
                                brotherNode.set_value1(alterNodeParentNode.get_value2());
                                brotherNode.set_isThreeNode(true);
                                alterNodeParentNode.set_data2(null);
                                alterNodeParentNode.set_value2(null);
                                alterNodeParentNode.set_isThreeNode(false);
                                alterNodeParentNode.set_right1(brotherNode.get_address());
                                alterNodeParentNode.set_left2(UNDEFINED);
                                alterNodeParentNode.set_right2(UNDEFINED);

                                insertToFileOnAddress(alterNodeParentNode,alterNodeParentNode.get_address());
                                if (!isLeaf(alterNode)){
                                    //upravy referencii ak nie su listy
                                    brotherNode.set_right2(brotherNode.get_right1());
                                    brotherNode.set_left2(brotherNode.get_left1());
                                    brotherNode.set_right1(brotherNode.get_left1());
                                    if (alterNode.get_left1() != UNDEFINED){
                                        brotherNode.set_left1(alterNode.get_left1());
                                    }else {
                                        brotherNode.set_left1(alterNode.get_right1());
                                    }
                                    if (brotherNode.get_left1() != UNDEFINED){
                                        BST23Node<T,V> brotherNodeLeft1Node = getNodeForAddress(brotherNode.get_left1());
                                        brotherNodeLeft1Node.set_parent(brotherNode.get_address());
                                        insertToFileOnAddress(brotherNodeLeft1Node,brotherNodeLeft1Node.get_address());
                                    }
                                }
                                insertToFileOnAddress(brotherNode,brotherNode.get_address());
                                invalidateNode(alterNode);
                                return true;
                            }else {
                                //v' je v strede
                                brotherNode.set_data2(alterNodeParentNode.get_data2());
                                brotherNode.set_value2(alterNodeParentNode.get_value2());
                                brotherNode.set_isThreeNode(true);
                                alterNodeParentNode.set_data2(null);
                                alterNodeParentNode.set_value2(null);
                                alterNodeParentNode.set_isThreeNode(false);
                                alterNodeParentNode.set_left2(UNDEFINED);
                                alterNodeParentNode.set_right2(UNDEFINED);

                                insertToFileOnAddress(alterNodeParentNode,alterNodeParentNode.get_address());
                                if (!isLeaf(alterNode)){
                                    //upravy referencii ak nie su listy
                                    brotherNode.set_left2(brotherNode.get_right1());
                                    if (alterNode.get_left1() != UNDEFINED){
                                        brotherNode.set_right2(alterNode.get_left1());
                                    }else {
                                        brotherNode.set_right2(alterNode.get_right1());
                                    }
                                    if (brotherNode.get_right2() != UNDEFINED){
                                        BST23Node<T,V> brotherNodeRight2Node = getNodeForAddress(brotherNode.get_right2());
                                        brotherNodeRight2Node.set_parent(brotherNode.get_address());
                                        insertToFileOnAddress(brotherNodeRight2Node,brotherNodeRight2Node.get_address());
                                    }
                                }
                                insertToFileOnAddress(brotherNode,brotherNode.get_address());
                                invalidateNode(alterNode);
                                return true;
                            }
                        }else {
                            //v je uplne nalavo
                            brotherNode.set_data2(brotherNode.get_data1());
                            brotherNode.set_value2(brotherNode.get_value1());
                            brotherNode.set_data1(alterNodeParentNode.get_data1());
                            brotherNode.set_value1(alterNodeParentNode.get_value1());
                            brotherNode.set_isThreeNode(true);
                            alterNodeParentNode.set_data1(alterNodeParentNode.get_data2());
                            alterNodeParentNode.set_value1(alterNodeParentNode.get_value2());
                            alterNodeParentNode.set_data2(null);
                            alterNodeParentNode.set_value2(null);
                            alterNodeParentNode.set_isThreeNode(false);
                            alterNodeParentNode.set_left1(brotherNode.get_address());
                            alterNodeParentNode.set_right1(alterNodeParentNode.get_right2());
                            alterNodeParentNode.set_left2(UNDEFINED);
                            alterNodeParentNode.set_right2(UNDEFINED);

                            insertToFileOnAddress(alterNodeParentNode,alterNodeParentNode.get_address());
                            if (!isLeaf(alterNode)){
                                //upravy referencii ak nie su listy
                                brotherNode.set_right2(brotherNode.get_right1());
                                brotherNode.set_left2(brotherNode.get_left1());
                                brotherNode.set_right1(brotherNode.get_left1());
                                if (alterNode.get_left1() != UNDEFINED){
                                    brotherNode.set_left1(alterNode.get_left1());
                                }else {
                                    brotherNode.set_left1(alterNode.get_right1());
                                }
                                if (brotherNode.get_left1() != UNDEFINED){
                                    BST23Node<T,V> brotherNodeLeft1Node = getNodeForAddress(brotherNode.get_left1());
                                    brotherNodeLeft1Node.set_parent(brotherNode.get_address());
                                    insertToFileOnAddress(brotherNodeLeft1Node,brotherNodeLeft1Node.get_address());
                                }
                            }
                            insertToFileOnAddress(brotherNode,brotherNode.get_address());
                            invalidateNode(alterNode);
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    public BST23Node findBrother(BST23Node node){
        BST23Node parent = getNodeForAddress(node.get_parent());
        if (parent == null){
            return null;
        }
        //if (getNodeForAddress(parent.get_left1()) == node){
        if (areNodesEqual(getNodeForAddress(parent.get_left1()),node)){
            //node pre ktoreho hladam brata je uplne lavym synom jeho otca
            if (getNodeForAddress(parent.get_right1()) != null){
                return getNodeForAddress(parent.get_right1());
            }
        }else if(areNodesEqual(getNodeForAddress(parent.get_right1()),node)){
            //node pre ktoreho hladam brata je pravym synom pre prvy prvok v node jeho otca
            if (getNodeForAddress(parent.get_right2()) == null){
                //otec ma len dvoch synov
                return getNodeForAddress(parent.get_left1());
            }else {
                //otec ma troch synov cize mam na vyber dvoch bratov
                //uprednostnujem 3 vrchol
                if (getNodeForAddress(parent.get_left1()).isThreeNode()){
                    return getNodeForAddress(parent.get_left1());
                }else{
                    return getNodeForAddress(parent.get_right2());
                }
            }
        }else if(getNodeForAddress(parent.get_right2()) != null &&
                areNodesEqual(getNodeForAddress(parent.get_right2()),node)){
            //node pre ktoreho hladam brata je pravym synom pre druhy prvok v node jeho otca
            if (getNodeForAddress(parent.get_left2()) != null){
                return getNodeForAddress(parent.get_left2());
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
            BST23Node temp = getNodeForAddress(node.get_right1());
            while (!isLeaf(temp)){
                temp = getNodeForAddress(temp.get_left1());
            }
            return temp;
        }else if(nodeData.get_data1().compareTo(node.get_data2()) == 0){
            //data ktore mazem su napravo
            BST23Node temp = getNodeForAddress(node.get_right2());
            while (!isLeaf(temp)){
                temp = getNodeForAddress(temp.get_left1());
            }
            return temp;
        }
        return null;
    }

    public boolean isLeaf(BST23Node node){
        if (getNodeForAddress(node.get_left1()) == null &&
                getNodeForAddress(node.get_right1()) == null &&
                getNodeForAddress(node.get_right2()) == null){
            return true;
        }
        return false;
    }

    //vracia adresu kam sa ulozil
    private int insertToFile(BST23Node pInsertedNode){
        //naseekovanie na next address a vlozenie na dane miesto
        try {
            if (!invalidRecordsAdrIncr.isEmpty()){
                int savedOnAddress = invalidRecordsAdrIncr.peek();
                fileOfRecords.seek(savedOnAddress);
                //nastavenie adresy kam sa ide ukladat
                pInsertedNode.set_address(savedOnAddress);

                //ziskanie pola bytov pre T zaznam
                byte[] arrayOfDataBytes = pInsertedNode.ToByteArray();

                fileOfRecords.write(arrayOfDataBytes);
                invalidRecordsAdrDecr.remove(invalidRecordsAdrIncr.poll());
                //savePriorityQueuestoFile();
                return savedOnAddress;
            }else {
                fileOfRecords.seek(nextAddress);
                //nastavenie adresy kam sa ide ukladat
                pInsertedNode.set_address(nextAddress);

                //ziskanie pola bytov pre T zaznam
                byte[] arrayOfDataBytes = pInsertedNode.ToByteArray();

                fileOfRecords.write(arrayOfDataBytes);
                //zvysenie poctu len ked sa pridavalo na koniec
                //tak isto aj pre dalsiu adresu
                numberOfBlocks++;
                nextAddress += pInsertedNode.getSize();
                //ulozit upravene hlavickove subory
                //saveHeader();
                return nextAddress- pInsertedNode.getSize();
            }
        }catch (IOException exception){
            return -1;
        }
    }

    //metoda ktora uklada do suboru na urcene miesto(vyuzivane na update pre node)
    private boolean insertToFileOnAddress(BST23Node pInsertedNode, int pAddress){
        //naseekovanie na zadanu adresu a vlozenie na dane miesto
        try {
            fileOfRecords.seek(pAddress);
            //nastavenie adresy kam sa ide ukladat
            pInsertedNode.set_address(pAddress);

            //ziskanie pola bytov pre T zaznam
            byte[] arrayOfDataBytes = pInsertedNode.ToByteArray();

            fileOfRecords.write(arrayOfDataBytes);

            return true;
        }catch (IOException exception){
            return false;
        }
    }

    private int getNextAddress(){
        if (!invalidRecordsAdrIncr.isEmpty()){
            return invalidRecordsAdrIncr.peek();
        }else {
            return nextAddress;
        }
    }

    //taktiez robene podla prednaskoveho pseudkokodu v kombinacii s poskytnutou strankou z elearningu
    public boolean insert(BST23Node pNode){
        if (_root == UNDEFINED){
            int address = insertToFile(pNode);
            if (address == -1){
                return false;
            }else{
                _root = address;
                return true;
            }
        }else if(find(pNode) == null) {
            BST23Node leaf = findLeafForInsert(pNode);
            if (leaf != null){
                if (!leaf.isThreeNode()){
                    //vkladanie pokial ma list len jeden kluc(data1)
                    leaf.set_isThreeNode(true);
                    if (leaf.get_data1().compareTo(pNode.get_data1()) > 0){
                        leaf.set_data2(pNode.get_data1());
                        leaf.set_value2(pNode.get_value1());
                        //ulozenie leafu do suboru
                        insertToFileOnAddress(leaf, leaf.get_address());
                        return true;
                    }else if(leaf.get_data1().compareTo(pNode.get_data1()) < 0){
                        leaf.set_data2(leaf.get_data1());
                        leaf.set_value2(leaf.get_value1());
                        leaf.set_data1(pNode.get_data1());
                        leaf.set_value1(pNode.get_value1());
                        //ulozenie leafu do suboru
                        insertToFileOnAddress(leaf, leaf.get_address());
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
                        if (areNodesEqual(leaf, getNodeForAddress(_root))){
                            //ked je node korenom
                            BST23Node newRoot = new BST23Node(middle, middleValue);
                            BST23Node newRightSon = new BST23Node(max, maxValue);
                            int newRootAddress = insertToFile(newRoot);
                            int newRightSonAddress = getNextAddress();

                            newRoot.set_left1(leaf.get_address());
                            newRoot.set_right1(newRightSonAddress);
                            //update noveho roota
                            insertToFileOnAddress(newRoot,newRootAddress);

                            newRightSon.set_left1(leaf.get_left2());
                            newRightSon.set_right1(leaf.get_right2());
                            newRightSon.set_parent(newRootAddress);
                            //vlozenie noveho praveho syna
                            insertToFile(newRightSon);

                            //vlozenie

                            leaf.set_parent(newRootAddress);
                            leaf.set_isThreeNode(false);
                            leaf.set_data2(null);
                            leaf.set_value2(null);
                            leaf.set_data1(min);
                            leaf.set_value1(minValue);
                            BST23Node<T,V> right2Node = getNodeForAddress(leaf.get_right2());
                            BST23Node<T,V> left2Node = getNodeForAddress(leaf.get_left2());
                            if (right2Node != null && left2Node != null){
                                right2Node.set_parent(newRightSonAddress);
                                left2Node.set_parent(newRightSonAddress);
                                //update hodnot tychto dvoch nodov v subore
                                insertToFileOnAddress(right2Node, right2Node.get_address());
                                insertToFileOnAddress(left2Node, left2Node.get_address());
                            }
                            leaf.set_left2(UNDEFINED);
                            leaf.set_right2(UNDEFINED);
                            //update leaf nodu
                            insertToFileOnAddress(leaf, leaf.get_address());

                            _root = newRootAddress;
                            return true;
                        }else {
                            //node nie je korenom
                            BST23Node<T,V> leafParent = getNodeForAddress(leaf.get_parent());
                            if (!leafParent.isThreeNode()){
                                //pokial je otec dvojvrchol
                                if (areNodesEqual(getNodeForAddress(leafParent.get_left1()),leaf)){
                                    //ak je lavy potomok otca
                                    BST23Node newNode = new BST23Node(max, maxValue);
                                    newNode.set_parent(leaf.get_parent());
                                    newNode.set_left1(leaf.get_left2());
                                    newNode.set_right1(leaf.get_right2());
                                    //vlozenie noveho nodu do suboru
                                    int newNodeAddress = insertToFile(newNode);

                                    BST23Node<T,V> leafLeft2Node = getNodeForAddress(leaf.get_left2());
                                    BST23Node<T,V> leafRight2Node = getNodeForAddress(leaf.get_right2());
                                    if (leafLeft2Node != null && leafRight2Node != null){
                                        leafLeft2Node.set_parent(newNodeAddress);
                                        leafRight2Node.set_parent(newNodeAddress);
                                        //update hodnot dvoch nodov
                                        insertToFileOnAddress(leafLeft2Node, leafLeft2Node.get_address());
                                        insertToFileOnAddress(leafRight2Node, leafRight2Node.get_address());
                                    }
                                    leaf.set_left2(UNDEFINED);
                                    leaf.set_right2(UNDEFINED);

                                    leafParent.set_data2(leafParent.get_data1());
                                    leafParent.set_value2(leafParent.get_value1());
                                    leafParent.set_data1(middle);
                                    leafParent.set_value1(middleValue);
                                    leafParent.set_isThreeNode(true);
                                    leafParent.set_right2(leafParent.get_right1());
                                    leafParent.set_right1(newNodeAddress);
                                    leafParent.set_left2(newNodeAddress);
                                    //update parenta pre leaf
                                    insertToFileOnAddress(leafParent, leafParent.get_address());

                                    leaf.set_data2(null);
                                    leaf.set_value2(null);
                                    leaf.set_data1(min);
                                    leaf.set_value1(minValue);
                                    leaf.set_isThreeNode(false);
                                    //update leafu
                                    insertToFileOnAddress(leaf,leaf.get_address());
                                    return true;
                                }else{
                                    //ak je pravy potomok
                                    BST23Node newNode = new BST23Node(min, minValue);
                                    newNode.set_parent(leaf.get_parent());
                                    newNode.set_left1(leaf.get_left1());
                                    newNode.set_right1(leaf.get_right1());

                                    //vlozenie noveho nodu do suboru
                                    int newNodeAddress = insertToFile(newNode);

                                    BST23Node<T,V> leafLeft1Node = getNodeForAddress(leaf.get_left1());
                                    BST23Node<T,V> leafRight1Node = getNodeForAddress(leaf.get_right1());
                                    if (leafLeft1Node != null && leafRight1Node != null){
                                        leafLeft1Node.set_parent(newNodeAddress);
                                        leafRight1Node.set_parent(newNodeAddress);
                                        //update hodnot dvoch nodov
                                        insertToFileOnAddress(leafLeft1Node, leafLeft1Node.get_address());
                                        insertToFileOnAddress(leafRight1Node, leafRight1Node.get_address());
                                    }

                                    leafParent.set_data2(middle);
                                    leafParent.set_value2(middleValue);
                                    leafParent.set_isThreeNode(true);
                                    leafParent.set_right2(leafParent.get_right1());
                                    leafParent.set_right1(newNodeAddress);
                                    leafParent.set_left2(newNodeAddress);
                                    //update parenta pre leaf
                                    insertToFileOnAddress(leafParent, leafParent.get_address());

                                    leaf.set_left1(leaf.get_left2());
                                    leaf.set_right1(leaf.get_right2());
                                    leaf.set_left2(UNDEFINED);
                                    leaf.set_right2(UNDEFINED);
                                    leaf.set_data2(null);
                                    leaf.set_value2(null);
                                    leaf.set_data1(max);
                                    leaf.set_value1(maxValue);
                                    leaf.set_isThreeNode(false);
                                    //update leafu
                                    insertToFileOnAddress(leaf,leaf.get_address());
                                    return true;
                                }
                            }else {
                                //pokial je otec trojvrchol(doslo by k preteceniu)
                                if(areNodesEqual(getNodeForAddress(leafParent.get_right2()),leaf)){
                                    //leaf je pravy potomok
                                    BST23Node newNode = new BST23Node(min, minValue);
                                    newNode.set_parent(leaf.get_parent());
                                    newNode.set_left1(leaf.get_left1());
                                    newNode.set_right1(leaf.get_right1());

                                    //vlozenie noveho nodu do suboru
                                    int newNodeAddress = insertToFile(newNode);

                                    BST23Node<T,V> leafLeft1Node = getNodeForAddress(leaf.get_left1());
                                    BST23Node<T,V> leafRight1Node = getNodeForAddress(leaf.get_right1());
                                    if (leafLeft1Node != null && leafRight1Node != null){
                                        leafLeft1Node.set_parent(newNodeAddress);
                                        leafRight1Node.set_parent(newNodeAddress);
                                        //update hodnot dvoch nodov
                                        insertToFileOnAddress(leafLeft1Node, leafLeft1Node.get_address());
                                        insertToFileOnAddress(leafRight1Node, leafRight1Node.get_address());
                                    }

                                    leafParent.set_left2(newNodeAddress);
                                    //update parenta pre leaf
                                    insertToFileOnAddress(leafParent, leafParent.get_address());

                                    leaf.set_left1(leaf.get_left2());
                                    leaf.set_right1(leaf.get_right2());
                                    leaf.set_left2(UNDEFINED);
                                    leaf.set_right2(UNDEFINED);

                                    leaf.set_data2(null);
                                    leaf.set_value2(null);
                                    leaf.set_data1(max);
                                    leaf.set_value1(maxValue);
                                    leaf.set_isThreeNode(false);
                                    //update leaf
                                    insertToFileOnAddress(leaf,leaf.get_address());

                                    leaf = getNodeForAddress(leaf.get_parent());
                                    pNode.set_data1(middle);
                                    pNode.set_value1(middleValue);
                                }else if(areNodesEqual(getNodeForAddress(leafParent.get_right1()), leaf)){
                                    //leaf je v strede
                                    BST23Node newNode = new BST23Node(max, maxValue);
                                    newNode.set_parent(leaf.get_parent());
                                    newNode.set_left1(leaf.get_left2());
                                    newNode.set_right1(leaf.get_right2());

                                    //vlozenie noveho nodu do suboru
                                    int newNodeAddress = insertToFile(newNode);

                                    BST23Node<T,V> leafLeft2Node = getNodeForAddress(leaf.get_left2());
                                    BST23Node<T,V> leafRight2Node = getNodeForAddress(leaf.get_right2());
                                    if (leafLeft2Node != null && leafRight2Node != null){
                                        leafLeft2Node.set_parent(newNodeAddress);
                                        leafRight2Node.set_parent(newNodeAddress);
                                        //update hodnot dvoch nodov
                                        insertToFileOnAddress(leafLeft2Node, leafLeft2Node.get_address());
                                        insertToFileOnAddress(leafRight2Node, leafRight2Node.get_address());
                                    }
                                    leafParent.set_left2(newNodeAddress);
                                    //update parenta pre leaf
                                    insertToFileOnAddress(leafParent, leafParent.get_address());

                                    leaf.set_left2(UNDEFINED);
                                    leaf.set_right2(UNDEFINED);
                                    leaf.set_data2(null);
                                    leaf.set_value2(null);
                                    leaf.set_data1(min);
                                    leaf.set_value1(minValue);
                                    leaf.set_isThreeNode(false);
                                    //update leaf
                                    insertToFileOnAddress(leaf,leaf.get_address());

                                    leaf = getNodeForAddress(leaf.get_parent());
                                    pNode.set_data1(middle);
                                    pNode.set_value1(middleValue);
                                }else {
                                    //leaf je lavy potomok
                                    BST23Node newNode = new BST23Node(max, maxValue);
                                    newNode.set_parent(leaf.get_parent());
                                    newNode.set_left1(leaf.get_left2());
                                    newNode.set_right1(leaf.get_right2());

                                    //vlozenie noveho nodu do suboru
                                    int newNodeAddress = insertToFile(newNode);

                                    BST23Node<T,V> leafLeft2Node = getNodeForAddress(leaf.get_left2());
                                    BST23Node<T,V> leafRight2Node = getNodeForAddress(leaf.get_right2());
                                    if (leafLeft2Node != null && leafRight2Node != null){
                                        leafLeft2Node.set_parent(newNodeAddress);
                                        leafRight2Node.set_parent(newNodeAddress);
                                        //update hodnot dvoch nodov
                                        insertToFileOnAddress(leafLeft2Node, leafLeft2Node.get_address());
                                        insertToFileOnAddress(leafRight2Node, leafRight2Node.get_address());
                                    }
                                    leafParent.set_right1(newNodeAddress);
                                    //update parenta pre leaf
                                    insertToFileOnAddress(leafParent, leafParent.get_address());

                                    leaf.set_left2(UNDEFINED);
                                    leaf.set_right2(UNDEFINED);
                                    leaf.set_data2(null);
                                    leaf.set_value2(null);
                                    leaf.set_data1(min);
                                    leaf.set_value1(minValue);
                                    leaf.set_isThreeNode(false);
                                    //update leaf
                                    insertToFileOnAddress(leaf,leaf.get_address());

                                    leaf = getNodeForAddress(leaf.get_parent());
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
        if (_root == UNDEFINED){
            return null;
        }
        BST23Node loadedRoot = getNodeForAddress(_root);
        if (loadedRoot != null){
            if (getNodeForAddress(loadedRoot.get_left1()) == null &&
                    getNodeForAddress(loadedRoot.get_right1()) == null &&
                    getNodeForAddress(loadedRoot.get_right2()) == null){
                //nema synov
                //overovanie ci 2 vrchol alebo 3 sa bude robit az v insert
                return loadedRoot;
            }else {
                BST23Node prev = null;
                BST23Node temp = loadedRoot;
                while (temp != null){
                    //if (temp.get_left() == null && temp.get_right() == null){
                    if (getNodeForAddress(temp.get_left1()) == null &&
                            getNodeForAddress(temp.get_right1()) == null &&
                            getNodeForAddress(temp.get_right2()) == null){
                        return temp;
                    }else {
                        if (temp.isThreeNode()){
                            //v pnode je zase data len data1
                            if(temp.get_data1().compareTo(pNode.get_data1()) < 0){
                                prev = temp;
                                temp = getNodeForAddress(prev.get_left1());
                            }else if(temp.get_data2().compareTo(pNode.get_data1()) > 0){
                                prev = temp;
                                temp = getNodeForAddress(prev.get_right2());
                            }else if ((temp.get_data1().compareTo(pNode.get_data1()) > 0) &&
                                    (temp.get_data2().compareTo(pNode.get_data1()) < 0)){
                                prev = temp;
                                temp = getNodeForAddress(prev.get_right1());
                            }
                        }else {
                            if (temp.get_data1().compareTo(pNode.get_data1()) < 0){
                                prev = temp;
                                temp = getNodeForAddress(prev.get_left1());
                            }else if (temp.get_data1().compareTo(pNode.get_data1()) > 0){
                                prev = temp;
                                temp = getNodeForAddress(prev.get_right1());
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
        if (_root == UNDEFINED){
            return null;
        }
        BST23Node loadedRoot = getNodeForAddress(_root);

        if (loadedRoot == null ||
                (pNode.get_data1().compareTo(loadedRoot.get_data1()) == 0) ||
                ((loadedRoot.get_data2() != null) && (pNode.get_data1().compareTo(loadedRoot.get_data2()) == 0))){
            if (loadedRoot.isValid()){
                return loadedRoot;
            }else {
                return null;
            }
        }else {
            BST23Node prev = null;
            BST23Node temp = loadedRoot;
            while (temp != null){
                if (temp.isThreeNode()){
                    if (temp.get_data1().compareTo(pNode.get_data1()) < 0){
                        //hladany kluc je mensi ako lavy vrchol(teda data1)
                        prev = temp;
                        temp = getNodeForAddress(prev.get_left1());
                    }else if (temp.get_data2().compareTo(pNode.get_data1()) > 0){
                        //hladany kluc je vacsi ako pravy vrchol
                        prev = temp;
                        temp = getNodeForAddress(prev.get_right2());
                    }else if ((temp.get_data1().compareTo(pNode.get_data1()) > 0) &&
                            (temp.get_data2().compareTo(pNode.get_data1()) < 0)){
                        //hladany kluc je medzi pravym a lavym klucom
                        prev = temp;
                        temp = getNodeForAddress(prev.get_right1());
                    }else if ((temp.get_data1().compareTo(pNode.get_data1()) == 0) ||
                            (temp.get_data2().compareTo(pNode.get_data1()) == 0)){
                        //hladany kluc je jeden z klucov dvoch vrcholov
                        if (temp.isValid()){
                            return temp;
                        }else {
                            return null;
                        }
                    }
                }else {
                    if (temp.get_data1().compareTo(pNode.get_data1()) > 0){
                        //ak pnode data1 je vacsie ako tempdata
                        prev = temp;
                        temp = getNodeForAddress(prev.get_right1());
                    }else if(temp.get_data1().compareTo(pNode.get_data1()) < 0){
                        //ak pnode data1 je mensie ako tempdata
                        prev = temp;
                        temp = getNodeForAddress(prev.get_left1());
                    }
                    if (temp != null){
                        if (temp.get_data1().compareTo(pNode.get_data1()) == 0){
                            if (temp.isValid()){
                                return temp;
                            }else {
                                return null;
                            }
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
        if (_root == UNDEFINED){
            return listOfFoundNodes;
        }
        BST23Node temp = getNodeForAddress(_root);
        if (temp == null){
            return listOfFoundNodes;
        }
        while (temp != null){
            if (temp.isThreeNode()){
                if (temp.get_data1().compareTo(minNode.get_data1()) < 0){
                    //hladany kluc je mensi ako lavy vrchol(teda data1)
                    prev = temp;
                    prevKey = (T) prev.get_data1();
                    temp = getNodeForAddress(prev.get_left1());
                }else if (temp.get_data2().compareTo(minNode.get_data1()) > 0){
                    //hladany kluc je vacsi ako pravy vrchol
                    prev = temp;
                    prevKey = (T) prev.get_data2();
                    temp = getNodeForAddress(prev.get_right2());
                }else if ((temp.get_data1().compareTo(minNode.get_data1()) > 0) &&
                        (temp.get_data2().compareTo(minNode.get_data1()) < 0)){
                    //hladany kluc je medzi pravym a lavym klucom
                    prev = temp;
                    prevKey = (T) prev.get_data1();
                    temp = getNodeForAddress(prev.get_right1());
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
                    temp = getNodeForAddress(prev.get_right1());
                }else if(temp.get_data1().compareTo(minNode.get_data1()) < 0){
                    //ak min data1 je mensie ako tempdata
                    prev = temp;
                    prevKey = (T) prev.get_data1();
                    temp = getNodeForAddress(prev.get_left1());
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
            if (getNodeForAddress(node.get_right1()) != null){
                BST23Node temp = getNodeForAddress(node.get_right1());
                while (!isLeaf(temp)){
                    temp = getNodeForAddress(temp.get_left1());
                }
                NodeAndKey nodeAndKey = new NodeAndKey(temp, temp.get_data1());
                return nodeAndKey;
            }else {
                //nema syna tak pojde po parentoch
                BST23Node temp = node;
                while (temp != null){
                    BST23Node<T,V> parent = getNodeForAddress(temp.get_parent());
                    if (parent != null){
                        if (parent.isThreeNode()){
                            if (key.compareTo((T) parent.get_data1()) > 0){
                                NodeAndKey nodeAndKey = new NodeAndKey(parent, parent.get_data1());
                                return nodeAndKey;
                            }
                            if ((key.compareTo((T) parent.get_data1()) < 0) &&
                                    (key.compareTo((T) parent.get_data2()) > 0)){
                                NodeAndKey nodeAndKey = new NodeAndKey(parent, parent.get_data2());
                                return nodeAndKey;
                            }
                        }else {
                            if (key.compareTo((T) parent.get_data1()) > 0){
                                NodeAndKey nodeAndKey = new NodeAndKey(parent, parent.get_data1());
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
                    temp = getNodeForAddress(temp.get_parent());
                }
            }
        }else {
            //node je 3 vrchol
            if (key.compareTo((T) node.get_data1()) == 0){
                if (getNodeForAddress(node.get_right1()) == null){
                    //nema stredneho syna tak nema kam vliezt tak vracia data 2
                    NodeAndKey nodeAndKey = new NodeAndKey(node, node.get_data2());
                    return nodeAndKey;
                }else {
                    //ma stredneho syna tak vlezie do neho
                    BST23Node temp = getNodeForAddress(node.get_right1());
                    while (!isLeaf(temp)){
                        //lez stale dolava az kym nenarazis na list
                        temp = getNodeForAddress(temp.get_left1());
                    }
                    NodeAndKey nodeAndKey = new NodeAndKey(temp, temp.get_data1());
                    return nodeAndKey;
                }
            }else if(key.compareTo((T) node.get_data2()) == 0){
                //hladame nasledovnika 2. kluca
                if (getNodeForAddress(node.get_right2()) != null){
                    //ma praveho syna tak vlezie do neho
                    BST23Node temp = getNodeForAddress(node.get_right2());
                    while (!isLeaf(temp)){
                        //lez stale dolava az kym nenarazis na list
                        temp = getNodeForAddress(temp.get_left1());
                    }
                    NodeAndKey nodeAndKey = new NodeAndKey(temp, temp.get_data1());
                    return nodeAndKey;
                }else {
                    BST23Node temp = node;
                    while (temp != null){
                        BST23Node<T,V> parent = getNodeForAddress(temp.get_parent());
                        if (parent != null){
                            if (parent.isThreeNode()){
                                if (key.compareTo((T) parent.get_data1()) > 0){
                                    NodeAndKey nodeAndKey = new NodeAndKey(parent, parent.get_data1());
                                    return nodeAndKey;
                                }
                                if ((key.compareTo((T) parent.get_data1()) < 0) &&
                                        (key.compareTo((T) parent.get_data2()) > 0)){
                                    NodeAndKey nodeAndKey = new NodeAndKey(parent, parent.get_data2());
                                    return nodeAndKey;
                                }
                            }else {
                                if (key.compareTo((T) parent.get_data1()) > 0){
                                    NodeAndKey nodeAndKey = new NodeAndKey(parent, parent.get_data1());
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
                        temp = getNodeForAddress(temp.get_parent());
                    }
                }
            }
        }
        return null;
    }

    public ArrayList<BST23Node> inOrder(){
        ArrayList<BST23Node> listOfFoundNodes = new ArrayList<>();
        if (_root == UNDEFINED){
            return listOfFoundNodes;
        }
        BST23Node temp = getNodeForAddress(_root);
        if (temp == null){
            return listOfFoundNodes;
        }
        while (!isLeaf(temp)){
            temp = getNodeForAddress(temp.get_left1());
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
        if (_root == UNDEFINED){
            return null;
        }
        BST23Node temp = getNodeForAddress(_root);
        if (temp == null){
            return null;
        }
        while (!isLeaf(temp)){
            temp = getNodeForAddress(temp.get_left1());
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
        if (_root == UNDEFINED){
            return null;
        }
        BST23Node loadedRoot = getNodeForAddress(_root);
        return loadedRoot;
    }

    public ArrayList<BST23Node> getAllNodesFromFile(){
        ArrayList<BST23Node> listOfNodes = new ArrayList<>();
        if (_root == UNDEFINED){
            return listOfNodes;
        }else {
            int next = HEADER_SIZE;
            while (next < nextAddress){
                BST23Node node = new BST23Node(classTypeKey, classTypeValue);
                //nacitanie pola bytov zo suboru
                byte[] arrayOfDataBytes = new byte[node.getSize()];
                try {
                    fileOfRecords.seek(next);
                    fileOfRecords.read(arrayOfDataBytes);
                }catch (IOException exception){
                    return null;
                }
                //nacitanie hodnot pre node z pola bytov
                node.FromByteArray(arrayOfDataBytes);
                listOfNodes.add(node);
                next += node.getSize();
            }
            return listOfNodes;
        }
    }
}



class NodeAndKey<T extends  Comparable<T> & IData, V extends IData>{
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
