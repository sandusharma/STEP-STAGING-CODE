package se.sigma.sallinggroup.managers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.mongodb.*;
import com.mongodb.client.MongoDatabase;
import org.apache.log4j.Logger;
import org.bson.types.ObjectId;
import se.sigma.sallinggroup.mongodbmodel.entity.ClassificationDocument;
import se.sigma.sallinggroup.mongodbmodel.entity.GoldenRecordDocument;
import se.sigma.sallinggroup.stepxml.StepToMongoDb;

import java.util.List;

public class MongoDbManager implements DbManager {
    private String _url;
    private String _usernamne;
    private String _password;
    private String _productsCollectionName;
    private String _classificationCollectionName;
    private String _database;

    private DBCollection _collection;
    private DBCollection _classificationCollection;

    static Logger log = Logger.getLogger(MongoDbManager.class.getName());


    public MongoDbManager(String url, String username, String password, String productCollection, String classificationCollection, String database) {        _url = url;
        _usernamne = username;
        _password = password;
        _productsCollectionName = productCollection;
        _classificationCollectionName = classificationCollection;
        _database = database;
    }

    public void connect() throws Exception {
        MongoClientOptions.Builder builder = new MongoClientOptions.Builder().socketKeepAlive(true).heartbeatFrequency(1000).maxConnectionIdleTime(18000);

        MongoClientURI uri = new MongoClientURI(_url, builder);

        MongoClient mongoClient = new MongoClient(uri);
        DB database = mongoClient.getDB(_database);

        _collection =  database.getCollection(_productsCollectionName);
        _classificationCollection =  database.getCollection(_classificationCollectionName);

    }


    public void store(List<GoldenRecordDocument> goldenRecordDocuments) throws Exception {

        ObjectMapper mapper = new ObjectMapper();

        //JacksonDBCollection<GoldenRecordDocument, String> coll = JacksonDBCollection.wrap(_collection, GoldenRecordDocument.class,
        //        String.class);

        for( GoldenRecordDocument doc : goldenRecordDocuments) {

            String json = mapper.writeValueAsString(doc);

            DBObject dbo = BasicDBObject.parse(json);
            _collection.insert(dbo);

        }
    }

    public void store(GoldenRecordDocument doc) throws Exception {

        ObjectMapper mapper = new ObjectMapper();

        mapper.setPropertyNamingStrategy(
                PropertyNamingStrategy.CAMEL_CASE_TO_LOWER_CASE_WITH_UNDERSCORES);
        String json = mapper.writeValueAsString(doc);

        log.debug("Storing JSON:\n" + json);

        DBObject dbo = BasicDBObject.parse(json);

        DBCursor cursor =  _collection.find(BasicDBObject.parse("{'header.step_id':'" + doc.getHeader().getStepId() + "'}"));
        if( cursor.hasNext() ) {
            DBObject dbObject = cursor.next();

            ObjectId id = (ObjectId)dbObject.get("_id");

            dbo.put("_id", id);
            _collection.update(BasicDBObject.parse("{'header.step_id':'" + doc.getHeader().getStepId() + "'}"), dbo);

        } else {
            _collection.insert(dbo);
        }

        cursor.close();

    }

    public void delete(GoldenRecordDocument doc) throws Exception {

        ObjectMapper mapper = new ObjectMapper();

        mapper.setPropertyNamingStrategy(
                PropertyNamingStrategy.CAMEL_CASE_TO_LOWER_CASE_WITH_UNDERSCORES);
        String json = mapper.writeValueAsString(doc);

        log.debug("DELETING JSON:\n" + json);

        DBObject dbo = BasicDBObject.parse(json);

        DBCursor cursor =  _collection.find(BasicDBObject.parse("{'header.step_id':'" + doc.getHeader().getStepId() + "'}"));
        if( cursor.hasNext() ) {
            _collection.remove(BasicDBObject.parse("{'header.step_id':'" + doc.getHeader().getStepId() + "'}"));
        }
        cursor.close();

    }

    public void delete(ClassificationDocument doc) throws Exception {

        ObjectMapper mapper = new ObjectMapper();

        mapper.setPropertyNamingStrategy(
                PropertyNamingStrategy.CAMEL_CASE_TO_LOWER_CASE_WITH_UNDERSCORES);
        String json = mapper.writeValueAsString(doc);

        log.debug("DELETING JSON:\n" + json);

        DBObject dbo = BasicDBObject.parse(json);

        DBCursor cursor =  _classificationCollection.find(BasicDBObject.parse("{'header.step_id':'" + doc.getHeader().getStepId() + "'}"));
        if( cursor.hasNext() ) {
            _classificationCollection.remove(BasicDBObject.parse("{'header.step_id':'" + doc.getHeader().getStepId() + "'}"));
        }
        cursor.close();

    }



    public void store(ClassificationDocument doc) throws Exception {

        ObjectMapper mapper = new ObjectMapper();

        mapper.setPropertyNamingStrategy(
                PropertyNamingStrategy.CAMEL_CASE_TO_LOWER_CASE_WITH_UNDERSCORES);
        String json = mapper.writeValueAsString(doc);

        DBObject dbo = BasicDBObject.parse(json);

        DBCursor cursor =  _classificationCollection.find(BasicDBObject.parse("{'header.step_id':'" + doc.getHeader().getStepId() + "'}"));
        if( cursor.hasNext() ) {
            DBObject dbObject = cursor.next();

            ObjectId id = (ObjectId)dbObject.get("_id");

            dbo.put("_id", id);
            _classificationCollection.update(BasicDBObject.parse("{'header.step_id':'" + doc.getHeader().getStepId() + "'}"), dbo);

        } else {
            _classificationCollection.insert(dbo);
        }

        cursor.close();

    }

}
