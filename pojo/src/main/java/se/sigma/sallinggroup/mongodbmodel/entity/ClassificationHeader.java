package se.sigma.sallinggroup.mongodbmodel.entity;

import com.fasterxml.jackson.annotation.JsonProperty;



import java.io.Serializable;
import java.util.Date;
import java.util.List;
import java.lang.Thread;


public class ClassificationHeader implements Serializable {


    private static final long serialVersionUID = 3451266872526946713L;

    public String stepId;
    //public String contextId;
    public String name;
    public long serial;
    public String updated;
    public String objectType;
  //sandeep added
   public List<Attribute> metadata; 
    public List<Attribute> values;
  
    
  

    public String parentId;
    public String getStepId() {
        return stepId;
    }

    @JsonProperty("step_id")
    public void setStepId(String stepId) {
        this.stepId = stepId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public long getSerial() {
        return serial;
    }

    public void setSerial(long serial) {
        this.serial = serial;
    }

    public String getUpdated() {
        return updated;
    }

    public void setUpdated(String updated) {
        this.updated = updated;
    }

    public String getObjectType() {
        return objectType;
    }

    public void setObjectType(String objectType) {
        this.objectType = objectType;
    }

    public String getParentId() {
        return parentId;
    }

    public void setParentId(String parentId) {
        this.parentId = parentId;
    }
    //sandeep add start
    public List<Attribute> getValues() {
        return values;
    }

    public void setValues(List<Attribute> values) {
        this.values = values;
    }
    public List<Attribute> getMetadata() {
        return metadata;
    }

    public void setMetadata(List<Attribute> metadata) {
        this.metadata = metadata;
    }
    //sandeep add end

}
