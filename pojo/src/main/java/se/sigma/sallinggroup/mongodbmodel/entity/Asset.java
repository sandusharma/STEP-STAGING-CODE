package se.sigma.sallinggroup.mongodbmodel.entity;

import java.util.List;

public class Asset {

    private String assetId;
    private String damIdentifier;
    private List<Attribute> metadata;

    public String getAssetId() {
        return assetId;
    }

    public void setAssetId(String assetId) {
        this.assetId = assetId;
    }

    public String getDamIdentifier() {
        return damIdentifier;
    }

    public void setDamIdentifier(String damIdentifier) {
        this.damIdentifier = damIdentifier;
    }

    public List<Attribute> getMetadata() {
        return metadata;
    }

    public void setMetadata(List<Attribute> metadata) {
        this.metadata = metadata;
    }

}
