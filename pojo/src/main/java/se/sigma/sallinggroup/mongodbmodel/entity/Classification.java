package se.sigma.sallinggroup.mongodbmodel.entity;

import java.util.List;

public class Classification {

    public String targetId;
    public String typeId;
    public String targetName;
    public List<Attribute> metadata;

    public String getTargetId() {
        return this.targetId;
    }

    public void setTargetId(String targetId) {
        this.targetId = targetId;
    }

    public String getTypeId() {
        return typeId;
    }

    public void setTypeId(String typeId) {
        this.typeId = typeId;
    }

    public String getTargetName() {
        return targetName;
    }

    public void setTargetName(String targetName) {
        this.targetName = targetName;
    }

    public List<Attribute> getMetadata() {
        return metadata;
    }
    public void setMetadata(List<Attribute> metadata) {
        this.metadata = metadata;
    }

}
