package com.maddox.tmc;

import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import java.util.HashMap;
import java.util.LinkedList;

import mapData.Mercator;
import mapData.Miasto;
import mapData.Node;
import mapData.OsmElement;

/**
 * Created by bopablo.g on 2015-05-06.
 */
public class OsmContentHandler extends DefaultHandler {

    private Node currNode;
    private OsmElement currElement;

    private HashMap nodeMap;
    private HashMap    			   cityMap;
    private LinkedList<OsmElement> elements;
    private LinkedList<String> 	   excludedAttributes;

    private static final int MAX_TILE_DEPTH = 17;

    public OsmContentHandler() {

        this.nodeMap  = MapBuilder.nodeMap;
        this.cityMap  = MapBuilder.cityMap;
        this.elements = MapBuilder.osmElements;

        this.excludedAttributes = new LinkedList<String>();
        this.excludedAttributes.add("created_by");
        this.excludedAttributes.add("user");
        this.excludedAttributes.add("timestamp");
        this.excludedAttributes.add("visible");

    }

    public void startNode(Attributes atts) {

        long  id  = Long.parseLong(atts.getValue("id"));
        float lat = Float.parseFloat(atts.getValue("lat"));
        float lon = Float.parseFloat(atts.getValue("lon"));

        int mercX = (int)((Mercator.mercX(lon)/1000)*(1<<16));
        int mercY = (int)((Mercator.mercY(lat)/1000)*(1<<16));

        this.currNode = new Node(id, mercX, mercY);
    }

    public void endNode() {

        this.nodeMap.put(currNode.id, currNode);

        String place = currNode.getAtrybut("place");
        if (place != null && (place.equals("city") ||
                place.equals("village") || place.equals("town"))) {

            String name = currNode.getAtrybut("name");
            if (name == null)
                return;

            this.cityMap.put(name, new Miasto(this.currNode.x, this.currNode.y, place));

            OsmElement element = new OsmElement(this.currNode.id);
            element.addAttribute("place", place);
            element.addAttribute("name", name);
            element.addNodeRef(this.currNode.id);

            this.elements.add(element);
        }

        currNode = null;
    }

    public void startWay(Attributes atts) {

        long id = Long.parseLong(atts.getValue("id"));
        this.currElement = new OsmElement(id);
    }

    public void endWay() {

        if (DrogaTyp.presets.get(currElement.getType()) != null) {
            this.elements.add(currElement);
        }

        currElement = null;
    }

    public void startNd(Attributes atts) {

        long nodeRef = Long.parseLong(atts.getValue("ref"));

        Node nd = (Node) nodeMap.get(nodeRef);
        if (nd != null) {
            nd.nalezyDoDroga();
        }

        currElement.addNodeRef(nodeRef);
    }

    public void endNd() {

    }

    public void startTag(Attributes atts) {

        String key = atts.getValue("k");
        String value = atts.getValue("v");

		/* don't waste memory for this attributes */
        if (this.excludedAttributes.contains(key))
            return;

		/* current processed element is a node */
        if (this.currNode != null) {
            this.currNode.addAtrybut(key, value);
        }
		/* current processed element is a way */
        else if (this.currElement != null) {
            this.currElement.addAttribute(key, value);
        }
    }

    public void endTag() {

    }

    public void startBound(Attributes atts) {

        int minY, minX, maxX, maxY;

        if (atts.getValue("minlat")!=null) {
            minY = (int)((Mercator.mercY(Double.parseDouble(atts.getValue("minlat")))/1000) * 65536);
            minX = (int)((Mercator.mercX(Double.parseDouble(atts.getValue("minlon")))/1000) * 65536);
            maxY = (int)((Mercator.mercY(Double.parseDouble(atts.getValue("maxlat")))/1000) * 65536);
            maxX = (int)((Mercator.mercX(Double.parseDouble(atts.getValue("maxlon")))/1000) * 65536);
        }

        else {

            String box      = atts.getValue("box");
            String[] coords = box.split(",");

            minY = (int)((Mercator.mercY(Double.parseDouble(coords[0]))/1000) * 65536);
            minX = (int)((Mercator.mercX(Double.parseDouble(coords[1]))/1000) * 65536);
            maxY = (int)((Mercator.mercY(Double.parseDouble(coords[2]))/1000) * 65536);
            maxX = (int)((Mercator.mercX(Double.parseDouble(coords[3]))/1000) * 65536);
        }

        MapBuilder.mapRegion.minX = minX;
        MapBuilder.mapRegion.minY = minY;
        MapBuilder.mapRegion.maxX = maxX;
        MapBuilder.mapRegion.maxY = maxY;

    }

	/* ContentHandler interface */

    public void startElement(String namespaceURI, String localName,
                             String rawName, Attributes atts) throws SAXException {

        if (localName.equals("node")) {
            startNode(atts);
        }
        else if (localName.equals("way")) {
            startWay(atts);
        }
        else if (localName.equals("nd")) {
            startNd(atts);
        }
        else if (localName.equals("tag")) {
            startTag(atts);
        }
        else if (localName.equals("bound") || localName.equals("bounds")) {
            startBound(atts);
        }
    }

    public void endElement(String namespaceURI, String localName, String rawName) {

        if (localName.equals("node")) {
            endNode();
        }
        else if (localName.equals("way")) {
            endWay();
        }
        else if (localName.equals("nd")) {
            endNd();
        }
        else if (localName.equals("tag")) {
            endTag();
        }
    }

    public void characters(char[] ch, int start, int end) throws SAXException {

    }

    @Override
    public void setDocumentLocator(Locator locator) {

    }

    @Override
    public void startDocument() throws SAXException {

    }

    @Override
    public void endDocument() throws SAXException {

    }

    @Override
    public void startPrefixMapping(String prefix, String uri) throws SAXException {

    }

    @Override
    public void endPrefixMapping(String prefix) throws SAXException {

    }

    @Override
    public void ignorableWhitespace(char[] ch, int start, int length) throws SAXException {

    }

    @Override
    public void processingInstruction(String target, String data) throws SAXException {

    }

    @Override
    public void skippedEntity(String name) throws SAXException {

    }
}
