package se.sigma.sallinggroup.mongodbmodel.entity;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;
import java.util.Date;
import java.util.List;
import java.lang.Thread;

public class Header implements Serializable {
    private static final long serialVersionUID = 6422293562918946889L;

    private String stepId;
    private String name;
    private long serial;
    private String updated;
    private List<CompletenessLevel> completenessLevel;
    private String objectType;
    private List<String> gtinIds;
    private String consumerFacingHierarchy;
    private List<String> externalIds;
    private List<String> variantIds;

    public String getStepId() {
        return stepId;
    }

    @JsonProperty("step_id")
    public void setStepId(String stepId) {
        this.stepId = stepId;
    }

/*    public String getContextId() {
        return contextId;
    }

    public void setContextId(String contextId) {
        this.contextId = contextId;
    }*/

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

    public List<String> getGtinIds() {
        return gtinIds;
    }

    public void setGtinIds(List<String> gtinIds) {
        this.gtinIds = gtinIds;
    }

    public String getConsumerFacingHierarchy() {
        return consumerFacingHierarchy;
    }

    public void setConsumerFacingHierarchy(String consumerFacingHierarchy) {
        this.consumerFacingHierarchy = consumerFacingHierarchy;
    }

    public List<CompletenessLevel> getCompleteness_level() {
        return completenessLevel;
    }

    public void setCompleteness_level(List<CompletenessLevel> completenessLevel) {
        this.completenessLevel = completenessLevel;
    }

    public List<String> getExternalIds() {
        return externalIds;
    }

    public void setExternalIds(List<String> externalIds) {
        this.externalIds = externalIds;
    }

    public List<String> getVariantIds() {
        return variantIds;
    }

    public void setVariantIds(List<String> variantIds) {
        this.variantIds = variantIds;
    }

}
