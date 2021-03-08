package se.sigma.sallinggroup.stepxml.entity;

import se.sigma.sallinggroup.mongodbmodel.entity.Attribute;

import java.util.List;

public class XmlUnit implements XmlValueContainer {
    private String stepId;
    private String name;
    private List<XmlProductAttributeValue> values;

    public String getStepId() {
        return stepId;
    }

    public void setStepId(String id) {
        this.stepId = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<XmlProductAttributeValue> getValues() {
        return values;
    }

    public void setValues(List<XmlProductAttributeValue> values) {
        this.values = values;
    }
}
