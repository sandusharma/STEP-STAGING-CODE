package se.sigma.sallinggroup.mongodbmodel.entity;

import java.util.List;

public class ReferenceGroup {

    public String referenceGroupId;
    public List<Reference> references;

    public String getReferenceGroupId() {
        return referenceGroupId;
    }

    public void setReferenceGroupId(String referenceGroupId) {
        this.referenceGroupId = referenceGroupId;
    }

    public List<Reference> getReferences() {
        return references;
    }

    public void setReferences(List<Reference> references) {
        this.references = references;
    }

}
