package se.sigma.sallinggroup.stepxml.entity;

import java.io.Serializable;
import java.util.List;

public class XmlProductAttributeValue implements Serializable {
    private String qualifierId;
    private String attributeId;
    private String value;
    private List<String> multiValue;
    private List<String> multiValueIds;
    private String valueId;
    private String unitId;

    public String getQualifierId() {
        return qualifierId;
    }

    public void setQualifierId(String qualifierId) {
        this.qualifierId = qualifierId;
    }

    public String getAttributeId() {
        return attributeId;
    }

    public void setAttributeId(String attributeId) {
        this.attributeId = attributeId;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
    public void setOrAppendValue(String value) {
        if( value == null ) this.value = null;
        if( this.value == null ) this.value = value;
        else this.value+=value;
    }

    public List<String> getMultiValueIds() {
        return multiValueIds;
    }

    public void setMultiValueIds(List<String> multiValueIds) {
        this.multiValueIds = multiValueIds;
    }

    public List<String> getMultiValue() {
        return multiValue;
    }

    public void setMultiValue(List<String> multiValue) {
        this.multiValue = multiValue;
    }

    public String getValueId() {
        return valueId;
    }

    public void setValueId(String valueId) {
        this.valueId = valueId;
    }

    public String getUnitId() {
        return unitId;
    }

    public void setUnitId(String unitId) {
        this.unitId = unitId;
    }
}
