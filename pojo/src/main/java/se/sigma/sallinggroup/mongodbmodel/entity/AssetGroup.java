package se.sigma.sallinggroup.mongodbmodel.entity;

import java.util.List;

public class AssetGroup {

    public String assetGroupId;
    public List<Asset> assets;

    public String getAssetGroupId() {
        return assetGroupId;
    }

    public void setAssetGroupId(String assetGroupId) {
        this.assetGroupId = assetGroupId;
    }

    public List<Asset> getAssets() {
        return assets;
    }

    public void setAssets(List<Asset> assets) {
        this.assets = assets;
    }

}
