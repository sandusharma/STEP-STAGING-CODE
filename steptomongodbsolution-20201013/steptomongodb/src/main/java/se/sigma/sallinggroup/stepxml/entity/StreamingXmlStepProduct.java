package se.sigma.sallinggroup.stepxml.entity;

import java.util.List;

public class StreamingXmlStepProduct {
    private String _stepId;
    private String userTypeId;
    private String name;
    private String parentId;
    private List<XmlProductAttributeValue> values;
    private List<XmlClassificationLink> classificationLinks;
    private List<XmlAssetReference> assetReferences;
    private List<XmlProductCrossReference> productCrossReferences;
    private List<XmlStepEntityReference> entityReferences;
    private List<String> variantIds;
    private Boolean deleted;

    public String getStepId() { return _stepId; }
    public void setStepId(String stepId) { _stepId = stepId; }

    public String getParentId() {
        return parentId;
    }

    public void setParentId(String parentId) {
        this.parentId = parentId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUserTypeId() {
        return userTypeId;
    }

    public void setUserTypeId(String userTypeId) {
        this.userTypeId = userTypeId;
    }

    public List<XmlProductAttributeValue> getValues() {
        return values;
    }

    public void setValues(List<XmlProductAttributeValue> values) {
        this.values = values;
    }

    public List<XmlClassificationLink> getClassificationLinks() {
        return classificationLinks;
    }

    public void setClassificationLinks(List<XmlClassificationLink> classificationLinks) {
        this.classificationLinks = classificationLinks;
    }

    public List<XmlAssetReference> getAssetReferences() {
        return assetReferences;
    }

    public void setAssetReferences(List<XmlAssetReference> assetReferences) {
        this.assetReferences = assetReferences;
    }

    public List<XmlProductCrossReference> getProductCrossReferences() {
        return productCrossReferences;
    }

    public void setProductCrossReferences(List<XmlProductCrossReference> productCrossReferences) {
        this.productCrossReferences = productCrossReferences;
    }

    public List<XmlStepEntityReference> getEntityReferences() {
        return entityReferences;
    }

    public void setEntityReferences(List<XmlStepEntityReference> entityReferences) {
        this.entityReferences = entityReferences;
    }

    public List<String> getVariantIds() {
        return variantIds;
    }

    public void setVariantIds(List<String> variantIds) {
        this.variantIds = variantIds;
    }

    public Boolean getDeleted() {
        return deleted;
    }

    public void setDeleted(Boolean deleted) {
        this.deleted = deleted;
    }
}
