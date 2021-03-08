package se.sigma.sallinggroup.mongodbmodel.entity;

import java.io.Serializable;
import java.util.List;

public class MultipleValue implements Serializable {

    private static final long serialVersionUID = -4860770157813572740L;
    public String attributeId;
    public List<SingleValue> values;

    public String getAttributeId() {
        return attributeId;
    }

    public void setAttributeId(String attributeId) {
        this.attributeId = attributeId;
    }

    public List<SingleValue> getValues() {
        return values;
    }

    public void setValues(List<SingleValue> values) {
        this.values = values;
    }

}