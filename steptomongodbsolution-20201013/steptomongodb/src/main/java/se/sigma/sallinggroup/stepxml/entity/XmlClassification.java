package se.sigma.sallinggroup.stepxml.entity;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import se.sigma.sallinggroup.mongodbmodel.entity.Attribute;

public class XmlClassification implements XmlValueContainer {

    public String stepId;
    public String userTypeId;
    // Key=Qualifier Id, Value=Name
    public Map<String, String> name;
    public List<XmlProductAttributeValue> values;
    public List<XmlClassification> children;
 

    public String parentId;

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

    public List<XmlClassification> getChildren() {
        return children;
    }

    public void setChildren(List<XmlClassification> children) {
        this.children = children;
    }

    public String getParentId() {
        return parentId;
    }

    public void setParentId(String parentId) {
        this.parentId = parentId;
    }
    
 

    public XmlClassification search(XmlClassification cls, String stepId) {
        //System.out.println(cls.getStepId());
        if (cls.getStepId().equals(stepId)) {
            return cls;
        } else {
            if (cls.getChildren() != null && cls.getChildren().size() > 0) {
                for (XmlClassification child : cls.getChildren()) {
                    XmlClassification node = search(child, stepId);
                    if (node != null) {
                        return node;
                    }
                }
            }
        }
        return null;
    }

    public void flattenHierarchy(XmlClassification cls, HashMap<String, XmlClassification> flattened) {
        //System.out.println(cls.getStepId());
        flattened.put(cls.getStepId(), cls);
        if (cls.getChildren() != null) {
            for (XmlClassification cls1 : cls.getChildren()) {
                cls1.setParentId(cls.getStepId());
                flattenHierarchy(cls1, flattened);
            }
        }
    }

}
