package se.sigma.sallinggroup.stepxml.parser;

import se.sigma.sallinggroup.stepxml.entity.*;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.events.XMLEvent;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * The StreamingXmlGenericParser parses Attributes, AttributeGroups and Classification
 */

public class StreamingXmlGenericParser {

    private int classificationCounter = 0;

    public int getClassificationCounter() {
        return classificationCounter;
    }

    public void setClassificationCounter(int classificationCounter) {
        this.classificationCounter = classificationCounter;
    }

    public HashMap<String, List> FindAll(String fileName) {

        List<XmlStepAttribute> attributes = new ArrayList<XmlStepAttribute>();
        List<XmlAttributeGroup> attributeGroups = new ArrayList<XmlAttributeGroup>();
        List<XmlClassification> classifications = new ArrayList<XmlClassification>();
        List<StreamingXmlStepProduct> deletedProducts = new ArrayList<>();
        List<XmlAsset> assets = new ArrayList<XmlAsset>();
        List<XmlUnit> units = new ArrayList<XmlUnit>();

        HashMap<String, List> genericLists = new HashMap<>();

        XMLInputFactory xmlInputFactory = XMLInputFactory.newInstance();
        try {
            XMLEventReader xmlEventReader = xmlInputFactory.createXMLEventReader(new FileInputStream(fileName));

            while (xmlEventReader.hasNext()) {
                XMLEvent xmlEvent = xmlEventReader.nextEvent();


                if (xmlEvent.isStartElement() && xmlEvent.asStartElement().getName().getLocalPart().equals("Attribute")) {
                    XmlStepAttribute p = parseAttribute(xmlEventReader, xmlEvent);
                    attributes.add(p);
                }

                if (xmlEvent.isStartElement() && xmlEvent.asStartElement().getName().getLocalPart().equals("AttributeGroup")) {
                    XmlAttributeGroup p = parseAttributeGroup(xmlEventReader, xmlEvent);
                    attributeGroups.add(p);
                }

                if (xmlEvent.isStartElement() && xmlEvent.asStartElement().getName().getLocalPart().equals("Classification")) {
                    XmlClassification p = parseClassification(xmlEventReader, xmlEvent);
                    classifications.add(p);
                }

                if (xmlEvent.isStartElement() && xmlEvent.asStartElement().getName().getLocalPart().equals("Asset")) {
                    XmlAsset p = parseAsset(xmlEventReader, xmlEvent);
                    assets.add(p);
                }

                if (xmlEvent.isStartElement() && xmlEvent.asStartElement().getName().getLocalPart().equals("Unit")) {
                    XmlUnit p = parseUnit(xmlEventReader, xmlEvent);
                    units.add(p);
                }

            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        genericLists.put("attributes", attributes);
        genericLists.put("classifications", classifications);
        genericLists.put("attributegroups", attributeGroups);
        genericLists.put("assets",assets);
        genericLists.put("deletedProducts", deletedProducts);
        genericLists.put("units", units);

        return genericLists;

    }

    private XmlStepAttribute parseAttribute(XMLEventReader xmlEventReader, XMLEvent current) throws Exception {
        XmlStepAttribute p = new XmlStepAttribute();

        p.setId(getAttributeValue(current, "ID"));
        p.setMultiValue(getAttributeValue(current, "MultiValue"));

        while (xmlEventReader.hasNext()) {
            XMLEvent xmlEvent = xmlEventReader.nextEvent();

            if (xmlEvent.isEndElement() && xmlEvent.asEndElement().getName().getLocalPart().equals("Attribute")) {
                return p;
            }
            try {


                if (xmlEvent.isStartElement() && xmlEvent.asStartElement().getName().getLocalPart().equals("Name")) {
                    p.setName(getNodeValue(xmlEventReader));
                }

                if (xmlEvent.isStartElement() && xmlEvent.asStartElement().getName().getLocalPart().equals("MetaData")) {
                    while (xmlEventReader.hasNext()) {
                        XMLEvent xmlEvent1 = xmlEventReader.nextEvent();

                        if (xmlEvent1.isEndElement() && xmlEvent1.asEndElement().getName().getLocalPart().equals("MetaData")) {
                            break;
                        }

                        if (xmlEvent1.isStartElement() && xmlEvent1.asStartElement().getName().getLocalPart().equals("ValueGroup")) {
                            parseValueGroup(xmlEventReader, xmlEvent1, p);
                        }

                        if (xmlEvent1.isStartElement() && xmlEvent1.asStartElement().getName().getLocalPart().equals("MultiValue")) {
                            parseMultiValue(xmlEventReader, xmlEvent1, p);
                        }

                        if (xmlEvent1.isStartElement() && xmlEvent1.asStartElement().getName().getLocalPart().equals("Value")) {
                            parseValue(xmlEventReader, xmlEvent1, p);
                        }
                    }
                }

                if (xmlEvent.isStartElement() && xmlEvent.asStartElement().getName().getLocalPart().equals("AttributeGroupLink")) {
                    parseAttributeGroupLink(xmlEventReader, xmlEvent, p);
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return p;
    }

    private XmlAttributeGroup parseAttributeGroup(XMLEventReader xmlEventReader, XMLEvent current) throws Exception {
        XmlAttributeGroup p = new XmlAttributeGroup();

        p.setStepId(getAttributeValue(current, "ID"));

        while (xmlEventReader.hasNext()) {
            XMLEvent xmlEvent = xmlEventReader.nextEvent();

            if (xmlEvent.isEndElement() && xmlEvent.asEndElement().getName().getLocalPart().equals("AttributeGroup")) {
                return p;
            }

            if (xmlEvent.isStartElement() && xmlEvent.asStartElement().getName().getLocalPart().equals("AttributeGroup")) {
                if (p.children == null) {
                    p.setChildren(new ArrayList<XmlAttributeGroup>());
                }

                p.getChildren().add(parseAttributeGroup(xmlEventReader, xmlEvent));
            }

            try {


                if (xmlEvent.isStartElement() && xmlEvent.asStartElement().getName().getLocalPart().equals("Name")) {
                    if (p.getName() == null) {
                        p.setName(new HashMap<String, String>());
                    }

                    String qualifierId = getAttributeValue(current, "QualifierID");

                    if (p.getName().get(qualifierId) == null) {
                        if (qualifierId == null) {
                            String name = getNodeValue(xmlEventReader);
                            name = name.replaceAll("[0-9]+\\.","");
                            p.getName().put("DEFAULT", name);
                        } else {
                            String name = getNodeValue(xmlEventReader);
                            name = name.replaceAll("[0-9]+\\.","");
                            p.getName().put(qualifierId, name);
                        }
                    }
                }

                if (xmlEvent.isStartElement() && xmlEvent.asStartElement().getName().getLocalPart().equals("MetaData")) {
                    while (xmlEventReader.hasNext()) {
                        XMLEvent xmlEvent1 = xmlEventReader.nextEvent();

                        if (xmlEvent1.isEndElement() && xmlEvent1.asEndElement().getName().getLocalPart().equals("MetaData")) {
                            break;
                        }

                        if (xmlEvent1.isStartElement() && xmlEvent1.asStartElement().getName().getLocalPart().equals("ValueGroup")) {
                            parseValueGroup(xmlEventReader, xmlEvent1, (XmlValueContainer) p);
                        }

                        if (xmlEvent1.isStartElement() && xmlEvent1.asStartElement().getName().getLocalPart().equals("Value")) {
                            parseValue(xmlEventReader, xmlEvent1, p);
                        }

                    }
                }

                if (xmlEvent.isStartElement() && xmlEvent.asStartElement().getName().getLocalPart().equals("Values")) {
                    while (xmlEventReader.hasNext()) {
                        XMLEvent xmlEvent1 = xmlEventReader.nextEvent();

                        if (xmlEvent1.isEndElement() && xmlEvent1.asEndElement().getName().getLocalPart().equals("Values")) {
                            break;
                        }

                        if (xmlEvent1.isStartElement() && xmlEvent1.asStartElement().getName().getLocalPart().equals("ValueGroup")) {
                            parseValueGroup(xmlEventReader, xmlEvent1, (XmlValueContainer)p);
                        }

                        if (xmlEvent1.isStartElement() && xmlEvent1.asStartElement().getName().getLocalPart().equals("Value")) {
                            parseValue(xmlEventReader, xmlEvent1, p);
                        }
                    }
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return p;
    }

    //Classification
    private XmlClassification parseClassification(XMLEventReader xmlEventReader, XMLEvent current) throws Exception {
        XmlClassification p = new XmlClassification();

        p.setStepId(getAttributeValue(current, "ID"));
        p.setUserTypeId(getAttributeValue(current, "UserTypeID"));

        while (xmlEventReader.hasNext()) {
            XMLEvent xmlEvent = xmlEventReader.nextEvent();

            if (xmlEvent.isEndElement() && xmlEvent.asEndElement().getName().getLocalPart().equals("Classification")) {
                return p;
            }

            if (xmlEvent.isStartElement() && xmlEvent.asStartElement().getName().getLocalPart().equals("Classification")) {
                if (p.getChildren() == null) {
                    p.setChildren(new ArrayList<XmlClassification>());
                }

                p.getChildren().add(parseClassification(xmlEventReader, xmlEvent));
            }

            try {


                if (xmlEvent.isStartElement() && xmlEvent.asStartElement().getName().getLocalPart().equals("Name")) {
                    if (p.getName() == null) {
                        p.setName(new HashMap<String, String>());
                    }

                    String qualifierId = getAttributeValue(current, "QualifierID");
                    if (p.getName().get(qualifierId) == null) {
                        if (qualifierId == null) {
                            p.getName().put("DEFAULT", getNodeValue(xmlEventReader));
                        } else {
                            p.getName().put(qualifierId, getNodeValue(xmlEventReader));
                        }
                    }
                }

                if (xmlEvent.isStartElement() && xmlEvent.asStartElement().getName().getLocalPart().equals("MetaData")) {
                    while (xmlEventReader.hasNext()) {
                        XMLEvent xmlEvent1 = xmlEventReader.nextEvent();

                        if (xmlEvent1.isEndElement() && xmlEvent1.asEndElement().getName().getLocalPart().equals("MetaData")) {
                            break;
                        }

                        if (xmlEvent1.isStartElement() && xmlEvent1.asStartElement().getName().getLocalPart().equals("ValueGroup")) {
                            parseValueGroup(xmlEventReader, xmlEvent1, p);
                        }

                        if (xmlEvent1.isStartElement() && xmlEvent1.asStartElement().getName().getLocalPart().equals("Value")) {
                            parseValue(xmlEventReader, xmlEvent1, p);
                        }

                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return p;
    }

    private XmlUnit parseUnit(XMLEventReader xmlEventReader, XMLEvent current) throws Exception {
        XmlUnit p = new XmlUnit();

        p.setStepId(getAttributeValue(current, "ID"));

        while (xmlEventReader.hasNext()) {
            XMLEvent xmlEvent = xmlEventReader.nextEvent();

            if (xmlEvent.isEndElement() && xmlEvent.asEndElement().getName().getLocalPart().equals("Unit")) {
                return p;
            }
            try {
                if (xmlEvent.isStartElement() && xmlEvent.asStartElement().getName().getLocalPart().equals("Name")) {
                    p.setName(getNodeValue(xmlEventReader));
                }

                if (xmlEvent.isStartElement() && xmlEvent.asStartElement().getName().getLocalPart().equals("MetaData")) {
                    while (xmlEventReader.hasNext()) {
                        XMLEvent xmlEvent1 = xmlEventReader.nextEvent();

                        if (xmlEvent1.isEndElement() && xmlEvent1.asEndElement().getName().getLocalPart().equals("MetaData")) {
                            break;
                        }

                        if (xmlEvent1.isStartElement() && xmlEvent1.asStartElement().getName().getLocalPart().equals("ValueGroup")) {
                            parseValueGroup(xmlEventReader, xmlEvent1, p);
                        }

                        if (xmlEvent1.isStartElement() && xmlEvent1.asStartElement().getName().getLocalPart().equals("Value")) {
                            parseValue(xmlEventReader, xmlEvent1, p);
                        }
                    }
                }


            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return p;
    }
    //Classification
    private XmlAsset parseAsset(XMLEventReader xmlEventReader, XMLEvent current) throws Exception {
        XmlAsset p = new XmlAsset();

        p.setStepId(getAttributeValue(current, "ID"));
        p.setUserTypeId(getAttributeValue(current, "UserTypeID"));

        while (xmlEventReader.hasNext()) {
            XMLEvent xmlEvent = xmlEventReader.nextEvent();

            if (xmlEvent.isEndElement() && xmlEvent.asEndElement().getName().getLocalPart().equals("Asset")) {
                return p;
            }
            try {
                if (xmlEvent.isStartElement() && xmlEvent.asStartElement().getName().getLocalPart().equals("Name")) {
                    if (p.getName() == null) {
                        p.setName(new HashMap<String, String>());
                    }

                    String qualifierId = getAttributeValue(current, "QualifierID");
                    if (p.getName().get(qualifierId) == null) {
                        if (qualifierId == null) {
                            p.getName().put("DEFAULT", getNodeValue(xmlEventReader));
                        } else {
                            p.getName().put(qualifierId, getNodeValue(xmlEventReader));
                        }
                    }
                }

                if (xmlEvent.isStartElement() && xmlEvent.asStartElement().getName().getLocalPart().equals("Values")) {
                    while (xmlEventReader.hasNext()) {
                        XMLEvent xmlEvent1 = xmlEventReader.nextEvent();

                        if (xmlEvent1.isEndElement() && xmlEvent1.asEndElement().getName().getLocalPart().equals("Values")) {
                            break;
                        }

                        if (xmlEvent1.isStartElement() && xmlEvent1.asStartElement().getName().getLocalPart().equals("ValueGroup")) {
                            parseValueGroup(xmlEventReader, xmlEvent1, p);
                        }

                        if (xmlEvent1.isStartElement() && xmlEvent1.asStartElement().getName().getLocalPart().equals("Value")) {
                            parseValue(xmlEventReader, xmlEvent1, p);
                        }

                        if (xmlEvent1.isStartElement() && xmlEvent1.asStartElement().getName().getLocalPart().equals("MultiValue")) {
                            parseMultiValue(xmlEventReader, xmlEvent1, p);
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return p;
    }

    //Attribute
    private void parseValue(XMLEventReader xmlEventReader, XMLEvent lastEvent, XmlStepAttribute p) throws Exception {
        String elementName = lastEvent.asStartElement().getName().getLocalPart();

        XmlProductAttributeValue attributeValue = new XmlProductAttributeValue();
        attributeValue.setAttributeId(getAttributeValue(lastEvent, "AttributeID"));
        attributeValue.setQualifierId(getAttributeValue(lastEvent, "QualifierID"));
        attributeValue.setValueId(getAttributeValue(lastEvent, "ID"));

        if (attributeValue.getQualifierId() == null) attributeValue.setQualifierId("");

        while (xmlEventReader.hasNext()) {
            XMLEvent event = xmlEventReader.nextEvent();

            if (event.isCharacters()) {
                attributeValue.setOrAppendValue(event.asCharacters().getData());
            }

            if (event.isEndElement() && event.asEndElement().getName().getLocalPart().equals(elementName)) {

                if (p.getValues() == null) {
                    p.setValues(new ArrayList<XmlProductAttributeValue>());
                }
                p.getValues().add(attributeValue);

                return;
            }

        }
    }

    //Attribute Group
    private void parseValue(XMLEventReader xmlEventReader, XMLEvent lastEvent, XmlAttributeGroup p) throws Exception {
        String elementName = lastEvent.asStartElement().getName().getLocalPart();

        XmlProductAttributeValue attributeValue = new XmlProductAttributeValue();
        attributeValue.setAttributeId(getAttributeValue(lastEvent, "AttributeID"));
        attributeValue.setQualifierId(getAttributeValue(lastEvent, "QualifierID"));
        attributeValue.setValueId(getAttributeValue(lastEvent, "ID"));

        if (attributeValue.getQualifierId() == null) attributeValue.setQualifierId("");

        while (xmlEventReader.hasNext()) {
            XMLEvent event = xmlEventReader.nextEvent();

            if (event.isCharacters()) {
                attributeValue.setOrAppendValue(event.asCharacters().getData());
            }

            if (event.isEndElement() && event.asEndElement().getName().getLocalPart().equals(elementName)) {

                if (p.getValues() == null) {
                    p.setValues(new ArrayList<XmlProductAttributeValue>());
                }
                p.getValues().add(attributeValue);

                return;
            }

        }
    }

    //Classification
    private void parseValue(XMLEventReader xmlEventReader, XMLEvent lastEvent, XmlValueContainer p) throws Exception {
        String elementName = lastEvent.asStartElement().getName().getLocalPart();

        XmlProductAttributeValue attributeValue = new XmlProductAttributeValue();
        attributeValue.setAttributeId(getAttributeValue(lastEvent, "AttributeID"));
        attributeValue.setQualifierId(getAttributeValue(lastEvent, "QualifierID"));
        attributeValue.setValueId(getAttributeValue(lastEvent, "ID"));

        if (attributeValue.getQualifierId() == null) attributeValue.setQualifierId("");

        while (xmlEventReader.hasNext()) {
            XMLEvent event = xmlEventReader.nextEvent();

            if (event.isCharacters()) {
                attributeValue.setOrAppendValue(event.asCharacters().getData());
            }

            if (event.isEndElement() && event.asEndElement().getName().getLocalPart().equals(elementName)) {

                if (p.getValues() == null) {
                    p.setValues(new ArrayList<XmlProductAttributeValue>());
                }
                p.getValues().add(attributeValue);

                return;
            }

        }
    }

    //Attribute
    private void parseValueGroup(XMLEventReader xmlEventReader, XMLEvent lastEvent, XmlStepAttribute p) throws Exception {

        final String attributeId = getAttributeValue(lastEvent, "AttributeID");
        String elementName = lastEvent.asStartElement().getName().getLocalPart();
        while (xmlEventReader.hasNext()) {
            XMLEvent event = xmlEventReader.nextEvent();

            if (event.isStartElement() && event.asStartElement().getName().getLocalPart().equals("Value")) {
                XmlProductAttributeValue attributeValue = findValueAttributes(attributeId, event);
                attributeValue.setValue(getNodeValue(xmlEventReader));

                if (p.getValues() == null) {
                    p.setValues(new ArrayList<XmlProductAttributeValue>());
                }
                ;

                if (p.getValues().stream().filter(x -> x.getAttributeId().equals(attributeId) && x.getQualifierId().equals(attributeValue.getQualifierId())).count() == 0) {
                    p.getValues().add(attributeValue);
                }
            }

            if (event.isEndElement() && event.asEndElement().getName().getLocalPart().equals(elementName)) {
                return;
            }
        }
    }

    //Classification
    private void parseValueGroup(XMLEventReader xmlEventReader, XMLEvent lastEvent, XmlValueContainer p) throws Exception {

        final String attributeId = getAttributeValue(lastEvent, "AttributeID");
        String elementName = lastEvent.asStartElement().getName().getLocalPart();
        while (xmlEventReader.hasNext()) {
            XMLEvent event = xmlEventReader.nextEvent();

            if (event.isStartElement() && event.asStartElement().getName().getLocalPart().equals("Value")) {
                XmlProductAttributeValue attributeValue = findValueAttributes(attributeId, event);
                attributeValue.setValue(getNodeValue(xmlEventReader));

                if (p.getValues() == null) {
                    p.setValues(new ArrayList<XmlProductAttributeValue>());
                }
                ;

                if (p.getValues().stream().filter(x -> x.getAttributeId().equals(attributeId) && x.getQualifierId().equals(attributeValue.getQualifierId())).count() == 0) {
                    p.getValues().add(attributeValue);
                }
            }

            if (event.isEndElement() && event.asEndElement().getName().getLocalPart().equals(elementName)) {
                return;
            }
        }
    }

    //Attribute
    private void parseMultiValue(XMLEventReader xmlEventReader, XMLEvent lastEvent, XmlValueContainer p) throws Exception {
        final String attributeId = getAttributeValue(lastEvent, "AttributeID");
        String elementName = lastEvent.asStartElement().getName().getLocalPart();
        while (xmlEventReader.hasNext()) {
            XMLEvent event = xmlEventReader.nextEvent();

            if (event.isStartElement() && event.asStartElement().getName().getLocalPart().equals("Value")) {

                XmlProductAttributeValue attributeValue = findValueAttributes(attributeId, event);

                attributeValue.setMultiValue(new ArrayList<String>());

                if (xmlEventReader.hasNext() && xmlEventReader.peek().isCharacters()) {
                    attributeValue.getMultiValue().add(xmlEventReader.nextEvent().asCharacters().getData());
                }

                if (p.getValues() == null) {
                    p.setValues(new ArrayList<XmlProductAttributeValue>());
                }

                if (p.getValues().stream().filter(x -> x.getAttributeId().equals(attributeId) && x.getQualifierId().equals(attributeValue.getQualifierId())).count() == 0) {
                    p.getValues().add(attributeValue);
                } else {
                    XmlProductAttributeValue existingValue = p.getValues().stream().filter(x -> x.getAttributeId().equals(attributeId) && x.getQualifierId().equals(attributeValue.getQualifierId())).findFirst().get();
                    existingValue.getMultiValue().add(attributeValue.getMultiValue().get(0));
                }
            }

            if (event.isEndElement() && event.asEndElement().getName().getLocalPart().equals(elementName)) {
                return;
            }
        }
    }


    //Attribute
    private void parseAttributeGroupLink(XMLEventReader xmlEventReader, XMLEvent lastEvent, XmlStepAttribute p) throws Exception {
        String attributeGroupId = getAttributeValue(lastEvent, "AttributeGroupID");

        if ((p.getAttributeGroupIds() == null || p.getAttributeGroupIds().size() == 0)) {
            p.setAttributeGroupIds(new ArrayList<String>());
            p.getAttributeGroupIds().add(attributeGroupId);
        } else {
            p.getAttributeGroupIds().add(attributeGroupId);
        }
    }

    //Generic
    private XmlProductAttributeValue findValueAttributes(String attributeId, XMLEvent event) {
        XmlProductAttributeValue attributeValue = new XmlProductAttributeValue();
        attributeValue.setAttributeId(attributeId);
        attributeValue.setQualifierId(getAttributeValue(event, "QualifierID"));
        attributeValue.setValueId(getAttributeValue(event, "ID"));
        if (attributeValue.getQualifierId() == null) attributeValue.setQualifierId("");

        return attributeValue;
    }

    //Generic
    private String getNodeValue(XMLEventReader xmlEventReader) throws Exception {
        if (xmlEventReader.hasNext() && xmlEventReader.peek().isCharacters()) {
            XMLEvent nextEvent = xmlEventReader.nextEvent();
            return nextEvent.asCharacters().getData();
        }
        return null;
    }

    //Generic
    private String getAttributeValue(XMLEvent event, String attributeName) {
        if (event.asStartElement().getAttributeByName(new QName(attributeName)) == null) return null;
        return event.asStartElement().getAttributeByName(new QName(attributeName)).getValue();
    }

}
