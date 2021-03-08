package se.sigma.sallinggroup.mongodbproxy;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import org.apache.commons.io.IOUtils;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import se.sigma.sallinggroup.mongodbmodel.entity.ClassificationDocument;
import se.sigma.sallinggroup.mongodbmodel.entity.GoldenRecordDocument;
import se.sigma.sallinggroup.managers.DbManager;

import java.io.*;
import javax.servlet.*;
import javax.servlet.http.*;

public class StoreInMongoServlet extends HttpServlet {

    private DbManager dbManager = null;


    public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
    {


        // JSonData
        if( request.getQueryString() == null || !request.getQueryString().contains("i4827askadsf32AK-askkKEWRQqerw")  ) {
            response.setContentType("text/html");
            response.sendError(401, "Not authorized");
            return;
        }


        StringWriter writer = new StringWriter();
        IOUtils.copy(request.getInputStream(), writer, "UTF-8");
        String inputData = writer.toString();



        ObjectMapper mapper = new ObjectMapper();
        mapper.setPropertyNamingStrategy(
                PropertyNamingStrategy.CAMEL_CASE_TO_LOWER_CASE_WITH_UNDERSCORES);

        ApplicationContext context = new ClassPathXmlApplicationContext("webbeans.xml");

        if( dbManager == null ) {
            dbManager = (DbManager) context.getBean("dbManager");
            try {
                dbManager.connect();
            } catch (Exception e) {
                e.printStackTrace();
                response.setContentType("text/html");
                response.sendError(500, "Unable to connect to MongoDB " + e.getMessage());
            }
        }


        if( request.getQueryString().contains("type=product")) {
            GoldenRecordDocument doc = new GoldenRecordDocument();
            try {
                doc = mapper.readValue(inputData, GoldenRecordDocument.class);
            } catch (Exception e) {
                response.setContentType("text/html");
                response.sendError(400, "Invalid format " + e.getMessage());
            }

            try {
                dbManager.store(doc);
            } catch( Exception e ) {
                e.printStackTrace();
                response.setContentType("text/html");
                response.sendError(500, "Unable to store " + e.getMessage());
            }

            response.setContentType("text/html");
            response.setStatus(200);
            response.getOutputStream().println("OK: data " + mapper.writeValueAsString(doc));
        } else if( request.getQueryString().contains("type=classification")) {
            ClassificationDocument doc = new ClassificationDocument();
            try {
                doc = mapper.readValue(inputData, ClassificationDocument.class);
            } catch (Exception e) {
                response.setContentType("text/html");
                response.sendError(400, "Invalid format " + e.getMessage());
            }

            try {
                dbManager.store(doc);
            } catch( Exception e ) {
                e.printStackTrace();

                response.setContentType("text/html");
                response.sendError(500, "Unable to store " + e.getMessage());
            }

            response.setContentType("text/html");
            response.setStatus(200);
            response.getOutputStream().println("OK: data " + mapper.writeValueAsString(doc));
        } else {
            response.setContentType("text/html");
            response.sendError(400, "Invalid format " );
        }




        return;


    }

}
