package se.sigma.sallinggroup.stepxml.entity;

import java.util.List;

public interface XmlValueContainer {
    public List<XmlProductAttributeValue> getValues();
    public void setValues(List<XmlProductAttributeValue> values);
}
