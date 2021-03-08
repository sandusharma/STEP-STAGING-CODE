package se.sigma.sallinggroup.mongodbmodel.entity;

import java.util.List;

public class Reference {

    public String targetId;
    public List<Attribute> metadata;

    public String getTargetId() {
        return targetId;
    }

    public void setTargetId(String targetId) {
        this.targetId = targetId;
    }

    public List<Attribute> getMetadata() {
        return metadata;
    }

    public void setMetadata(List<Attribute> metadata) {
        this.metadata = metadata;
    }

}
