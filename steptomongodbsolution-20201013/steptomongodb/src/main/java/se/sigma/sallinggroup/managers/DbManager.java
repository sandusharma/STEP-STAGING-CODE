package se.sigma.sallinggroup.managers;

import se.sigma.sallinggroup.mongodbmodel.entity.ClassificationDocument;
import se.sigma.sallinggroup.mongodbmodel.entity.GoldenRecordDocument;

public interface DbManager {
    public void connect() throws Exception;
    public void store(GoldenRecordDocument doc) throws Exception;
    public void store(ClassificationDocument doc) throws Exception;
    public void delete(ClassificationDocument doc) throws Exception;
    public void delete(GoldenRecordDocument doc) throws Exception;
}
