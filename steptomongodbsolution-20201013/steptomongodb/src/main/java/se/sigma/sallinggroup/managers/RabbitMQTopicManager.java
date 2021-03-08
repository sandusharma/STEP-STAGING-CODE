package se.sigma.sallinggroup.managers;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.DeliverCallback;
import com.rabbitmq.jms.admin.RMQConnectionFactory;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

import se.sigma.sallinggroup.mongodbmodel.entity.ClassificationDocument;
import se.sigma.sallinggroup.mongodbmodel.entity.GoldenRecordDocument;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;

public class RabbitMQTopicManager {
    private String _consumerName;
    private ConnectionFactory _connectionFactory;

    private Channel _channel;

    public RabbitMQTopicManager( RMQConnectionFactory connectionFactory, String consumerName) {
        _connectionFactory = new ConnectionFactory();
        _connectionFactory.setHost(connectionFactory.getHost());
        _connectionFactory.setUsername(connectionFactory.getUsername());
        _connectionFactory.setPassword(connectionFactory.getPassword());
        _connectionFactory.setPort(connectionFactory.getPort());
        _consumerName = consumerName;
    }



    /**
     * Init the JMS Connection
     */
    public void startConnection() throws Exception {

        try {

            Connection c = _connectionFactory.newConnection();
            _channel = c.createChannel();

        } catch(Exception e ) {
            e.printStackTrace();
            e.fillInStackTrace();
            throw e;
        }
    }

    public void publishClassificationToTopic(GoldenRecordDocument doc, String routingKey) throws Exception, IOException {
        _channel.exchangeDeclare("PIM_Classifications", "topic");

        ObjectMapper mapper = new ObjectMapper();
        mapper.setPropertyNamingStrategy(
                PropertyNamingStrategy.CAMEL_CASE_TO_LOWER_CASE_WITH_UNDERSCORES);
        String textMessage = mapper.writeValueAsString(doc.getHeader());

        _channel.basicPublish("PIM_Classifications", routingKey, null, textMessage.getBytes("UTF-8"));
    }


    public void publishArticleToTopic(GoldenRecordDocument doc, String routingKey) throws Exception, IOException {
        _channel.exchangeDeclare("PIM_Articles", "topic");
        ObjectMapper mapper = new ObjectMapper();
        mapper.setPropertyNamingStrategy(
                PropertyNamingStrategy.CAMEL_CASE_TO_LOWER_CASE_WITH_UNDERSCORES);
        String textMessage = mapper.writeValueAsString(doc.getHeader());

        _channel.basicPublish("PIM_Articles", routingKey, null, textMessage.getBytes("UTF-8"));
    }

    public void publishClassificationToTopic(ClassificationDocument doc, String routingKey) throws Exception, IOException {
        _channel.exchangeDeclare("PIM_Classifications", "topic");

        ObjectMapper mapper = new ObjectMapper();
        mapper.setPropertyNamingStrategy(
                PropertyNamingStrategy.CAMEL_CASE_TO_LOWER_CASE_WITH_UNDERSCORES);
        String textMessage = mapper.writeValueAsString(doc.getHeader());

        _channel.basicPublish("PIM_Classifications", routingKey, null, textMessage.getBytes("UTF-8"));
    }

    public void subscribeToArticle(String routingKey) throws Exception, IOException {
        String queueName = _channel.queueDeclare(this._consumerName, false, false, false, null).getQueue();
        _channel.queueBind(queueName, "PIM_Articles", routingKey);
        System.out.println("Subscribed to " + routingKey);
    }

    public void subscribeToClassification(String routingKey) throws Exception, IOException {
        String queueName = _channel.queueDeclare(this._consumerName, false, false, false, null).getQueue();
        _channel.queueBind(queueName, "PIM_Classifications", routingKey);
        System.out.println("Subscribed to " + routingKey);
    }

    public void consume() {
        try {
            DeliverCallback deliverCallback = (consumerTag, delivery) -> {
                String message = new String(delivery.getBody(), "UTF-8");
                System.out.println(" [x] Received '" + message + "'");
            };
            _channel.basicConsume(_consumerName, true, deliverCallback, (consumerTag, delivery) -> {
                System.out.println(" [x] Received '" + delivery + "'");
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public void closeConnection() throws Exception {
        try {
            _channel.close();
        } catch(Exception e ) {
            e.printStackTrace();
            e.fillInStackTrace();
            throw e;
        }
    }
}
