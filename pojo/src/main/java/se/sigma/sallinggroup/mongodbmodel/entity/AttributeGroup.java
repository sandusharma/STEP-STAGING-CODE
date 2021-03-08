package se.sigma.sallinggroup.mongodbmodel.entity;

import java.io.Serializable;
import java.util.List;

public class AttributeGroup implements Serializable {
    private static final long serialVersionUID = 1088745263134324916L;

    public String attributeGroupId;
    public String name;
    public List<Attribute> attributes;

    public String getAttributeGroupId() {
        return attributeGroupId;
    }

    public void setAttributeGroupId(String attributeGroupId) {
        this.attributeGroupId = attributeGroupId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<Attribute> getAttributes() {
        return attributes;
    }

    public void setAttributes(List<Attribute> attributes) {
        this.attributes = attributes;
    }


}
