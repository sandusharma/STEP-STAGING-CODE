package se.sigma.sallinggroup.stepxml.parser;

import se.sigma.sallinggroup.stepxml.entity.XmlProductAttributeValue;
import se.sigma.sallinggroup.stepxml.entity.XmlStepAttribute;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventReader;
import java.io.FileInputStream;
import java.util.*;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.events.XMLEvent;

@Deprecated
public class StreamingXmlAttributeParser {

    private int classificationCounter = 0;

    public int getClassificationCounter() {
        return classificationCounter;
    }

    public void setClassificationCounter(int classificationCounter) {
        this.classificationCounter = classificationCounter;
    }

    public List<XmlStepAttribute> FindAll(String fileName) {

        List<XmlStepAttribute> result = new ArrayList<XmlStepAttribute>();

        XMLInputFactory xmlInputFactory = XMLInputFactory.newInstance();
        try {
            XMLEventReader xmlEventReader = xmlInputFactory.createXMLEventReader(new FileInputStream(fileName));

            while (xmlEventReader.hasNext()) {
                XMLEvent xmlEvent = xmlEventReader.nextEvent();


                if (xmlEvent.isStartElement() && xmlEvent.asStartElement().getName().getLocalPart().equals("Attribute")) {
                    XmlStepAttribute p = parseAttribute(xmlEventReader, xmlEvent);
                    result.add(p);
                }


            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return result;

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

    private void parseMultiValue(XMLEventReader xmlEventReader, XMLEvent lastEvent, XmlStepAttribute p) throws Exception {
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

    private void parseAttributeGroupLink (XMLEventReader xmlEventReader, XMLEvent lastEvent, XmlStepAttribute p) throws Exception {
        String attributeGroupId = getAttributeValue(lastEvent, "AttributeGroupID");

        if(!(p.getAttributeGroupIds().size() > 0) || p.getAttributeGroupIds() == null) {
            p.setAttributeGroupIds(new ArrayList<String>());
            p.getAttributeGroupIds().add(attributeGroupId);
        } else {
            if(p.getAttributeGroupIds().add(attributeGroupId));
        }
    }

    private XmlProductAttributeValue findValueAttributes(String attributeId, XMLEvent event) {
        XmlProductAttributeValue attributeValue = new XmlProductAttributeValue();
        attributeValue.setAttributeId(attributeId);
        attributeValue.setQualifierId(getAttributeValue(event, "QualifierID"));
        attributeValue.setValueId(getAttributeValue(event, "ID"));
        if (attributeValue.getQualifierId() == null) attributeValue.setQualifierId("");

        return attributeValue;
    }

    private String getNodeValue(XMLEventReader xmlEventReader) throws Exception {
        if (xmlEventReader.hasNext() && xmlEventReader.peek().isCharacters()) {
            XMLEvent nextEvent = xmlEventReader.nextEvent();
            return nextEvent.asCharacters().getData();
        }
        return null;
    }

    private String getAttributeValue(XMLEvent event, String attributeName) {
        if (event.asStartElement().getAttributeByName(new QName(attributeName)) == null) return null;
        return event.asStartElement().getAttributeByName(new QName(attributeName)).getValue();
    }

}
