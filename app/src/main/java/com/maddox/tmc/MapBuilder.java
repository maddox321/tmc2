package com.maddox.tmc;

import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

import java.io.BufferedWriter;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;

import mapData.BoundingBox;
import mapData.ElementMapy;
import mapData.Miasto;
import mapData.Node;
import mapData.OsmElement;
import mapData.Tile;

/**
 * Created by bopablo.g on 2015-05-06.
 */
public class MapBuilder {

    public static LinkedList<OsmElement> osmElements;
    public static HashMap<String, Tile> tileMap;
    public static HashMap<Long, Node> 		nodeMap;
    public static HashMap<String, Miasto>  		cityMap;
    public static HashMap<String, NameRecord> 	nameRecords;
    public static BoundingBox mapRegion;
    public static int nameRecordID = 0;

    private static final int MAX_TILE_DEPTH = 17;

    //private static Logger logger = Logger.getLogger(MapBuilder.class.getName());

    public MapBuilder() {

        tileMap 	= new HashMap<String, Tile>();
        cityMap 	= new HashMap<String, Miasto>();
        nodeMap	 	= new HashMap<Long, Node>();
        osmElements = new LinkedList<OsmElement>();
        nameRecords = new HashMap<String, NameRecord>();
        mapRegion   = new BoundingBox();
    }

