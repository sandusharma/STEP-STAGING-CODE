package com.dsg.dk;
/**
 * 
 */
import java.io.*;
import javax.xml.parsers.*;
import java.io.OutputStream;
import org.xml.sax.Attributes; 
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;
import com.sap.aii.mapping.api.AbstractTransformation; 
import com.sap.aii.mapping.api.StreamTransformationException; 
import com.sap.aii.mapping.api.TransformationInput; 
import com.sap.aii.mapping.api.TransformationOutput;


import java.io.Writer;


/**
 * @author ibmprm
 *
 */
public class ACNielson_Mapping extends AbstractTransformation {
	String fieldSep;
	String countryCode;
	public void transform(TransformationInput transformationInput, TransformationOutput transformationOutput) throws StreamTransformationException {
		fieldSep = transformationInput.getInputParameters().getString("fieldSep");
		countryCode = transformationInput.getInputParameters().getString("countryCode");
		InputStream inputstream = transformationInput.getInputPayload().getInputStream();  
		OutputStream outputstream = transformationOutput.getOutputPayload().getOutputStream();
		
		execute(inputstream, outputstream);

	}
	
	public void execute(InputStream inputstream, OutputStream outputstream)  {
		try{         
			SAXParserFactory factory = SAXParserFactory.newInstance();
			SAXParser parser = factory.newSAXParser();
			One2OneHandler handler = new One2OneHandler();
			parser.parse(inputstream, handler);
			Writer out = new OutputStreamWriter(outputstream, "UTF8");
			String outputString = handler.getOutputString();
			out.write(outputString);
			out.close();

		} 
		catch (Exception exception){  
			exception.printStackTrace();  
			//this.getTrace().addDebugMessage(exception.toString());
		}  

	}


	class One2OneHandler extends DefaultHandler{
		String sContent = "";
		String sTemp = "";
		String YearWeek,Site,ProfitCenter,EAN,NetSalesQty,NetSales,UnitPrice,ArticleDescription,ContentUnit,NetContents,Article,PromotionFlag,SalesCurrency,SalesUnit,CountryCode,BLOCK_SEQUENCE_NUMBER,TOTAL_BLOCKS;
		//String Partner;

		StringBuffer fresult = new StringBuffer();
		//fresult.append("YearWeek,Site");
		//java.util.Map map;

		public String getOutputString(){
			return fresult.toString();
		}

		public void startDocument() throws SAXException{
			        //fresult.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
			if(!countryCode.equals("DE"))
			fresult.append("YearWeek"+fieldSep+"Site"+fieldSep+"EAN"+fieldSep+"NetSalesQty"+fieldSep+"NetSales"+fieldSep+"UnitPrice"+fieldSep+"ArticleDescription"+fieldSep+"ContentUnit"+fieldSep+"NetContents"+fieldSep+"Article"+fieldSep+"PromotionFlag"+fieldSep+"SalesCurrency"+fieldSep+"SalesUnit"+fieldSep+"CountryCode\n");
		}

		public void endDocument() throws SAXException{
		}

		public void startElement(String uri, String tag, String name, Attributes attributes) throws SAXException{

			/**if("ControlRecords".equals(name))
			{
				Partner = "";
			}*/
			if("DataRecords".equals((name)))
			{
				YearWeek = "";
				Site = "";
				ProfitCenter = "";
				EAN = "";
				NetSalesQty= "";
				NetSales ="";
				UnitPrice="";
				ArticleDescription="";
				ContentUnit="";
				NetContents="";
				Article="";
				PromotionFlag="";
				SalesCurrency="";
				SalesUnit="";
				CountryCode="";

			}
		}

		public void characters(char ch[], int start, int length)throws SAXException{
			sContent = new String(ch,start,length);

		}

		public void endElement(String uri,String tag,String qName){
			// parse elements
			//if("PARTNER".equals(qName)) Partner = sContent;
			if("BLOCK_SEQUENCE_NUMBER".equals(qName)) BLOCK_SEQUENCE_NUMBER = sContent;
			if("TOTAL_BLOCKS".equals(qName)) TOTAL_BLOCKS = sContent;
			if("YearWeek".equals(qName)) YearWeek = sContent;
			if("Site".equals(qName)) Site = sContent;
			//if("ProfitCenter".equals(qName)) ProfitCenter = sContent;
			if("EAN".equals(qName)) EAN = sContent;
			if("NetSalesQty".equals(qName)) NetSalesQty = sContent;
			if("NetSales".equals(qName)) NetSales = sContent;
			if("UnitPrice".equals(qName)) UnitPrice = sContent;
			if("ArticleDescription".equals(qName)) ArticleDescription = sContent;
			if("ContentUnit".equals(qName)) ContentUnit = sContent;
			if("NetContents".equals(qName)) NetContents = sContent;
			if("Article".equals(qName)) Article = sContent;
			if("PromotionFlag".equals(qName)) PromotionFlag = sContent;
			if("SalesCurrency".equals(qName)) SalesCurrency = sContent;
			if("SalesUnit".equals(qName)) SalesUnit = sContent;
			if("CountryCode".equals(qName)) CountryCode = sContent;
			

			// Write output
			if("DataRecords".equals(qName)) {
				fresult.append(YearWeek+fieldSep+Site+fieldSep+EAN+fieldSep+NetSalesQty+fieldSep+NetSales+fieldSep+UnitPrice+fieldSep+"\""+ArticleDescription+"\""+fieldSep+ContentUnit+fieldSep+NetContents+fieldSep+Article+fieldSep+PromotionFlag+fieldSep+SalesCurrency+fieldSep+SalesUnit+fieldSep+CountryCode+"\n");
			}

		}

	}

//	  public static void main(String[] args) {
//
//		    try {
//		      InputStream in = new FileInputStream(new File("in.xml"));
//		      OutputStream out = new FileOutputStream(new File("out.xml"));
//		      ACNielson_Mapping myMapping = new ACNielson_Mapping();
//		      myMapping.execute(in, out);
//		    } catch (Exception e) {
//		      e.printStackTrace();
//		    }
//		  }

}
