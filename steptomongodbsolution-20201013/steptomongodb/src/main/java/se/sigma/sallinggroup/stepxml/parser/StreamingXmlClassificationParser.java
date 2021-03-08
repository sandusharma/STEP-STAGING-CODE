package se.sigma.sallinggroup.stepxml.parser;

import se.sigma.sallinggroup.stepxml.entity.XmlClassification;
import se.sigma.sallinggroup.stepxml.entity.XmlProductAttributeValue;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventReader;
import java.io.FileInputStream;
import java.util.*;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.events.XMLEvent;

@Deprecated
public class StreamingXmlClassificationParser {
    private int classificationCounter = 0;

    public int getCounter() {
        return classificationCounter;
    }

    public List<XmlClassification> FindAll(String fileName) {

        List<XmlClassification> result = new ArrayList<XmlClassification>();

        XMLInputFactory xmlInputFactory = XMLInputFactory.newInstance();

        try {
            XMLEventReader xmlEventReader = xmlInputFactory.createXMLEventReader(new FileInputStream(fileName));

            while (xmlEventReader.hasNext()) {
                XMLEvent xmlEvent = xmlEventReader.nextEvent();

                if (xmlEvent.isStartElement() && xmlEvent.asStartElement().getName().getLocalPart().equals("Classification")) {
                    XmlClassification p = parseClassification(xmlEventReader, xmlEvent);
                    result.add(p);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return result;
    }

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
                if (p.children == null) {
                    p.setChildren(new ArrayList<XmlClassification>());
                }

                p.getChildren().add(parseClassification(xmlEventReader, current));
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

    private void parseValue(XMLEventReader xmlEventReader, XMLEvent lastEvent, XmlClassification p) throws Exception {
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

    private void parseValueGroup(XMLEventReader xmlEventReader, XMLEvent lastEvent, XmlClassification p) throws Exception {

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

    private void parseMultiValue(XMLEventReader xmlEventReader, XMLEvent lastEvent, XmlClassification p) throws Exception {
        final String attributeId =  getAttributeValue(lastEvent, "AttributeID");
        String elementName = lastEvent.asStartElement().getName().getLocalPart();
        while( xmlEventReader.hasNext()) {
            XMLEvent event = xmlEventReader.nextEvent();

            if( event.isStartElement() && event.asStartElement().getName().getLocalPart().equals("Value")) {

                XmlProductAttributeValue attributeValue =  findValueAttributes(attributeId, event);

                attributeValue.setMultiValue(new ArrayList<String>());

                if(xmlEventReader.hasNext() && xmlEventReader.peek().isCharacters() ) {
                    attributeValue.getMultiValue().add(xmlEventReader.nextEvent().asCharacters().getData()) ;
                }

                if( p.getValues()==null) { p.setValues(new ArrayList<XmlProductAttributeValue>()); }

                if( p.getValues().stream().filter(x->x.getAttributeId().equals(attributeId) && x.getQualifierId().equals(attributeValue.getQualifierId())).count() == 0 ) {
                    p.getValues().add(attributeValue);
                } else {
                    XmlProductAttributeValue existingValue =  p.getValues().stream().filter(x->x.getAttributeId().equals(attributeId) && x.getQualifierId().equals(attributeValue.getQualifierId())).findFirst().get();
                    existingValue.getMultiValue().add(attributeValue.getMultiValue().get(0));
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
        String result = null;
        while( xmlEventReader.hasNext() && xmlEventReader.peek().isCharacters()) {
            XMLEvent nextEvent = xmlEventReader.nextEvent();
            String characters =  nextEvent.asCharacters().getData();
            if( result == null ) {
                result = characters;
            } else {
                result += characters;
            }
        }
        return result;
    }

    private String getAttributeValue(XMLEvent event, String attributeName) {
        if( event.asStartElement().getAttributeByName(new QName(attributeName)) == null ) return null;
        return event.asStartElement().getAttributeByName(new QName(attributeName)).getValue();
    }
}
