package mapData;

import java.util.LinkedList;

/**
 * Created by bopablo.g on 2015-05-06.
 */
public class Tile {

    private String name;
    private LinkedList<ElementMapy> mapItems = new LinkedList<ElementMapy>();

    public Tile(String name){
        this.name = name;
    }

    public String getName() {
        return name;
    }


    public void addItem(ElementMapy item){
        mapItems.add(item);
    }

    public void addMapItems(LinkedList<ElementMapy> collection, int size){
        mapItems.addAll(collection);
    }

    public LinkedList<ElementMapy> getMapItems(){
        return mapItems;
    }

    public int size() {

        int size = 0;

        for (ElementMapy item : this.mapItems) {

            size += 8 + 4 + 4 + 4 +4; // id, nameId, type, flags, numNodes
            size += (item.numNodes * 2) * 4;
            size += 4; //numSegments
            if (item.numSegments !=1 ) {
                size += item.numSegments * 4;
            }
        }

        return size;
    }

}
