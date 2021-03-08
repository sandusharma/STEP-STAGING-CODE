package se.sigma.sallinggroup.stepxml.entity;

import java.util.List;
import java.util.Map;

public class XmlAttributeGroup {

    public String stepId;
    public String userTypeId;
    //Key=Qualifier Id, Value=Name
    public Map<String, String> name;
    public List<XmlProductAttributeValue> values;
    public List<XmlAttributeGroup> children;

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

    public List<XmlAttributeGroup> getChildren() {
        return children;
    }

    public void setChildren(List<XmlAttributeGroup> children) {
        this.children = children;
    }

    public XmlAttributeGroup search(XmlAttributeGroup cls, String stepId) {
        //System.out.println(cls.getStepId());
        if (cls.getStepId().equals(stepId)) {
            return cls;
        } else {
            if (cls.getChildren() != null && cls.getChildren().size() > 0) {
                for (XmlAttributeGroup child : cls.getChildren()) {
                    XmlAttributeGroup node = search(child, stepId);
                    if (node != null) {
                        return node;
                    }
                }
            }
        }
        return null;
    }
}
