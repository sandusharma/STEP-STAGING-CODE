package se.sigma.sallinggroup.stepxml.parser;

import se.sigma.sallinggroup.stepxml.entity.XmlAttributeGroup;
import se.sigma.sallinggroup.stepxml.entity.XmlProductAttributeValue;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventReader;
import java.io.FileInputStream;
import java.util.*;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.events.XMLEvent;

@Deprecated
public class StreamingXmlAttributeGroupParser {

    private int AttributeGroupCounter = 0;

    public int getCounter() {
        return AttributeGroupCounter;
    }

    public List<XmlAttributeGroup> FindAll(String fileName) {

        List<XmlAttributeGroup> result = new ArrayList<XmlAttributeGroup>();

        XMLInputFactory xmlInputFactory = XMLInputFactory.newInstance();

        try {
            XMLEventReader xmlEventReader = xmlInputFactory.createXMLEventReader(new FileInputStream(fileName));

            while (xmlEventReader.hasNext()) {
                XMLEvent xmlEvent = xmlEventReader.nextEvent();

                if (xmlEvent.isStartElement() && xmlEvent.asStartElement().getName().getLocalPart().equals("AttributeGroup")) {
                    XmlAttributeGroup p = parseAttributeGroup(xmlEventReader, xmlEvent);
                    result.add(p);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return result;
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

                p.getChildren().add(parseAttributeGroup(xmlEventReader, current));
            }

            try {


                if (xmlEvent.isStartElement() && xmlEvent.asStartElement().getName().getLocalPart().equals("Name")) {
                    if (p.getName() == null) {
                        p.setName(new HashMap<String, String>());
                    }

                    String qualifierId = getAttributeValue(current, "QualifierID");
                    if (p.getName().get(qualifierId) == null) {
                        p.getName().put(qualifierId, getNodeValue(xmlEventReader));
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

    private void parseValue(XMLEventReader xmlEventReader, XMLEvent lastEvent, XmlAttributeGroup p) throws Exception {
        String elementName = lastEvent.asStartElement().getName().getLocalPart();

        XmlProductAttributeValue attributeValue = new XmlProductAttributeValue();
        attributeValue.setAttributeId(getAttributeValue(lastEvent, "AttributeID"));
        attributeValue.setQualifierId(getAttributeValue(lastEvent, "QualifierID"));
        attributeValue.setValueId(getAttributeValue(lastEvent, "ID"));

        if(attributeValue.getQualifierId() == null ) attributeValue.setQualifierId("");

        while( xmlEventReader.hasNext() ) {
            XMLEvent event = xmlEventReader.nextEvent();

            if( event.isCharacters() ) {
                attributeValue.setOrAppendValue(event.asCharacters().getData());
            }

            if( event.isEndElement() && event.asEndElement().getName().getLocalPart().equals(elementName)) {

                if( p.getValues() == null ) {
                    p.setValues(new ArrayList<XmlProductAttributeValue>());
                }
                p.getValues().add(attributeValue);

                return;
            }

        }
    }

    private void parseValueGroup(XMLEventReader xmlEventReader, XMLEvent lastEvent, XmlAttributeGroup p) throws Exception {

        final String attributeId =  getAttributeValue(lastEvent, "AttributeID");
        String elementName = lastEvent.asStartElement().getName().getLocalPart();
        while( xmlEventReader.hasNext()) {
            XMLEvent event = xmlEventReader.nextEvent();

            if( event.isStartElement() && event.asStartElement().getName().getLocalPart().equals("Value")) {
                XmlProductAttributeValue attributeValue =  findValueAttributes(attributeId, event);
                attributeValue.setValue(getNodeValue(xmlEventReader));

                if( p.getValues()==null) { p.setValues(new ArrayList<XmlProductAttributeValue>()); };

                if( p.getValues().stream().filter(x->x.getAttributeId().equals(attributeId) && x.getQualifierId().equals(attributeValue.getQualifierId())).count() == 0 ) {
                    p.getValues().add(attributeValue);
                }
            }

            if( event.isEndElement() && event.asEndElement().getName().getLocalPart().equals(elementName)) {
                return;
            }
        }
    }

    private void parseMultiValue(XMLEventReader xmlEventReader, XMLEvent lastEvent, XmlAttributeGroup p) throws Exception {
        final String attributeId =  getAttributeValue(lastEvent, "AttributeID");
        String elementName = lastEvent.asStartElement().getName().getLocalPart();
        while( xmlEventReader.hasNext()) {
            XMLEvent event = xmlEventReader.nextEvent();

            if( event.isStartElement() && event.asStartElement().getName().getLocalPart().equals("Value")) {

                XmlProductAttributeValue attributeValue =  findValueAttributes(attributeId, event);

                attributeValue.setMultiValue(new ArrayList<String>());
                attributeValue.setMultiValueIds(new ArrayList<String>());

                if(xmlEventReader.hasNext() && xmlEventReader.peek().isCharacters() ) {
                    attributeValue.getMultiValue().add(xmlEventReader.nextEvent().asCharacters().getData());
                }

                if(  attributeValue.getValueId() != null ) {
                    attributeValue.getMultiValueIds().add(attributeValue.getValueId());
                } else {
                    attributeValue.getMultiValueIds().add("");
                }

                if( p.getValues()==null) { p.setValues(new ArrayList<XmlProductAttributeValue>()); }

                if( p.getValues().stream().filter(x->x.getAttributeId().equals(attributeId) && x.getQualifierId().equals(attributeValue.getQualifierId())).count() == 0 ) {
                    p.getValues().add(attributeValue);
                } else {
                    XmlProductAttributeValue existingValue =  p.getValues().stream().filter(x->x.getAttributeId().equals(attributeId) && x.getQualifierId().equals(attributeValue.getQualifierId())).findFirst().get();
                    existingValue.getMultiValue().add(attributeValue.getMultiValue().get(0));
                    existingValue.getMultiValueIds().add(attributeValue.getMultiValueIds().get(0));
                }
            }

            if( event.isEndElement() && event.asEndElement().getName().getLocalPart().equals(elementName)) {
                return;
            }
        }
    }

    private XmlProductAttributeValue findValueAttributes(String attributeId, XMLEvent event) {
        XmlProductAttributeValue attributeValue = new XmlProductAttributeValue();
        attributeValue.setAttributeId(attributeId);
        attributeValue.setQualifierId(getAttributeValue(event, "QualifierID"));
        attributeValue.setValueId(getAttributeValue(event, "ID"));
        if( attributeValue.getQualifierId() == null ) attributeValue.setQualifierId("");

        return attributeValue;
    }

    private String getNodeValue(XMLEventReader xmlEventReader) throws Exception {
        if( xmlEventReader.hasNext() && xmlEventReader.peek().isCharacters()) {
            XMLEvent nextEvent = xmlEventReader.nextEvent();
            return nextEvent.asCharacters().getData();
        }
        return null;
    }

    private String getAttributeValue(XMLEvent event, String attributeName) {
        if( event.asStartElement().getAttributeByName(new QName(attributeName)) == null ) return null;
        return event.asStartElement().getAttributeByName(new QName(attributeName)).getValue();
    }

}
