package Models;

import Structure.BST23Node;

public class WorkplaceData extends BST23Node<WorkplaceKey, Address> {
    public WorkplaceData(WorkplaceKey key, Address address){
        super(key,address);
    }
}
