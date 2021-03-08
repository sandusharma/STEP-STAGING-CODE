package se.sigma.sallinggroup.mongodbmodel.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;
import java.util.List;

/**
 * Action: Apply Maven dependency for MongoDB java driver
 */
public class GoldenRecordDocument implements Serializable {

    private static final long serialVersionUID = 9051248359004100263L;

    //Action: Apply Maven dependency for MongoDB java driver
    //public BsonObjectId id;

    private String id;
    private Header header;
    private Boolean deleted;
    private List<Classification> classifications;
    private List<AttributeGroup> attributeGroups;
    private List<AssetGroup> assetGroups;
    private List<ReferenceGroup> referenceGroups;

    @JsonProperty("_id")
    public String getId() {
        return id;
    }

    @JsonProperty("_id")
    public void setId(String id) {
        this.id = id;
    }

    @JsonProperty("header")
    public Header getHeader() {
        return header;
    }

    public void setHeader(Header header) {
        this.header = header;
    }

    public List<Classification> getClassifications() {
        return classifications;
    }

    public void setClassifications(List<Classification> classifications) {
        this.classifications = classifications;
    }

    public List<AttributeGroup> getAttributeGroups() {
        return attributeGroups;
    }

    public void setAttributeGroups(List<AttributeGroup> attributeGroups) {
        this.attributeGroups = attributeGroups;
    }

    public List<AssetGroup> getAssetGroups() {
        return assetGroups;
    }

    public void setAssetGroup(List<AssetGroup> assetGroups) {
        this.assetGroups = assetGroups;
    }

    public List<ReferenceGroup> getReferenceGroups() {
        return referenceGroups;
    }

    public void setReferenceGroups(List<ReferenceGroup> referenceGroups) {
        this.referenceGroups = referenceGroups;
    }

    @JsonIgnore
    public Boolean getDeleted() {
        return deleted;
    }

    @JsonIgnore
    public void setDeleted(Boolean deleted) {
        this.deleted = deleted;
    }
}
