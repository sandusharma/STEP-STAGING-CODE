package se.sigma.sallinggroup.stepxml;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;

import se.sigma.sallinggroup.StepToJson;
import se.sigma.sallinggroup.mongodbmodel.entity.ClassificationDocument;
import se.sigma.sallinggroup.mongodbmodel.entity.GoldenRecordDocument;
import se.sigma.sallinggroup.managers.DbManager;
import se.sigma.sallinggroup.managers.JMSQueueManager;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import se.sigma.sallinggroup.managers.RabbitMQTopicManager;
import se.sigma.sallinggroup.Settings;
import javax.management.Attribute;
import javax.management.MBeanServerConnection;
import javax.management.ObjectName;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static java.lang.System.exit;

public class StepToMongoDb {
    static Logger log = Logger.getLogger(StepToMongoDb.class.getName());


    public static void main(String[] args) {

        String params = "";
        if (args != null) {
            for (String param : args) {
                if (params.length() > 0) params += " ";
                params += param;
            }
        }
        log.info("Application starting with parameters " + params);

        log.debug("main: Loading application configuration file");
        ApplicationContext context = new ClassPathXmlApplicationContext("beans.xml");
        log.debug("main: Loading Mongo DB manager");
        DbManager mongoDbManager = (DbManager) context.getBean("dbManager");
        JMSQueueManager productsQueueManager = null;
        JMSQueueManager classificationsQueueManager = null;
        RabbitMQTopicManager topicManager = null;
        log.debug("main: Loading application settings");
        Settings settings = (Settings) context.getBean("settings");
        log.debug("main: Settings loaded.");


        // Init managers to be able to connect to the JMS Queues for products and classifications
        try {
            productsQueueManager = (JMSQueueManager) context.getBean("stepProductsQueueManager");
            classificationsQueueManager = (JMSQueueManager) context.getBean("stepClassificationsQueueManager");
            topicManager = (RabbitMQTopicManager) context.getBean("rabbitMqTopicManager");
        } catch (Exception e) {
            log.error("main: Unable to create beans from beans.xml", e);
            return;
        }

        if (args.length == 0) {
            log.error("main: Unable to start application. Usage: StepToMongoDb {postfile|hotfolder|queue} filename");
            System.out.print("Usage: StepToMongoDb {postfile|hotfolder|queue} filename");
            return;
        }

        // POST file to JMS Queue
        if ("postfile".equals(args[0])) {
            if (args.length != 2) {
                log.error("Unable to start application. Usage: StepToMongoDb postfile filename-of-stepxml.xml");

                System.out.print("Usage: StepToMongoDb postfile filename-of-stepxml.xml");
                return;
            }

            log.info("main: Running postfile on file " + args[1]);
            if (postToQueue(args[1], productsQueueManager)) return;
            log.info("main: Post file done");

            exit(0);

            // Process items from JMS queue
        } else if (args[0].equals("queue")) {
            log.info("main: Running queue to listen to MQ Queue");


            try {
                log.info("main: Connecting to Mongo DB using " + mongoDbManager.getClass().getName());
                mongoDbManager.connect();
            } catch (Exception e) {
                log.error("Unable to connect to Mongo DB", e);
                return;
            }


            try {
                log.info("main: Connecting to products queue ");
                productsQueueManager.startConnection();
                log.info("main: Connecting to classifications queue ");
                classificationsQueueManager.startConnection();
                log.info("main: Connecting to topics");
                topicManager.startConnection();
                log.info("main: Start listening to messages on queues");
                while (true) {


                    // Process products
                    {
                        String onlyFilename = "product-" + System.currentTimeMillis() + ".xml";
                        String temporaryFileName = storeFromQueueToFile(productsQueueManager, settings, onlyFilename);

                        if (temporaryFileName == null) {
                            try {
                                TimeUnit.SECONDS.sleep(5);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                            continue;
                        }

                        File stepXmlFile = new File(temporaryFileName);
                        processProductsFromFile(context, topicManager, mongoDbManager, settings, onlyFilename, stepXmlFile);
                    }

                    {

                        // Process classifications
                        String onlyFilename = "classifications-" + System.currentTimeMillis() + ".xml";
                        String temporaryFileName = storeFromQueueToFile(classificationsQueueManager, settings, onlyFilename);

                        if (temporaryFileName == null) {
                            try {
                                TimeUnit.SECONDS.sleep(5);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                            continue;
                        }

                        File stepXmlFile = new File(temporaryFileName);
                        processClassificationsFromFile(context, topicManager, mongoDbManager, settings, onlyFilename, stepXmlFile);
                    }
                }

            } catch (Exception e) {
                log.error("main: queue processing stopped", e);
                return;
            }


        } else if (args[0].equals("hotfolder")) {

            log.info("main: Starting as hotfolder");
            try {
                log.info("main hotfolder: Connecting to products queue");
                productsQueueManager.startConnection();
                log.info("main hotfolder: Connecting to classifications queue");

                classificationsQueueManager.startConnection();
                log.info("main hotfolder: Connecting to topics");

                topicManager.startConnection();
            } catch (Exception e) {
                log.error("main hotfolder: Unable to connect to queues", e);
                return;
            }



            if ((settings.getHotfolderPathClassifications() != null && settings.getHotfolderPathClassifications().length() != 0) || (settings.getHotfolderPathProducts() != null && settings.getHotfolderPathProducts().length() != 0)) {
                log.info("main hotfolder: Start watching hotfolder for classifications " + settings.getHotfolderPathClassifications() + ", products " + settings.getHotfolderPathProducts());

                // Check for existence of folders
                if( !new File(settings.getHotfolderPathClassifications() ).isDirectory()) {
                    log.error("Hotfolder: Unable to find directory for classification files: " + settings.getHotfolderPathClassifications());
                    exit(10);
                }

                if( !new File(settings.getHotfolderPathClassifications() + File.separator + "archived").isDirectory()) {
                    log.error("Hotfolder: Unable to find directory for classification files archive: " + settings.getHotfolderPathClassifications() + File.separator + "archived");
                    exit(10);
                }

                if( !new File(settings.getHotfolderPathProducts() ).isDirectory()) {
                    log.error("Hotfolder: Unable to find directory for products files: " + settings.getHotfolderPathProducts());
                    exit(10);
                }

                if( !new File(settings.getHotfolderPathProducts() + File.separator + "archived").isDirectory()) {
                    log.error("Hotfolder: Unable to find directory for products files archive: " + settings.getHotfolderPathProducts() + File.separator + "archived");
                    exit(10);
                }




                while (true) {

                    try {

                        // Process hotfolder for Classifications
                        if ((settings.getHotfolderPathClassifications() != null && settings.getHotfolderPathClassifications().length() != 0)) {

                            DirectoryStream<Path> classification_stepxml = Files.newDirectoryStream(Paths.get(settings.getHotfolderPathClassifications()), path -> path.toFile().isFile() && path.toFile().toString().endsWith((".xml")));

                            for (Path path1 : classification_stepxml) {

                                File stepXmlFile = path1.toFile();
                                if( postToQueue(stepXmlFile.getAbsolutePath(), classificationsQueueManager) ) {
                                    log.error("Hotfolder: Unable to process file " + path1.toFile().getName());
                                } else {
                                    //Archived folder
                                    File ArchivedDir = new File(settings.getHotfolderPathClassifications() + File.separator + "archived");

                                    boolean success = path1.toFile().renameTo(new File(ArchivedDir.getAbsolutePath() + File.separator + path1.toFile().getName()));
                                    if (!success) {
                                        success = path1.toFile().renameTo(new File(ArchivedDir.getAbsolutePath() + File.separator + System.currentTimeMillis() + "_" + path1.toFile().getName()));
                                    }
                                    if (!success) {
                                        log.error("Hotfolder: Unable to move classification file " + path1.toFile().getName() + " to archived");
                                        exit(10);
                                    }
                                }
                            }

                            classification_stepxml.close();
                        }


                        // Process hotfolder for Products
                        if ((settings.getHotfolderPathProducts() != null && settings.getHotfolderPathProducts().length() != 0)) {

                            DirectoryStream<Path> product_stepxmls = Files.newDirectoryStream(Paths.get(settings.getHotfolderPathProducts()), path -> path.toFile().isFile() && path.toFile().toString().endsWith((".xml")));

                            for (Path path1 : product_stepxmls) {
                                File stepXmlFile = path1.toFile();
                                if( postToQueue(stepXmlFile.getAbsolutePath(), productsQueueManager) ) {
                                    log.error("Hotfolder: Unable to process file " + path1.toFile().getName());
                                } else {
                                    //Archived folder
                                    File ArchivedDir = new File(settings.getHotfolderPathProducts() + File.separator + "archived");

                                    boolean success = path1.toFile().renameTo(new File(ArchivedDir.getAbsolutePath() + File.separator + path1.toFile().getName()));
                                    if (!success) {
                                        success = path1.toFile().renameTo(new File(ArchivedDir.getAbsolutePath() + File.separator + System.currentTimeMillis() + "_" + path1.toFile().getName()));
                                    }
                                    if (!success) {
                                        log.error("Hotfolder: Unable to move products file " + path1.toFile().getName() + " to archived");
                                        exit(10);
                                    }
                                }
                            }
                            product_stepxmls.close();

                        }

                        ((StepToJson) context.getBean("stepToJson")).hotfolderTimeIntervalCheck(5);

                    } catch (Exception e) {
                        log.warn("main hotfolder: Error processing on hotfolder", e);
                    }
                }
            }


        } else if (args[0].equals("subscribe")) {
            try {
                topicManager.startConnection();

            } catch (Exception e) {
                log.error("subsccribe: unable to connect to topic manager", e);
            }

            if (args.length == 2) {
                if (args[1] != null && !args[1].equals("")) {
                    try {
                        String parameter = args[1];
                        if (parameter.contains("Article")) {
                            topicManager.subscribeToArticle(parameter);
                        } else if (parameter.contains("CFH")) {
                            topicManager.subscribeToClassification(parameter);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        } else if (args[0].equals("consume")) {
            topicManager = (RabbitMQTopicManager) context.getBean("rabbitMqTopicManager");
            try {
                topicManager.startConnection();

                topicManager.consume();

                topicManager.closeConnection();
            } catch (Exception e) {
                e.printStackTrace();
                return;
            }
        }
    }

    private static void processClassificationsFromFile(ApplicationContext context, RabbitMQTopicManager topicManager, DbManager mongoDbManager, Settings settings, String onlyFilename, File stepXmlFile) {
        try {

            log.debug("processClassificationsFromFile: Processing classification " + stepXmlFile.getAbsolutePath());

            List<ClassificationDocument> documents = ((StepToJson) context.getBean("stepToJson")).processClassificationStepXml(stepXmlFile);

            for (ClassificationDocument doc : documents) {
                log.debug("processClassificationsFromFile: Storing classification to MongoDB " + doc.getHeader().getStepId());
                mongoDbManager.store(doc);
                if (doc.getHeader().getStepId().contains("CFH")) {
                    log.debug("processClassificationsFromFile: Notifying topic " + "PIM.PIMProductInformation." + doc.getHeader().getStepId());
                    topicManager.publishClassificationToTopic(doc, "PIM.PIMProductInformation." + doc.getHeader().getStepId());
                } else {
                    log.debug("processClassificationsFromFile: Notifying topic " + "PIM.PIMProductInformation.CFH" + doc.getHeader().getStepId());

                    topicManager.publishClassificationToTopic(doc, "PIM.PIMProductInformation.CFH" + doc.getHeader().getStepId());

                }
            }

            // Success folder
            File successDir = new File(settings.getTempPathForStepXmlFiles() + File.separator + "processed");
            if (!successDir.exists()) {
                successDir.mkdirs();
            }
            stepXmlFile.renameTo(new File(successDir.getAbsolutePath() + File.separator + onlyFilename));
            log.debug("processClassificationsFromFile: Successful processing " + successDir.getAbsolutePath() + File.separator + onlyFilename);
        } catch (Exception e) {
            // Failed files folder
            File failedDir = new File(settings.getTempPathForStepXmlFiles() + File.separator + "error");
            if (!failedDir.exists()) {
                failedDir.mkdirs();
            }
            stepXmlFile.renameTo(new File(failedDir.getAbsolutePath() + File.separator + onlyFilename));
            log.error("processClassificationsFromFile: Failed processing " + failedDir.getAbsolutePath() + File.separator + onlyFilename);
        }
    }

    /**
     * Process a single file stored to disk in a temporary location. This will convert from StepXML to JSON
     * and publish the result to the provided mongodb manager.
     *
     * @param context
     * @param mongoDbManager
     * @param settings
     * @param onlyFilename
     * @param stepXmlFile
     */
    private static void processProductsFromFile(ApplicationContext context, RabbitMQTopicManager topicManager, DbManager mongoDbManager, Settings settings, String onlyFilename, File stepXmlFile) {
        try {
            log.debug("processProductsFromFile: Processing product " + stepXmlFile.getAbsolutePath());

            List<GoldenRecordDocument> goldenRecordDocuments = ((StepToJson) context.getBean("stepToJson")).processStepxmlFile(stepXmlFile);

            Boolean successful = true;

            for (GoldenRecordDocument doc : goldenRecordDocuments) {

                if( doc.getDeleted() != null && doc.getDeleted() ) {
                    log.debug("processProductsFromFile: DELETE document from MongoDB using id " + doc.getHeader().getStepId());
                    try {
                        mongoDbManager.delete(doc);
                        topicManager.publishArticleToTopic(doc, "PIM.PIMProductInformation.DeleteArticle.GoldenRecordId." + doc.getHeader().getStepId());
                    } catch( Exception e ) {
                        successful = false;
                        log.warn("processProductsFromFile: Failed deleting from DB " + doc.getHeader().getStepId(), e);
                    }
                    continue;
                }


                log.debug("processProductsFromFile: Storing document to MongoDB using id " + doc.getHeader().getStepId());
                try {
                    mongoDbManager.store(doc);
                    if(doc.getHeader().getObjectType().equals("GoldenRecord")) {
                        log.debug("processProductsFromFile: processing Golden record" + doc.getHeader().getStepId());
                        topicManager.publishArticleToTopic(doc, "PIM.PIMProductInformation.Article.GoldenRecordId." + doc.getHeader().getStepId());
                    }

                        if (doc.getHeader().getExternalIds() != null && doc.getHeader().getExternalIds().size() != 0) {
                            for (String ext_id : doc.getHeader().getExternalIds()) {
                                log.debug("processProductsFromFile: processing Golden record with External ID" + doc.getHeader().getStepId());
                                topicManager.publishArticleToTopic(doc, ("PIM.PIMProductInformation.Article.ExternalId." + ext_id));
                            }
                        }



                    if(doc.getHeader().getConsumerFacingHierarchy() != null) {
                        log.debug("processProductsFromFile: processing Golden record with CFH Node ID" + doc.getHeader().getConsumerFacingHierarchy());
                        topicManager.publishArticleToTopic(doc, ("PIM.PIMProductInformation.Article.CFHNodeId." + doc.getHeader().getConsumerFacingHierarchy()));

                    }
                    if(doc.getHeader().getObjectType().equals("ItemFamily")) {
                        log.debug("processProductsFromFile: processing Item Family with ID" + doc.getHeader().getStepId());
                        topicManager.publishArticleToTopic(doc, ("PIM.PIMProductInformation.Article.ItemFamilyId." + doc.getHeader().getStepId()));
                    }


                } catch( Exception e ) {
                    successful = false;
                    log.warn("processProductsFromFile: Failed storing to DB " + doc.getHeader().getStepId(), e);

                }
            }

            String folderName = "processed";
            if( !successful ) folderName = "failed";

            // Success folder
            File successDir = new File(settings.getTempPathForStepXmlFiles() + File.separator + folderName);
            if (!successDir.exists()) {
                successDir.mkdirs();
            }

            stepXmlFile.renameTo(new File(successDir.getAbsolutePath() + File.separator + onlyFilename));

            log.debug("processProductsFromFile: Done with file " + stepXmlFile.getAbsolutePath());


        } catch (Exception e) {
            log.error("processProductsFromFile: Failed with file " + stepXmlFile.getAbsolutePath(), e);

            // Failed files folder
            File failedDir = new File(settings.getTempPathForStepXmlFiles() + File.separator + "error");
            if (!failedDir.exists()) {
                failedDir.mkdirs();
            }
            stepXmlFile.renameTo(new File(failedDir.getAbsolutePath() + File.separator + onlyFilename));

        }
    }

    /**
     * @param jmsQueueManager
     * @param settings
     * @param onlyFilename
     * @return If nothing was found on queue, null is returned, otherwiase the name of the temporary file name
     * @throws Exception
     */
    private static String storeFromQueueToFile(JMSQueueManager jmsQueueManager, Settings settings, String onlyFilename) throws Exception {
        String stepXmlAsString = jmsQueueManager.getNextMessageFromQueue();
        if (stepXmlAsString == null) {
            return null;
        }

        String temporaryFileName = settings.getTempPathForStepXmlFiles() + File.separator + onlyFilename;

        try {
            log.debug("storeFromQueueToFile: Storing file " + temporaryFileName);
            FileUtils.writeStringToFile(new File(temporaryFileName), stepXmlAsString, StandardCharsets.UTF_8);
        } catch (Exception e) {
            log.error("storeFromQueueToFile: Unable to store file ", e);
        }
        return temporaryFileName;
    }

    /**
     * Post message to JMS queue using a queue manager.
     *
     * @param fileName
     * @param jmsQueueManager
     * @return
     */
    private static boolean postToQueue(String fileName, JMSQueueManager jmsQueueManager) {
        String stepXmlAsFile = null;
        try {
            stepXmlAsFile = readFile(fileName);
        } catch (IOException ioe) {
            ioe.printStackTrace();
            return true;
        }

        try {
            log.debug("postToQueue: Start file " + fileName);
            jmsQueueManager.startConnection();
            jmsQueueManager.postMessage(fileName, stepXmlAsFile);
            log.debug("postToQueue: Done file " + fileName);

        } catch (Exception e) {
            log.error("postToQueue: Failed to store file " + fileName, e);

            return true;
        } finally {
            try {
                jmsQueueManager.closeConnection();
            } catch( Exception x) {}
        }
        System.out.println("Done, exiting");
        return false;
    }

    private static String readFile(String path) throws IOException {
        byte[] encoded = Files.readAllBytes(Paths.get(path));
        return new String(encoded, StandardCharsets.UTF_8);
    }
}
