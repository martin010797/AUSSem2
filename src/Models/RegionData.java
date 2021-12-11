package Models;

import Structure.BST23Node;

public class RegionData extends BST23Node<RegionKey, Address> {
    public RegionData(RegionKey key, Address address){
        super(key,address);
    }
}
