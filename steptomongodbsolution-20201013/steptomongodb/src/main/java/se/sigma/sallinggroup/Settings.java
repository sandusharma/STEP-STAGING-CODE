package se.sigma.sallinggroup;

public class Settings {

    private String _tempPathForStepXmlFiles;

    //HOTFOLDER PATH CLASSIFICATIONS // PRODUCTS
    private String _hotfolderPath_products;
    private String _hotfolderPath_classifications;

    public String getTempPathForStepXmlFiles() {
        return _tempPathForStepXmlFiles;
    }

    public void setTempPathForStepXmlFiles(String tempPathForStepXmlFiles) {
        this._tempPathForStepXmlFiles = tempPathForStepXmlFiles;
    }

    public String getHotfolderPathProducts() {
        return _hotfolderPath_products;
    }

    public void setHotfolderPathProducts(String hotfolderPath_products) {
        this._hotfolderPath_products = hotfolderPath_products;
    }

    public String getHotfolderPathClassifications() {
        return _hotfolderPath_classifications;
    }

    public void setHotfolderPathClassifications(String hotfolderPath_classifications) {
        this._hotfolderPath_classifications = hotfolderPath_classifications;
    }
}
