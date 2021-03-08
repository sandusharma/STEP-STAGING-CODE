package se.sigma.sallinggroup;
/*
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.runner.RunWith;
 */

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.util.Assert;
import se.sigma.sallinggroup.mongodbmodel.entity.*;

import java.util.List;

import static org.junit.Assert.*;

//@RunWith(Arquillian.class)
public class StepToJsonTest {
    /*@Deployment
    public static JavaArchive createDeployment() {
        return ShrinkWrap.create(JavaArchive.class)
                .addClass(StepToJson.class)
                .addAsManifestResource(EmptyAsset.INSTANCE, "beans.xml");
    }*/

    private ApplicationContext context;
    private List<GoldenRecordDocument> documents = null;

    @org.junit.Before
    public void setUp() throws Exception {
        context = new ClassPathXmlApplicationContext("beans-test.xml");

        StepToJson stepToJson = context.getBean(StepToJson.class);
        assertNotNull(stepToJson);
        Resource resource = new ClassPathResource("test/samplestepxml.xml");
        try {
            documents = stepToJson.processStepxmlFile(resource.getFile());
            assertNotNull(documents);
        }catch( Exception e ) {
            fail("Unable to read stepxml file " + e.getMessage());
        }
    }

    @org.junit.After
    public void tearDown() throws Exception {
    }

    @org.junit.Test
    public void testHeader() {
        assertEquals(5, documents.size());

        // First record
        GoldenRecordDocument doc1 = documents.get(0);

        assertEquals("GR2771970", doc1.getHeader().getStepId());
        assertNotNull(doc1.getHeader().getCompleteness_level());
        assertEquals(2, doc1.getHeader().getCompleteness_level().size());

        assertEquals("Samsung Galaxy A20e - koralorange - 4G - 32 GB - GSM - smartphone \"med mera\"", doc1.getHeader().getName());

        // Check for level 4
        CompletenessLevel l4 = doc1.getHeader().getCompleteness_level().stream().filter(x -> x.getValueId().equals("ProductDataLevel4")).findFirst().orElse(null);
        assertNotNull(l4);
        assertEquals("ProductDataLevel4", l4.getValueId());
        assertEquals("Level 4", l4.getValue());

        // Check for level 5
        CompletenessLevel l5 = doc1.getHeader().getCompleteness_level().stream().filter(x -> x.getValueId().equals("ProductDataLevel5")).findFirst().orElse(null);
        assertNotNull(l5);
        assertEquals("ProductDataLevel5", l5.getValueId());
        assertEquals("Top Product", l5.getValue());

        assertEquals("ECC:91050600010-EA", doc1.getHeader().getExternalIds().get(0));
        assertEquals("ECC:91050600011-EA", doc1.getHeader().getExternalIds().get(1));
        assertEquals("GoldenRecord", doc1.getHeader().getObjectType());
        assertNotNull(doc1.getHeader().getGtinIds().stream().filter( x->x.equals("7311170037028")).findFirst().orElse(null));
        assertNotNull(doc1.getHeader().getGtinIds().stream().filter( x->x.equals("5710405071707")).findFirst().orElse(null));
        assertNotNull(doc1.getHeader().getGtinIds().stream().filter( x->x.equals("8801643870416")).findFirst().orElse(null));
        assertEquals("NotCategorizedL2", doc1.getHeader().getConsumerFacingHierarchy());
        assertNotNull(doc1.getHeader().getUpdated());
        assertNotNull(doc1.getHeader().getSerial());
    }

    @org.junit.Test
    public void testMultivalue() {
        assertEquals(5, documents.size());

        // Find attribute group of master data
        GoldenRecordDocument doc1 = documents.get(0);
        assertNotNull(doc1.getAttributeGroups());

        AttributeGroup agMasterdata = doc1.getAttributeGroups().stream().filter(x->x.getAttributeGroupId().equals("MasterData")).findFirst().orElse(null);
        assertNotNull(agMasterdata);

        AttributeGroup agDimensions = doc1.getAttributeGroups().stream().filter(x->x.getAttributeGroupId().equals("DimensionsAndWeight")).findFirst().orElse(null);
        assertNotNull(agDimensions);

        Attribute attrGtinList = agMasterdata.attributes.stream().filter(x->x.getAttributeId().equals("GTINList")).findFirst().orElse(null);
        assertNotNull(attrGtinList);
        assertEquals(3, attrGtinList.getValues().size());
        assertNull(attrGtinList.getValue());
        assertEquals("7311170037028", attrGtinList.getValues().get(0).getValue());

        // Check for valid metadata for sales rules with a DisplaySequence of 1015
        Attribute attrSalesRules = agMasterdata.attributes.stream().filter(x->x.getAttributeId().equals("SalesRules")).findFirst().orElse(null);
        assertNotNull(attrSalesRules);
        assertNotNull(attrSalesRules.getMetadata());
        assertEquals(1, attrSalesRules.getMetadata().size());
        assertEquals("1015", attrSalesRules.getMetadata().get(0).getValue().getValue());

        // Check for article type
        Attribute attrArticleType = agMasterdata.attributes.stream().filter(x->x.getAttributeId().equals("ArticleType")).findFirst().orElse(null);
        assertNotNull(attrArticleType);
        assertNull(attrArticleType.getValues());
        assertNotNull(attrArticleType.getValue());
        assertEquals("ZDSV", attrArticleType.getValue().getValueId());
        assertEquals("Dropship Vendor articles (e-commerce)", attrArticleType.getValue().getValue());


        // Product specificationw
        AttributeGroup agProductsSpecifications = doc1.getAttributeGroups().stream().filter(x->x.getAttributeGroupId().equals("ProductsSpecifications")).findFirst().orElse(null);
        assertNotNull(agProductsSpecifications);

        Attribute attrCnetTest1 = agProductsSpecifications.attributes.stream().filter(x->x.getAttributeId().equals("CNET.A01502")).findFirst().orElse(null);
        assertNotNull(attrCnetTest1);

        SingleValue svflac = attrCnetTest1.getValues().stream().filter(x->x.getValue().equals("FLAC")).findFirst().orElse(null);
        assertNotNull(svflac);
        assertEquals("FLAC", svflac.getValue());
        assertEquals("CNET.K105857", svflac.getValueId());

        SingleValue svSpecial = attrCnetTest1.getValues().stream().filter(x->x.getValueId().equals("CNET.K174660")).findFirst().orElse(null);
        assertNotNull(svSpecial);
        assertEquals("XMF&MER", svSpecial.getValue());
        assertEquals("CNET.K174660", svSpecial.getValueId());

        Attribute attrWidth = agDimensions.attributes.stream().filter(x->x.getAttributeId().equals("Width")).findFirst().orElse(null);
        assertNotNull(attrWidth);
        assertNotNull(attrWidth.getMetadata());
        assertTrue(attrWidth.getMetadata().size() > 0 );
        assertEquals("51", attrWidth.getMetadata().get(0).getValue().getValue());
        assertEquals("AttributeDisplaySequence", attrWidth.getMetadata().get(0).getAttributeId());


    }

    @org.junit.Test
    public void testDeleted() {
        assertEquals(5, documents.size());

        GoldenRecordDocument doc = documents.stream().filter(x->x.getHeader().getStepId().equals("IF8612202")).findFirst().get();
        assertNotNull(doc);
        assertEquals(true, doc.getDeleted());

        // Other documents are supposed to be true
        assertEquals(null, documents.get(0).getDeleted());

    }

}
