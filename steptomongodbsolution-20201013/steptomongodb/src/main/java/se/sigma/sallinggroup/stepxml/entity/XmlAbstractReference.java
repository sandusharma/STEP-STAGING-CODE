package se.sigma.sallinggroup.stepxml.entity;

import java.util.List;

public abstract class XmlAbstractReference {

    public List<XmlProductAttributeValue> metadata;
    public String qualifierId;
    public String type;

    public List<XmlProductAttributeValue> getMetadata() {
        return metadata;
    }

    public void setMetadata(List<XmlProductAttributeValue> metadata) {
        this.metadata = metadata;
    }

    public String getQualifierId() {
        return qualifierId;
    }

    public void setQualifierId(String qualifierId) {
        this.qualifierId = qualifierId;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
