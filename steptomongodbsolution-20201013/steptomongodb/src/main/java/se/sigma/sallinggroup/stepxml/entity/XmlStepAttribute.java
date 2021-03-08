package se.sigma.sallinggroup.stepxml.entity;

import se.sigma.sallinggroup.mongodbmodel.entity.Attribute;

import java.util.List;

public class XmlStepAttribute implements XmlValueContainer {
    public String id;
    public String name;
    public String multiValue;
    public List<XmlProductAttributeValue> values;
    public List<String> attributeGroupIds;
    public List<Attribute> metadata;


    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getMultiValue() {
        return multiValue;
    }

    public void setMultiValue(String multiValue) {
        this.multiValue = multiValue;
    }

    public List<XmlProductAttributeValue> getValues() {
        return values;
    }

    public void setValues(List<XmlProductAttributeValue> values) {
        this.values = values;
    }

    public List<String> getAttributeGroupIds() {
        return attributeGroupIds;
    }

    public void setAttributeGroupIds(List<String> attributeGroupIds) {
        this.attributeGroupIds = attributeGroupIds;
    }

    public List<Attribute> getMetadata() {
        return metadata;
    }

    public void setMetadata(List<Attribute> metadata) {
        this.metadata = metadata;
    }
}
