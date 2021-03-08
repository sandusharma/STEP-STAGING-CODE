package se.sigma.sallinggroup.stepxml.parser;

import se.sigma.sallinggroup.stepxml.entity.*;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventReader;
import java.io.FileInputStream;
import java.util.*;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.XMLEvent;

public class StreamingXmlProductParser {

    private int classificationCounter = 0;


    public List<StreamingXmlStepProduct> FindAll(String fileName) {

        List<StreamingXmlStepProduct> result = new ArrayList<StreamingXmlStepProduct>();

        XMLInputFactory xmlInputFactory = XMLInputFactory.newInstance();
        try {
            XMLEventReader xmlEventReader = xmlInputFactory.createXMLEventReader(new FileInputStream(fileName));

            while (xmlEventReader.hasNext()) {
                XMLEvent xmlEvent = xmlEventReader.nextEvent();


                if (xmlEvent.isStartElement() && xmlEvent.asStartElement().getName().getLocalPart().equals("Product")) {
                    StreamingXmlStepProduct p = parseProduct(xmlEventReader, xmlEvent);
                    result.add(p);
                }

                if( xmlEvent.isStartElement() && xmlEvent.asStartElement().getName().getLocalPart().equals("DeleteProduct")) {
                    StreamingXmlStepProduct p = parseDeleteProduct(xmlEventReader, xmlEvent);
                    result.add(p);
                }

            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return result;

    }

    private StreamingXmlStepProduct parseDeleteProduct(XMLEventReader xmlEventReader, XMLEvent current) throws Exception {
        StreamingXmlStepProduct p = new StreamingXmlStepProduct();
        p.setStepId(getAttributeValue(current, "ID"));
        p.setDeleted(true);

        while (xmlEventReader.hasNext()) {
            XMLEvent xmlEvent = xmlEventReader.nextEvent();

            if (xmlEvent.isEndElement() && xmlEvent.asEndElement().getName().getLocalPart().equals("DeleteProduct")) {
                return p;
            }
        }

        return p;
    }

    private StreamingXmlStepProduct parseProduct(XMLEventReader xmlEventReader, XMLEvent current) throws Exception {
        StreamingXmlStepProduct p = new StreamingXmlStepProduct();

        p.setStepId(getAttributeValue(current, "ID"));
        p.setUserTypeId(getAttributeValue(current, "UserTypeID"));
        p.setParentId(getAttributeValue(current, "ParentID"));
        p.setDeleted(false);

        //if UserTypeID = ItemFamily... do your thing
        if (p.getUserTypeId().equals("ItemFamily")) {

            boolean itemFamilyNameHasSet = false;

            p.setVariantIds(new ArrayList<String>());

            boolean runOnce = false;

            String userTypeID = "";

            while (xmlEventReader.hasNext()) {
                XMLEvent xmlEvent = xmlEventReader.nextEvent();


                if (xmlEvent.isStartElement() && xmlEvent.asStartElement().getName().getLocalPart().equals("Name") && !itemFamilyNameHasSet) {
                    p.setName(getNodeValue(xmlEventReader));
                    itemFamilyNameHasSet = true;
                }

                if (xmlEvent.isStartElement() && xmlEvent.asStartElement().getName().getLocalPart().equals("Values") && !runOnce) {
                    parseValues(xmlEventReader, p);
                    runOnce = true;
                }

                if (xmlEvent.isStartElement() && xmlEvent.asStartElement().getName().getLocalPart().equals("Product") && (getAttributeValue(xmlEvent, "UserTypeID").equals("GoldenRecord"))) {
                    String id = getAttributeValue(xmlEvent, "ID");
                    p.getVariantIds().add(id);
                    userTypeID = getAttributeValue(xmlEvent, "UserTypeID");
                    continue;
                }

                if (xmlEvent.isStartElement() && xmlEvent.asStartElement().getName().getLocalPart().equals("Product") && (getAttributeValue(xmlEvent, "UserTypeID").equals("SilverRecord") || getAttributeValue(xmlEvent, "UserTypeID").equals("DataSourceItem"))) {
                    userTypeID = getAttributeValue(xmlEvent, "UserTypeID");
                    continue;
                }

                if (xmlEvent.isEndElement() && xmlEvent.asEndElement().getName().getLocalPart().equals("Product") && (userTypeID.equals("GoldenRecord") || userTypeID.equals("SilverRecord") || userTypeID.equals("DataSourceItem"))) {
                    userTypeID = "";
                    continue;
                } else if (xmlEvent.isEndElement() && xmlEvent.asEndElement().getName().getLocalPart().equals("Product") && userTypeID.equals("")) {
                    return p;
                }
            }
        }

        while (xmlEventReader.hasNext()) {
            XMLEvent xmlEvent = xmlEventReader.nextEvent();

            if (xmlEvent.isEndElement() && xmlEvent.asEndElement().getName().getLocalPart().equals("Product")) {
                return p;
            }

            if (xmlEvent.isStartElement() && xmlEvent.asStartElement().getName().getLocalPart().equals("Name")) {
                p.setName(getNodeValue(xmlEventReader));
            }

            if (xmlEvent.isStartElement() && xmlEvent.asStartElement().getName().getLocalPart().equals("ClassificationReference")) {
                parseClassificationReference(xmlEventReader, xmlEvent, p);
            }

            if (xmlEvent.isStartElement() && xmlEvent.asStartElement().getName().getLocalPart().equals("AssetCrossReference")) {
                parseAssetReferences(xmlEventReader, xmlEvent, p);
            }

            if (xmlEvent.isStartElement() && xmlEvent.asStartElement().getName().getLocalPart().equals("EntityCrossReference")) {
                parseEntityReferences(xmlEventReader, xmlEvent, p);
            }

            if (xmlEvent.isStartElement() && xmlEvent.asStartElement().getName().getLocalPart().equals("ProductCrossReference")) {
                parseProductCrossReference(xmlEventReader, xmlEvent, p);
            }

            if (xmlEvent.isStartElement() && xmlEvent.asStartElement().getName().getLocalPart().equals("Values")) {
                parseValues(xmlEventReader, p);
            }
        }
        return p;
    }

    private StreamingXmlStepProduct parseVariants(XMLEventReader xmlEventReader, XMLEvent current) throws Exception {
        return null;
    }

    private void parseValues(XMLEventReader xmlEventReader, StreamingXmlStepProduct p) throws Exception {

        while (xmlEventReader.hasNext()) {
            XMLEvent event = xmlEventReader.nextEvent();

            if (event.isEndElement() && event.asEndElement().getName().getLocalPart().equals("Values")) {
                return;
            }

            if (event.isStartElement() && event.asStartElement().getName().getLocalPart().equals("ValueGroup")) {
                parseValueGroup(xmlEventReader, event, p);
            }
            if (event.isStartElement() && event.asStartElement().getName().getLocalPart().equals("MultiValue")) {
                parseMultiValue(xmlEventReader, event, p);
            }

            if (event.isStartElement() && event.asStartElement().getName().getLocalPart().equals("Value")) {
                parseValue(xmlEventReader, event, p);
            }


        }
    }

    private void parseValue(XMLEventReader xmlEventReader, XMLEvent lastEvent, StreamingXmlStepProduct p) throws Exception {
        String elementName = lastEvent.asStartElement().getName().getLocalPart();
        XmlProductAttributeValue attributeValue = new XmlProductAttributeValue();

        attributeValue.setAttributeId(getAttributeValue(lastEvent, "AttributeID"));
        attributeValue.setQualifierId(getAttributeValue(lastEvent, "QualifierID"));
        attributeValue.setValueId(getAttributeValue(lastEvent, "ID"));
        attributeValue.setUnitId(getAttributeValue(lastEvent, "UnitID"));

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

    private void parseValueGroup(XMLEventReader xmlEventReader, XMLEvent lastEvent, StreamingXmlStepProduct p) throws Exception {

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


    private void parseMultiValue(XMLEventReader xmlEventReader, XMLEvent lastEvent, StreamingXmlStepProduct p) throws Exception {
        final String attributeId = getAttributeValue(lastEvent, "AttributeID");


        String elementName = lastEvent.asStartElement().getName().getLocalPart();
        while (xmlEventReader.hasNext()) {
            XMLEvent event = xmlEventReader.nextEvent();

            if (event.isStartElement() && event.asStartElement().getName().getLocalPart().equals("Value")) {

                XmlProductAttributeValue attributeValue = findValueAttributes(attributeId, event);
                attributeValue.setMultiValue(new ArrayList<String>());
                attributeValue.setMultiValueIds(new ArrayList<String>());

                attributeValue.getMultiValue().add(getNodeValue(xmlEventReader));

                if(attributeValue.getValueId() != null ) {
                    attributeValue.getMultiValueIds().add(attributeValue.getValueId());
                } else {
                    attributeValue.getMultiValueIds().add("");
                }

                if (p.getValues() == null) {
                    p.setValues(new ArrayList<XmlProductAttributeValue>());
                }

                if (p.getValues().stream().filter(x -> x.getAttributeId().equals(attributeId) && x.getQualifierId().equals(attributeValue.getQualifierId())).count() == 0) {
                    p.getValues().add(attributeValue);
                } else {
                    XmlProductAttributeValue existingValue = p.getValues().stream().filter(x -> x.getAttributeId().equals(attributeId) && x.getQualifierId().equals(attributeValue.getQualifierId())).findFirst().get();
                    existingValue.getMultiValue().add(attributeValue.getMultiValue().get(0));
                    existingValue.getMultiValueIds().add(attributeValue.getMultiValueIds().get(0));
                }
            }

            if (event.isEndElement() && event.asEndElement().getName().getLocalPart().equals(elementName)) {
                return;
            }
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

    private String getNodeValue(XMLEventReader reader) throws Exception {
        String result = "";
        if( reader.hasNext() && !reader.peek().isCharacters() ) return null;

        while( reader.hasNext() && reader.peek().isCharacters()) {
            result += reader.nextEvent().asCharacters().getData();
        }
        return result;
    }

    private String getAttributeValue(XMLEvent event, String attributeName) {
        if (event.asStartElement().getAttributeByName(new QName(attributeName)) == null) return null;
        return event.asStartElement().getAttributeByName(new QName(attributeName)).getValue();
    }

    private void parseClassificationReference(XMLEventReader xmlEventReader, XMLEvent lastEvent, StreamingXmlStepProduct p) throws Exception {

        XmlClassificationLink classificationLink = new XmlClassificationLink();

        classificationCounter++;

        classificationLink.setClassificationId(getAttributeValue(lastEvent, "ClassificationID"));
        classificationLink.setType(getAttributeValue(lastEvent, "Type"));
        classificationLink.setQualifierId(getAttributeValue(lastEvent, "QualifierID"));
        classificationLink.setMetadata(new ArrayList<XmlProductAttributeValue>());

        if (p.getClassificationLinks() == null) p.setClassificationLinks(new ArrayList<XmlClassificationLink>());
        p.getClassificationLinks().add(classificationLink);

        while (xmlEventReader.hasNext()) {
            XMLEvent event = xmlEventReader.nextEvent();

            if (event.isEndElement() && event.asEndElement().getName().getLocalPart().equals("ClassificationReference"))
                return;

            if (event.isStartElement() && event.asStartElement().getName().getLocalPart().equals("MetaData")) {
                parseReferenceMetaData(xmlEventReader, classificationLink);
            }
        }
    }

    private void parseReferenceMetaData(XMLEventReader xmlEventReader, XmlAbstractReference classificationLink) throws Exception {
        while (xmlEventReader.hasNext()) {
            XMLEvent event = xmlEventReader.nextEvent();

            if (event.isEndElement() && event.asEndElement().getName().getLocalPart().equals("MetaData")) break;

            if (event.isStartElement() && event.asStartElement().getName().getLocalPart().equals("ValueGroup")) {
                XmlProductAttributeValue metaDataValue = new XmlProductAttributeValue();
                metaDataValue.setAttributeId(getAttributeValue(event, "AttributeID"));
                metaDataValue.setUnitId(getAttributeValue(event, "UnitID"));
                metaDataValue.setValueId(getAttributeValue(event, "ID"));

                metaDataValue.setValue("");

                classificationLink.getMetadata().add(metaDataValue);

                while (xmlEventReader.hasNext()) {
                    XMLEvent event1 = xmlEventReader.nextEvent();
                    if (event1.isEndElement() && event1.asEndElement().getName().getLocalPart().equals("ValueGroup"))
                        break;
                }
            }

            if (event.isStartElement() && event.asStartElement().getName().getLocalPart().equals("Value")) {
                XmlProductAttributeValue metaDataValue = new XmlProductAttributeValue();

                metaDataValue.setAttributeId(getAttributeValue(event, "AttributeID"));
                metaDataValue.setUnitId(getAttributeValue(event, "UnitID"));
                metaDataValue.setValueId(getAttributeValue(event, "ID"));

                metaDataValue.setValue(getNodeValue(xmlEventReader));

                classificationLink.getMetadata().add(metaDataValue);
            }

        }
    }

    private void parseAssetReferences(XMLEventReader xmlEventReader, XMLEvent lastEvent, StreamingXmlStepProduct p) throws Exception {
        XmlAssetReference assetReference = new XmlAssetReference();

        assetReference.setAssetId(getAttributeValue(lastEvent, "AssetID"));
        assetReference.setType(getAttributeValue(lastEvent, "Type"));
        assetReference.setQualifierId(getAttributeValue(lastEvent, "QualifierID"));
        assetReference.setMetadata(new ArrayList<XmlProductAttributeValue>());

        if (p.getAssetReferences() == null) p.setAssetReferences(new ArrayList<XmlAssetReference>());
        p.getAssetReferences().add(assetReference);

        while (xmlEventReader.hasNext()) {
            XMLEvent event = xmlEventReader.nextEvent();

            if (event.isEndElement() && event.asEndElement().getName().getLocalPart().equals("AssetCrossReference"))
                return;

            if (event.isStartElement() && event.asStartElement().getName().getLocalPart().equals("MetaData")) {
                parseReferenceMetaData(xmlEventReader, assetReference);
            }
        }
    }

    private void parseEntityReferences(XMLEventReader xmlEventReader, XMLEvent lastEvent, StreamingXmlStepProduct p) throws Exception {
        XmlStepEntityReference entityReference = new XmlStepEntityReference();

        entityReference.setEntityId(getAttributeValue(lastEvent, "EntityID"));
        entityReference.setType(getAttributeValue(lastEvent, "Type"));
        entityReference.setQualifierId(getAttributeValue(lastEvent, "QualifierID"));
        entityReference.setMetadata(new ArrayList<XmlProductAttributeValue>());

        if (p.getEntityReferences() == null) p.setEntityReferences(new ArrayList<XmlStepEntityReference>());
        p.getEntityReferences().add(entityReference);

        while (xmlEventReader.hasNext()) {
            XMLEvent event = xmlEventReader.nextEvent();

            if (event.isEndElement() && event.asEndElement().getName().getLocalPart().equals("EntityCrossReference"))
                return;

            if (event.isStartElement() && event.asStartElement().getName().getLocalPart().equals("MetaData")) {
                parseReferenceMetaData(xmlEventReader, entityReference);
            }
        }
    }

    private void parseProductCrossReference(XMLEventReader xmlEventReader, XMLEvent lastEvent, StreamingXmlStepProduct p) throws Exception {
        XmlProductCrossReference productReference = new XmlProductCrossReference();

        productReference.setProductid(getAttributeValue(lastEvent, "ProductID"));
        productReference.setType(getAttributeValue(lastEvent, "Type"));
        productReference.setQualifierId(getAttributeValue(lastEvent, "QualifierID"));
        productReference.setMetadata(new ArrayList<XmlProductAttributeValue>());

        if (p.getProductCrossReferences() == null)
            p.setProductCrossReferences(new ArrayList<XmlProductCrossReference>());
        p.getProductCrossReferences().add(productReference);

        while (xmlEventReader.hasNext()) {
            XMLEvent event = xmlEventReader.nextEvent();

            if (event.isEndElement() && event.asEndElement().getName().getLocalPart().equals("ProductCrossReference"))
                return;

            if (event.isStartElement() && event.asStartElement().getName().getLocalPart().equals("MetaData")) {
                parseReferenceMetaData(xmlEventReader, productReference);
            }
        }
    }
}