    public void parseOsmFile(String filename){

        try {
/*
            XMLReader parser = new SAXParser();
            OsmContentHandler contentHandler = 	new OsmContentHandler();

            parser.setContentHandler(contentHandler);
            parser.parse(filename);*/

            SAXParserFactory factory = SAXParserFactory.newInstance();
            factory.setNamespaceAware(true);
            SAXParser parser = factory.newSAXParser();
            OsmContentHandler handler = new OsmContentHandler();
            parser.parse(filename, handler);

        }/*
        catch(IOException e){

            System.out.println("Error reading file: " + e.getMessage());
            throw new RuntimeException(e);
        }
        catch(SAXException e){

            System.out.println("Error in parsing: " + e.getMessage());
            throw new RuntimeException(e);
        }*/ catch (ParserConfigurationException e) {
            e.printStackTrace();
        } catch (SAXException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public void buildTiles() {

        for (OsmElement element : this.osmElements) {

            // skip place elements represented by an area
            if (element.getType().indexOf("place") >= 0 && element.getNumNodes() != 1) {

                continue;
            }

            ElementMapy item = element.convert();

            String name = item.name;
            if (name != null && MapBuilder.nameRecords != null) {
                item.nameId = MapBuilder.createNameRecord(item, name);
            }

            String tileName = this.matchTile(item);

            Tile tile = (Tile)tileMap.get(tileName);

            if(tile != null) {

                tile.addItem(item);
            }
            else {

                Tile t = new Tile(tileName);
                t.addItem(item);
                tileMap.put(tileName, t);
            }

        }
    }

    public String matchTile(ElementMapy item) {

        int x0,x1,x2,x3,x4;
        int y0,y1,y2,y3,y4;
        int i;
        int maxTileDepth;

        StringBuffer tileName = new StringBuffer();
        BoundingBox itemBBox  = new BoundingBox();


        // Initialize the bounding box for this way to the
        // coordinates of the first node
        itemBBox.minX = item.nodes[0];
        itemBBox.maxX = item.nodes[0];
        itemBBox.minY = item.nodes[1];
        itemBBox.maxY = item.nodes[1];

		/* Calculate the bounding box for this way */
        for(i = 0; i < item.numNodes; ++i)
        {
            int x = item.nodes[i*2];
            int y = item.nodes[i*2+1];

            itemBBox.maxX = (x > itemBBox.maxX) ? x : itemBBox.maxX;
            itemBBox.minX = (x < itemBBox.minX) ? x : itemBBox.minX;
            itemBBox.maxY = (y > itemBBox.maxY) ? y : itemBBox.maxY;
            itemBBox.minY = (y < itemBBox.minY) ? y : itemBBox.minY;
        }

		/* find out the maximum tile depth for this element */
        maxTileDepth = (int)(item.type/1000);

		/* now the wayBox contains the whole street;
		 * we must find out in which tile this element fits in
		 */
        x0 = BoundingBox.WORLD_MIN_X;
        y0 = BoundingBox.WORLD_MIN_Y;
        x4 = BoundingBox.WORLD_MAX_X;
        y4 = BoundingBox.WORLD_MAX_Y;

        for (i = 0 ; i < maxTileDepth ; i++) {

            x2=(x0+x4)/2;
            y2=(y0+y4)/2;

            if (itemBBox.isIncludedIn(x0,y0,x2,y2)) {
                tileName.append("c");
                x4=x2;
                y4=y2;
            }
            else if (itemBBox.isIncludedIn(x2,y0,x4,y2)) {
                tileName.append("d");
                x0=x2;
                y4=y2;
            }
            else if (itemBBox.isIncludedIn(x0,y2,x2,y4)) {
                tileName.append("a");
                x4=x2;
                y0=y2;
            }
            else if (itemBBox.isIncludedIn(x2,y2,x4,y4)) {
                tileName.append("b");
                x0=x2;
                y0=y2;
            }
            else
                break;
        }

        if (tileName.length()==0)
            tileName.append("index");

        return tileName.toString();
    }

    public static String findNearestCity(int x, int y) {

        if (cityMap == null)
            return "";

        String nearestCity = "";
        long minDistance = Long.MAX_VALUE;

        Iterator it = cityMap.keySet().iterator();

        while (it.hasNext()) {

            String cityName = (String)it.next();
            Miasto c = cityMap.get(cityName);

            long t1 = (long)c.x - x;
            long t2 = (long)c.y - y;

            long distance = (long) Math.sqrt(t1 * t1 + t2 * t2);

            if (c.type.equals("city")) {
                distance /= 2;
            }
            else if (c.type.equals("town")) {
                distance *= 1;
            }
            else if (c.type.equals("village")) {
                distance *= 2;
            }
            else {
                distance *= 4;
            }

            if (distance < minDistance) {
                minDistance = distance;
                nearestCity = cityName;
            }
        }

        return nearestCity;
    }

    public static int createNameRecord(ElementMapy item, String name){

        int centerX, centerY;

        if ((item.flags & OsmElement.SHAPE_AREA_MASK) == OsmElement.SHAPE_AREA_MASK) {

            Punkt[] vertices = new Punkt[item.numNodes];

            for (int i = 0; i < item.numNodes; i++) {
                vertices[i] = new Punkt(item.nodes[i*2], item.nodes[i*2+1]);
            }

            Polygon polygon = new Polygon(vertices);
            Punkt p = polygon.findCentroid();

            centerX = (int)(p.x);
            centerY = (int)(p.y);

        }
        else {

            if (item.numNodes == 1) {

                centerX = item.nodes[0];
                centerY = item.nodes[1];
            }
            else if (item.numNodes == 2) {

                centerX = item.nodes[0] + (item.nodes[2] - item.nodes[0])/2;
                centerY = item.nodes[1] + (item.nodes[3] - item.nodes[1])/2;
            }
            else {
                int centerNode = item.numNodes / 2;
                centerX = item.nodes[centerNode*2];
                centerY = item.nodes[centerNode*2+1];
            }

        }

        String city = MapBuilder.findNearestCity(centerX, centerY);

        String key = city + ":" + name;

        NameRecord nameRecord = MapBuilder.nameRecords.get(key);
        if (nameRecord == null) {
            nameRecord         = new NameRecord();
            nameRecord.id      = ++MapBuilder.nameRecordID;
            nameRecord.name    = name;
            nameRecord.city    = city;
            nameRecord.centerX = centerX;
            nameRecord.centerY = centerY;
            nameRecord.length  = item.getLength();

            MapBuilder.nameRecords.put(key, nameRecord);
            return nameRecord.id;
        }
        else {

            int length = item.getLength();
            if (length > nameRecord.length) {

                int centerNode = item.numNodes / 2;
                nameRecord.centerX = item.nodes[centerNode * 2];
                nameRecord.centerY = item.nodes[centerNode * 2 + 1];

                nameRecord.length = length;
            }

            return nameRecord.id;
        }
    }

    public void mergeTiles() {

        int work_done = 0;

        do{
            work_done = 0;
            LinkedList<String> sortedTiles = new LinkedList<String>();
            Iterator it = tileMap.entrySet().iterator();

            while (it.hasNext()) {
                Map.Entry pairs = (Map.Entry)it.next();
                if ((String)pairs.getKey()!=null)
                    sortedTiles.add((String)pairs.getKey());
            }

            Collections.sort(sortedTiles, new TileComparator());

            it = sortedTiles.descendingIterator();
            while (it.hasNext() && !(sortedTiles.size()==1 && sortedTiles.peekFirst().equals("index") ) ) {

                work_done = 0;

                String last = (String)it.next();
                if(last.equals("index"))
                    break;

                String[] subtileName = new String[4];

                subtileName[0] = last.substring(0, last.length()-1) + "a";
                subtileName[1] = last.substring(0, last.length()-1) + "b";
                subtileName[2] = last.substring(0, last.length()-1) + "c";
                subtileName[3] = last.substring(0, last.length()-1) + "d";

                String basetileName;

                if (subtileName[0].length() != 1) {
                    basetileName = last.substring(0, last.length()-1);
                }
                else {
                    basetileName = "index";
                }

                Tile[] subtile = new Tile[4];
                subtile[0] = (Tile)tileMap.get(subtileName[0]);
                subtile[1] = (Tile)tileMap.get(subtileName[1]);
                subtile[2] = (Tile)tileMap.get(subtileName[2]);
                subtile[3] = (Tile)tileMap.get(subtileName[3]);

                Tile basetile = (Tile)tileMap.get(basetileName);

                int basetileSize = 0;
                if (basetile != null) {
                    basetileSize = basetile.size();
                }
                else {
                    basetile = new Tile(basetileName);
                    tileMap.put(basetileName, basetile);
                }

                int[] subtileSize = new int[4];
                subtileSize[0] = subtileSize[1] = subtileSize[2] = subtileSize[3] = 0;

                if (subtile[0] != null)
                    subtileSize[0] = subtile[0].size();

                if (subtile[1] != null)
                    subtileSize[1] = subtile[1].size();

                if (subtile[2] != null)
                    subtileSize[2] = subtile[2].size();

                if (subtile[3] != null)
                    subtileSize[3] = subtile[3].size();

                int sizeAll = basetileSize + subtileSize[0] +
                        subtileSize[1] + subtileSize[2] + subtileSize[3];

                for (;;) {
                    int size_min=sizeAll;
                    int i_min=-1;

                    for (int i = 0 ; i < 4 ; i++) {
                        if (subtileSize[i] != 0 && subtileSize[i] <= size_min) {
                            size_min=subtileSize[i];
                            i_min=i;
                        }
                    }
                    if (i_min == -1)
                        break;

                    if (basetileSize+size_min >= 65536)
                        break;

                    work_done += mergeTile(basetileName, subtileName[i_min]);

                    basetileSize += subtileSize[i_min];
                    subtileSize[i_min]=0;
                }

            }


        } while (work_done > 0);

    }

    public int mergeTile(String basetile, String subtile) {

    //    logger.info("Merging "+subtile+" with " + basetile);

        Tile baset =  (Tile)tileMap.get(basetile);
        Tile subt  =  (Tile)tileMap.get(subtile);

        LinkedList<ElementMapy> elements = subt.getMapItems();

        baset.addMapItems(elements, subt.size());

        tileMap.remove(subtile);

        return 1;
    }

    public void writeTiles(String tilesDir) throws IOException {

        // delete the existing tiles
        try {
            File folder = new File (tilesDir);
            File[] files = folder.listFiles();

            for (File file : files) {
                file.delete();
            }
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }

        do {

            //write the coordinates file
            File file = new File(tilesDir + "/" + "coords");
            FileOutputStream file_output = new FileOutputStream (file);
            DataOutputStream data_out = new DataOutputStream (file_output);

            data_out.writeInt(MapBuilder.mapRegion.minX);
            data_out.writeInt(MapBuilder.mapRegion.maxX);
            data_out.writeInt(MapBuilder.mapRegion.minY);
            data_out.writeInt(MapBuilder.mapRegion.maxY);

            file_output.close();

        } while (false);

        Iterator it = tileMap.keySet().iterator();
        while(it.hasNext()){

            Tile tile = (Tile)tileMap.get(it.next());

            LinkedList<ElementMapy> mapItems = tile.getMapItems();
            Collections.sort(mapItems, new ItemComparator());

            File file = new File(tilesDir + "/" + tile.getName());
            FileOutputStream file_output = new FileOutputStream (file);
            DataOutputStream data_out = new DataOutputStream (file_output);

            for (ElementMapy item: mapItems) {

                data_out.writeLong(item.id);
                data_out.writeInt(item.nameId);
                data_out.writeInt(item.type);
                data_out.writeInt(item.flags);
                data_out.writeInt(item.minX);
                data_out.writeInt(item.maxX);
                data_out.writeInt(item.minY);
                data_out.writeInt(item.maxY);
                data_out.writeInt(item.numNodes);
                for (int i = 0; i < item.nodes.length; i++) {
                    data_out.writeInt(item.nodes[i]);
                }

                data_out.writeInt(item.numSegments);
                if (item.numSegments > 1) {
                    for (int i = 0; i < item.segments.length; i++) {
                        data_out.writeInt(item.segments[i]);
                    }
                }

            }

            file_output.close ();

        }
    }

    public void writeNameRecords(String path) throws IOException {

        BufferedWriter dataOut = null;

        try {

            dataOut = new BufferedWriter(new FileWriter(path));

            Iterator it = nameRecords.keySet().iterator();
            while (it.hasNext()) {

                NameRecord nameRecord = (NameRecord)nameRecords.get(it.next());
				/*
				nameRecord.name = nameRecord.name.replace('ş', 's');
				nameRecord.name = nameRecord.name.replace('â', 'a');
				nameRecord.name = nameRecord.name.replace('ţ', 't');
				nameRecord.name = nameRecord.name.replace('ă', 'a');

				nameRecord.city = nameRecord.city.replace('ş', 's');
				nameRecord.city = nameRecord.city.replace('â', 'a');
				nameRecord.city = nameRecord.city.replace('ţ', 't');
				nameRecord.city = nameRecord.city.replace('ă', 'a');
				*/
                dataOut.write(nameRecord.id + "\n");
                dataOut.write(nameRecord.name + "\n");
                dataOut.write(nameRecord.city + "\n");
                dataOut.write(nameRecord.centerX + "\n");
                dataOut.write(nameRecord.centerY + "\n");
            }

            dataOut.close();
        }
        catch (IOException e) {
            return;
        }
    }

}
