package se.sigma.sallinggroup.managers;

import com.rabbitmq.jms.admin.RMQConnectionFactory;
import com.rabbitmq.jms.client.message.RMQTextMessage;

import javax.jms.*;

public class JMSQueueManager {
    private String _queueName;
    private RMQConnectionFactory _connectionFactory;

    private Session _session;
    private Queue _queue;

    public JMSQueueManager(String queue, RMQConnectionFactory connectionFactory) {
        _queueName = queue;
        _connectionFactory = connectionFactory;
    }


    public String getNextMessageFromQueue() throws Exception {
        MessageConsumer receiver = _session.createConsumer(_queue);
        Message msg = receiver.receiveNoWait();

        if( msg == null ) return null;

        if( !(msg instanceof RMQTextMessage)) return null;

        RMQTextMessage textMessage = (RMQTextMessage)msg;

        return textMessage.getText();
    }


    public void postMessage(String correlationId, String messageContent) throws Exception {
        TextMessage message = _session.createTextMessage(messageContent);
        message.setJMSCorrelationID(correlationId);

        Destination replyQueue = _session.createTemporaryQueue();
        MessageProducer producer = _session.createProducer(_queue);

        message.setJMSReplyTo(replyQueue);
        producer.send(message);
    }


    /**
     * Init the JMS Connection
     */
    public void startConnection() throws Exception {

        try {

            Connection c = _connectionFactory.createConnection();
            c.start();
            _session = c.createSession(false, Session.AUTO_ACKNOWLEDGE);
            _queue = _session.createQueue(_queueName);
        } catch(Exception e ) {
            e.printStackTrace();
            e.fillInStackTrace();
            throw e;
        }
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
