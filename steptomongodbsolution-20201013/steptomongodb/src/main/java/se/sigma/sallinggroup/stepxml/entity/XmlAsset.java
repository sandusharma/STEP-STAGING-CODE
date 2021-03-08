package se.sigma.sallinggroup.stepxml.entity;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class XmlAsset implements XmlValueContainer {

    public String stepId;
    public String userTypeId;
    // Key=Qualifier Id, Value=Name
    public Map<String, String> name;
    public List<XmlProductAttributeValue> values;

    public String getStepId() {
        return stepId;
    }

    public void setStepId(String stepId) {
        this.stepId = stepId;
    }

    public String getUserTypeId() {
        return userTypeId;
    }

    public void setUserTypeId(String userTypeId) {
        this.userTypeId = userTypeId;
    }

    public Map<String, String> getName() {
        return name;
    }

    public void setName(Map<String, String> name) {
        this.name = name;
    }

    public List<XmlProductAttributeValue> getValues() {
        return values;
    }

    public void setValues(List<XmlProductAttributeValue> values) {
        this.values = values;
    }

}
