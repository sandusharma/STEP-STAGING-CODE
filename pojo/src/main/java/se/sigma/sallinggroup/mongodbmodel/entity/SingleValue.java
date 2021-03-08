package se.sigma.sallinggroup.mongodbmodel.entity;

import java.io.Serializable;
import java.lang.Thread;
import java.util.List;

public class SingleValue implements Serializable {
    private static final long serialVersionUID = -1499833541118615699L;

    private String valueId;
    private String unitId;
    private String unit;
    private String value;
    private List<String> multiValue;

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

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }
    public List<String> getMultiValue() {
        return multiValue;
    }

    public void setMultiValue(List<String> multiValue) {
        this.multiValue = multiValue;
    }
}
