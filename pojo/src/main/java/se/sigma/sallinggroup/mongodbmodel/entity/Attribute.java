package se.sigma.sallinggroup.mongodbmodel.entity;

import java.util.List;
import java.io.Serializable;

public class Attribute implements Serializable {

    private static final long serialVersionUID = -7587878269229190167L;

    private String attributeId;
    private String name;
    //public String multiValue;
    private SingleValue value;
    private List<SingleValue> values;
    private List<Attribute> metadata;

    public String getAttributeId() {
        return attributeId;
    }

    public void setAttributeId(String attributeId) {
        this.attributeId = attributeId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    //public String getMultiValue() {
    //    return multiValue;
    //}

    //public void setMultiValue(String multiValue) {
    //    this.multiValue = multiValue;
    //}

    public SingleValue getValue() {
        return value;
    }

    public void setValue(SingleValue value) {
        this.value = value;
    }

    public List<SingleValue> getValues() {
        return values;
    }

    public void setValues(List<SingleValue> values) {
        this.values = values;
    }

    public List<Attribute> getMetadata() {
        return metadata;
    }

    public void setMetadata(List<Attribute> metadata) {
        this.metadata = metadata;
    }
}
