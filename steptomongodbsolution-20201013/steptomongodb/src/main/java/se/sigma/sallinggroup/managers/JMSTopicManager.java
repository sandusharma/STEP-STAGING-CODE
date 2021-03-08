package se.sigma.sallinggroup.managers;

import com.rabbitmq.jms.admin.RMQConnectionFactory;
import se.sigma.sallinggroup.mongodbmodel.entity.GoldenRecordDocument;

import javax.jms.*;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class JMSTopicManager {
    private List<String> _topicNames;
    private RMQConnectionFactory _connectionFactory;

    private Session _session;

    private Map<String,Topic> _topicsMap = null;

    public JMSTopicManager(List<String> topics, RMQConnectionFactory connectionFactory) {
        _topicNames = topics;
        _connectionFactory = connectionFactory;
    }



    /**
     * Init the JMS Connection
     */
    public void startConnection() throws Exception {

        try {

            Connection c = _connectionFactory.createConnection();
            c.start();
            _session = c.createSession(false, Session.AUTO_ACKNOWLEDGE);
            _topicsMap = new HashMap<>();

            for( String topicName: _topicNames) {
                _topicsMap.put(topicName, _session.createTopic(topicName));
            }
        } catch(Exception e ) {
            e.printStackTrace();
            e.fillInStackTrace();
            throw e;
        }
    }

    public void publishToTopic( String topicName, GoldenRecordDocument doc ) throws Exception, IOException {
        // Find the topic
        if( !_topicsMap.containsKey(topicName)) {
            throw new Exception("Topic " + topicName + " not found in configuration");
        }

        Topic topic = _topicsMap.get(topicName);

        // TODO: Change to JSON format
        TextMessage textMessage = _session.createTextMessage(doc.getHeader().getStepId());
        textMessage.setText(doc.getHeader().getStepId());

        MessageProducer producer = _session.createProducer(topic);
        producer.send(textMessage);
    }


    public void closeConnection() throws Exception {
        try {
            _session.close();
        } catch(Exception e ) {
            e.printStackTrace();
            e.fillInStackTrace();
            throw e;
        }
    }
}
