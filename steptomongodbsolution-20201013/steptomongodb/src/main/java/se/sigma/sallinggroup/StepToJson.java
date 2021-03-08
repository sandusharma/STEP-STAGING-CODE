package se.sigma.sallinggroup;

import se.sigma.sallinggroup.mongodbmodel.entity.*;
import se.sigma.sallinggroup.stepxml.entity.*;
import se.sigma.sallinggroup.stepxml.parser.*;

import java.io.*;

import java.time.ZoneId;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.time.LocalDateTime;
import java.text.SimpleDateFormat;
import java.text.DateFormat;


public class StepToJson {


    //PUT THIS VARIABLE IN THE PROPERTIES FILE TO HANDLE VISIBILITY OF ATTRIBUTE GROUPS
    private String attribute_group_validity_metadata_field; //MetaDataID separated with # and value to include the AttributeGroup e.g. ATTRGRPVALIDFORMONGODB#Yes
    private String valid_reference_types; //Provide reference type ids separated by ";"
    private String valid_classification_links; //Provide Classification link type ids separated by ";"
    private String valid_asset_types; //Provide Asset Type ids separated by ";"
    private String assetExternalIdAttributeId;
    private boolean includeAttributeMetadata; // Include asset metadata in JSON
    private String valid_metadata_attributes; // Valid attribute metadata

    public StepToJson(String attribute_group_validity_metadata_field, String valid_reference_types, String valid_classification_links, String valid_asset_types) {
        this.attribute_group_validity_metadata_field = attribute_group_validity_metadata_field;
        this.valid_reference_types = valid_reference_types;
        this.valid_classification_links = valid_classification_links;
        this.valid_asset_types = valid_asset_types;
        this.includeAttributeMetadata = true;
    }

    public StepToJson() {

    }

    public String getAssetExternalIdAttributeId() {
        return this.assetExternalIdAttributeId;
    }
    public void setAssetExternalIdAttributeId(String assetExternalIdAttributeId) {
        this.assetExternalIdAttributeId = assetExternalIdAttributeId;
    }

    public void setIncludeAttributeMetadata(boolean includeMetadata) {
        this.includeAttributeMetadata = includeMetadata;
    }
    public boolean isIncludeAtttributeMetadata() {
        return this.includeAttributeMetadata;
    }
/*
    public static void main(String[] args) throws FileNotFoundException {
        System.out.println(attribute_group_validity_metadata_field.split("#")[0] + " " + attribute_group_validity_metadata_field.split("#")[1]);
        if (args.length == 0 || args[0].equals("hotfolder")) {

            while (true) {

                try {
                    for (Path path1 : Files.newDirectoryStream(Paths.get(hotfolder), path -> path.toFile().isFile() && path.toFile().toString().endsWith((".xml")))) {
                        ProcessStepxmlFile(path1.toFile());
                        processedFile(path1);
                    }

                } catch (IOException e) {
                    e.printStackTrace();
                }

                hotfolderTimeIntervalCheck(5);

            }


        } else if (args.length == 1 || args[0].equals("client")) {
            ActAsClient();
        }
    }*/

    private static void ActAsClient() {
        //Setup MongoDB Client
    }

    //StaX transformation for products
    public List<ClassificationDocument> processClassificationStepXml(File f) {
        System.out.println("PROCESSING STEP XML FILE " + f);
        List<ClassificationDocument> classificationDocumentsdocuments = new ArrayList<>();

        /**
         * START HERE
         */

        StreamingXmlGenericParser genericParser = new StreamingXmlGenericParser();
        HashMap<String, XmlClassification> classifications = new HashMap<String, XmlClassification>();
        int createdCounter = 0;
        int updatedCounter = 0;

        HashMap genericLists = genericParser.FindAll(f.toString());

        for (XmlClassification cls : (List<XmlClassification>) genericLists.get("classifications")) {
            classifications.put(cls.getStepId(), cls);
        }

        for (String key1 : classifications.keySet()) {

            XmlClassification root_cls = classifications.get(key1);

            HashMap<String, XmlClassification> flattenedClassificationHierarchy = new HashMap<String, XmlClassification>();
            root_cls.flattenHierarchy(root_cls, flattenedClassificationHierarchy);

            for (String key : flattenedClassificationHierarchy.keySet()) {
                ClassificationDocument doc = new ClassificationDocument();
                XmlClassification item = flattenedClassificationHierarchy.get(key);

                doc.setHeader(new ClassificationHeader());
                doc.getHeader().setStepId(item.getStepId());
                if (item.getName() != null) {
                    if (item.getName().get("DEFAULT") != null) {
                        doc.getHeader().setName(item.getName().get("DEFAULT"));
                    }
                }

                doc.getHeader().setObjectType(item.getUserTypeId());
                doc.getHeader().setSerial(LocalDateTime.now().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli());

                doc.getHeader().setUpdated(getTodaysISODate());

                doc.getHeader().setParentId(item.getParentId());
                
      //Code added for populating metadata value for classifications          
                if(item.getValues()!=null)
                {
                	List<Attribute> metalist = new ArrayList<Attribute>();
                    doc.getHeader().setMetadata(metalist);
                	
               	for(XmlProductAttributeValue mdv : item.getValues())
                	{
               				
                		Attribute attribute = new Attribute();
                		
                		 attribute.setAttributeId(mdv.getAttributeId());
                         //attribute.setName(attrs.get(attribute.getAttributeId()).getName());
                         //attribute.setMultiValue(attrs.get(attribute.getAttributeId()).getMultiValue());
                         attribute.setValue(new SingleValue());
                         attribute.getValue().setValue(mdv.getValue());
                        attribute.getValue().setValueId(mdv.getValueId());
                         attribute.getValue().setUnitId(mdv.getUnitId());
                		
                	try {
                	doc.getHeader().getMetadata().add(attribute);
                	}
                	catch(Exception e){
                	System.out.println(e);}
                	}
                	
                }
              //Code added for populating metadata value for classifications 
                
                
                //JsonSerializer serializer = new JsonSerializer();
                //serializer.NullValueHandling = NullValueHandling.Ignore;
            /*
            JSONObject jsonDocument = new JSONObject(doc);
            OutputStream os = null;
            try {

                os = new FileOutputStream("D:\\mongodb\\hotfolder" + "\\data\\" + item.getStepId().replaceAll("/", "") + ".json");
                OutputStreamWriter sw = new OutputStreamWriter(os);
                sw.write(jsonDocument.toString(4));
                //System.out.println(jsonDocument.toString(4));
                //JSONWriter writer = new JSONWriter(sw);
                sw.close();
            } catch (Exception e) {
                e.printStackTrace();
            }

            }
            */
                classificationDocumentsdocuments.add(doc);
            }

        }
        return classificationDocumentsdocuments;

    }


    public List<GoldenRecordDocument> processStepxmlFile(File f) {
        System.out.println("PROCESSING STEP XML FILE " + f);

        return processStepxmlFile(f.toString());

    }

    public List<GoldenRecordDocument> processStepxmlFile(String inputXml) {
        List<GoldenRecordDocument> goldenRecordDocuments = new ArrayList<>();

        /**
         * START HERE
         */

        StreamingXmlProductParser productParser = new StreamingXmlProductParser();
        StreamingXmlGenericParser genericParser = new StreamingXmlGenericParser();
        HashMap<String, XmlStepAttribute> attributes = new HashMap<String, XmlStepAttribute>();
        HashMap<String, XmlClassification> classifications = new HashMap<String, XmlClassification>();
        HashMap<String, XmlAttributeGroup> attributeGroups = new HashMap<String, XmlAttributeGroup>();
        HashMap<String, XmlAsset> assets = new HashMap<String, XmlAsset>();
        HashMap<String, XmlUnit> units = new HashMap<String, XmlUnit>();

        int createdCounter = 0;
        int updatedCounter = 0;

        HashMap genericLists = genericParser.FindAll(inputXml);

        for (XmlStepAttribute attr : (List<XmlStepAttribute>) genericLists.get("attributes")) {
            attributes.put(attr.getId(), attr);
          
        }
       

        for (XmlAttributeGroup attrgrp : (List<XmlAttributeGroup>) genericLists.get("attributegroups")) {
            attributeGroups.put(attrgrp.getStepId(), attrgrp);
        }

        for (XmlClassification cls : (List<XmlClassification>) genericLists.get("classifications")) {
            classifications.put(cls.getStepId(), cls);
        }

        for (XmlAsset asset : (List<XmlAsset>) genericLists.get("assets")) {
            assets.put(asset.getStepId(), asset);
        }

        for (XmlUnit unit : (List<XmlUnit>) genericLists.get("units")) {
            units.put(unit.getStepId(), unit);
        }

        List<StreamingXmlStepProduct> data = productParser.FindAll(inputXml);

        for (StreamingXmlStepProduct item : data) {
            GoldenRecordDocument doc = new GoldenRecordDocument();
            doc.setHeader(new Header());
            doc.getHeader().setStepId(item.getStepId());
            goldenRecordDocuments.add(doc);

            // Handle DELETED documents as documents, but with no properties
            if( item.getDeleted() != null && item.getDeleted()) {
                doc.setDeleted(true);
                continue;
            }

            doc.getHeader().setName(item.getName());
            doc.getHeader().setObjectType(item.getUserTypeId());
            doc.getHeader().setSerial(LocalDateTime.now().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli());
            doc.getHeader().setUpdated(getTodaysISODate());
            doc.getHeader().setVariantIds(item.getVariantIds());

            if (item.getValues() != null && item.getValues().size() != 0) {
                XmlProductAttributeValue gtin = item.getValues().stream().filter(x -> x.getAttributeId().equals("GTINList")).findFirst().orElse(null);
                if (gtin != null) doc.getHeader().setGtinIds(gtin.getMultiValue());
            }
            if (item.getClassificationLinks() != null && item.getClassificationLinks().size() != 0) {
                XmlClassificationLink cfh = item.getClassificationLinks().stream().filter(x -> x.getType().equals("CFHLink")).findFirst().orElse(null);
                if (cfh != null) doc.getHeader().setConsumerFacingHierarchy(cfh.getClassificationId());
            }

            if (item.getValues() != null && item.getValues().size() != 0) {
                XmlProductAttributeValue completenessLevel = item.getValues().stream().filter(x -> x.getAttributeId().equals("ProductDataLevelReached")).findFirst().orElse(null);
                if (completenessLevel != null) {
                    if (completenessLevel.getMultiValue() != null) {
                        doc.getHeader().setCompleteness_level(new ArrayList<CompletenessLevel>());
                        for (int i=0; i<completenessLevel.getMultiValueIds().size(); i++) {
                            CompletenessLevel l = new CompletenessLevel();
                            l.setValueId(completenessLevel.getMultiValueIds().get(i));
                            l.setValue(completenessLevel.getMultiValue().get(i));
                            doc.getHeader().getCompleteness_level().add(l);
                        }
                    }
                }

                doc.getHeader().setExternalIds(new ArrayList<String>());
                XmlProductAttributeValue externalIds = item.getValues().stream().filter(x -> x.getAttributeId().equals("ExternalIDs")).findFirst().orElse(null);

                if (externalIds != null && externalIds.getValue() != null ) {
                    String[] parts = externalIds.getValue().split(";");
                    for( String part: parts ) {
                        doc.getHeader().getExternalIds().add(part);
                    }
                }

            }

            populateAttributeData(units, attributes, attributeGroups, doc, item);
            populateAssetData(attributes, assets, doc, item);
            populateClassificationData(attributes, classifications, doc, item);
            populateReferences(attributes, doc, item);

            //JSONObject jsonDocument = new JSONObject(doc);
            //OutputStream os = null;
            //try {
            //os = new FileOutputStream("D:\\mongodb\\hotfolder" + "\\data\\" + item.getStepId() + ".json");
            // OutputStreamWriter sw = new OutputStreamWriter(os);
            // sw.write(jsonDocument.toString(4));
            //System.out.println(jsonDocument.toString(4));
            //JSONWriter writer = new JSONWriter(sw);
            //sw.close();
            //} catch (Exception e) {
            //    e.printStackTrace();
            //}

        }

        return goldenRecordDocuments;

    }

    private void populateAttributeData(HashMap<String,XmlUnit> units,  HashMap<String, XmlStepAttribute> attrs, HashMap<String, XmlAttributeGroup> attrgrps, GoldenRecordDocument doc, StreamingXmlStepProduct p) 
    {
        doc.setAttributeGroups(new ArrayList<AttributeGroup>());
        
        //valid attributes to be populated
        List<String> reqattr = new ArrayList<String>();
        for(XmlProductAttributeValue validattr : p.getValues())
        {
        	if(validattr.getAttributeId().equalsIgnoreCase("AtrValidOnBrick"))
        	{
        		
        		 reqattr= Arrays.asList(validattr.getValue().trim().split(";"));
        		 reqattr.replaceAll(String::trim);
        		for(int i =0;i<reqattr.size();i++)
        		{
        			
        			System.out.println(reqattr.get(i));
        		}
        	}
        }//valid attributes to be populated
        
        // Create a valid list of attribute types
        List<String> validAttributeMetadata = new ArrayList<String>();
        if( this.getValid_metadata_attributes() != null ) {

            Arrays.stream(this.getValid_metadata_attributes().split(";")).forEach(x->validAttributeMetadata.add(x));

        }

        if (p.getValues() != null && p.getValues().size() != 0) {

            for (XmlProductAttributeValue stepAttribute : p.getValues()) {
            	//valid attributes to be populated
            	System.out.println(stepAttribute.getAttributeId());
            	if(reqattr.contains(stepAttribute.getAttributeId())){
            	
                if (attrs != null && attrs.size() != 0) {

                    XmlStepAttribute attributeDef = attrs.get(stepAttribute.getAttributeId());

                    if (attributeDef != null && attributeDef.getAttributeGroupIds() == null) continue;

                    for (String attributeGroupId : attributeDef.getAttributeGroupIds()) {

                        boolean valid_attribute_group = false;

                        //Find attribute group and create if it does not exist
                        AttributeGroup storedGroup = doc.getAttributeGroups().stream().filter(x -> x.getAttributeGroupId().equals(attributeGroupId)).findFirst().orElse(null);
                        if (storedGroup == null) {
                            storedGroup = new AttributeGroup();
                            storedGroup.setAttributeGroupId(attributeGroupId);
                            storedGroup.setAttributes(new ArrayList<Attribute>());

                            for (String key : attrgrps.keySet()) {
                                XmlAttributeGroup attrgrp = attrgrps.get(key);
                                //System.out.println(attrgrp.getStepId() + " " + attributeGroupId);
                                XmlAttributeGroup found = attrgrp.search(attrgrp, attributeGroupId);

                                if (found != null) {
                                    if (found.getName() != null) {
                                        String name = found.getName().get("DEFAULT");
                                        if (name != null)
                                            storedGroup.setName(name);
                                    }

                                    if (attribute_group_validity_metadata_field != null) {
                                        if (found.getValues() != null) {
                                            XmlProductAttributeValue valid_for_ecommerce = found.getValues().stream().filter(x -> x.getAttributeId().equals(attribute_group_validity_metadata_field.split("#")[0])).findFirst().orElse(null);
                                            if (valid_for_ecommerce != null) {
                                                if (valid_for_ecommerce.getValue().equals(attribute_group_validity_metadata_field.split("#")[1])) {
                                                    valid_attribute_group = true;
                                                }
                                            }
                                        }
                                    }
                                    break;
                                }
                            }

                            if (valid_attribute_group || attribute_group_validity_metadata_field == null || attribute_group_validity_metadata_field.length() == 0) {
                                doc.getAttributeGroups().add(storedGroup);
                            }


                        }

                        Attribute attr0 = new Attribute();
                        attr0.setAttributeId(stepAttribute.getAttributeId());
                        //attr0.setMultiValue(attributeDef.getMultiValue());
                        attr0.setName(attributeDef.getName());

                        if (stepAttribute.getMultiValue() != null) {

                            attr0.setValues(new ArrayList<SingleValue>());

                            for (int i=0; i<stepAttribute.getMultiValue().size(); i++) {
                                String oneValue = stepAttribute.getMultiValue().get(i);
                                String oneKey = stepAttribute.getMultiValueIds().get(i);

                                SingleValue singleValue = new SingleValue();
                                singleValue.setValue(oneValue);
                                singleValue.setValueId(oneKey);
                                attr0.getValues().add(singleValue);
                            }
                        } else if (stepAttribute.getValue() != null) {
                            SingleValue singleValue = new SingleValue();
                            singleValue.setValue(stepAttribute.getValue());
                            attr0.setValue(singleValue);

                            if (stepAttribute.getValueId() != null) {
                                attr0.getValue().setValueId(stepAttribute.getValueId());
                            }

                            attr0.getValue().setUnitId(stepAttribute.getUnitId());
                            if(stepAttribute.getUnitId() != null  && units.containsKey(stepAttribute.getUnitId())) {
                                XmlUnit u = units.get(stepAttribute.getUnitId());
                                XmlProductAttributeValue data = u.getValues() == null ? null :   u.getValues().stream().filter(x->x.getAttributeId() != null &&  x.getAttributeId().equals("UnitDescription")).findFirst().orElse(null);
                                if( data != null ) {
                                    attr0.getValue().setUnit(data.getValue());
                                } else {
                                    attr0.getValue().setUnit(u.getName());
                                }
                            }

                        }

                        attr0.setMetadata(new ArrayList<Attribute>());

                        if( this.isIncludeAtttributeMetadata() ) {
                            if (attributeDef.getValues() != null && attributeDef.getValues().size() != 0) {
                                for (XmlProductAttributeValue value : attributeDef.getValues()) {

                                    // If there are any selections of which attributes that are valid, then consider
                                    // this list. Otherwise, use the default setting for any attribute
                                    if( validAttributeMetadata.size() > 0 ) {
                                        if( !validAttributeMetadata.stream().anyMatch(x->x.equals(value.getAttributeId()))) {
                                            continue;
                                        }
                                    }
                                    Attribute metadata_attr = new Attribute();
                                    metadata_attr.setAttributeId(value.getAttributeId());
                                    SingleValue metadata_singleValue = new SingleValue();
                                    metadata_singleValue.setValue(value.getValue());
                                    metadata_singleValue.setMultiValue(value.getMultiValue());//Line added by sandeep for populating multivalue
                                    metadata_singleValue.setValueId(value.getValueId());
                                    metadata_attr.setValue(metadata_singleValue);
                                    attr0.getMetadata().add(metadata_attr);
                                }
                            }
                        }

                        storedGroup.getAttributes().add(attr0);

                    }
                }
            }
         }
        }
    }

    private static void addAttributeGroupsLookupsRecursive(HashMap<String, XmlAttributeGroup> cls, XmlAttributeGroup parent) {
        if (cls.containsKey(parent.getStepId())) {
            cls.replace(parent.getStepId(), parent);
        }

        if (parent.getChildren() != null) {

            for (XmlAttributeGroup child : parent.getChildren()) {
                addAttributeGroupsLookupsRecursive(cls, child);
            }
        }
    }

    private void populateClassificationData(HashMap<String, XmlStepAttribute> attrs, HashMap<String, XmlClassification> classifications, GoldenRecordDocument doc, StreamingXmlStepProduct p) {

        //SHOULD NEVER BE NULL
        if (classifications != null && classifications.size() != 0) {
            XmlClassification classification_root_node = classifications.get("Classification 1 root");
            if (p.getClassificationLinks() == null || p.getClassificationLinks().size() == 0) {
                return;
            }

            String[] valid_list_classification_links = null;

            if (valid_classification_links != null && !valid_classification_links.equals("")) {
                valid_list_classification_links = valid_classification_links.split(";");
            }
            doc.setClassifications(new ArrayList<Classification>());

            for (XmlClassificationLink cls : p.getClassificationLinks()) {

                if (valid_list_classification_links == null || valid_list_classification_links.length == 0 || Arrays.asList(valid_list_classification_links).contains(cls.getType())) {

                    Classification jsonClassification = new Classification();
                    jsonClassification.setTargetId(cls.getClassificationId());
                    jsonClassification.setTypeId(cls.getType());

                    XmlClassification classification = classification_root_node.search(classification_root_node, cls.getClassificationId());

                    if (classification != null) {
                        jsonClassification.setTargetName(classification.getName().get("DEFAULT"));
                    }

                    if (cls.getMetadata() != null) {
                        for (XmlProductAttributeValue mdv : cls.getMetadata()) {
                            Attribute attribute = new Attribute();
                            attribute.setAttributeId(mdv.getAttributeId());
                            attribute.setName(attrs.get(attribute.getAttributeId()).getName());
                            //attribute.setMultiValue(attrs.get(attribute.getAttributeId()).getMultiValue());
                            attribute.setValue(new SingleValue());
                            attribute.getValue().setValue(mdv.getValue());
                            attribute.getValue().setValueId(mdv.getValueId());
                            attribute.getValue().setUnitId(mdv.getUnitId());

                            if ((jsonClassification.getMetadata() == null || jsonClassification.getMetadata().size() == 0))
                                jsonClassification.setMetadata(new ArrayList<Attribute>());
                            jsonClassification.getMetadata().add(attribute);
                        }
                    }

                    doc.getClassifications().add(jsonClassification);
                }
            }
        }

    }

    private void populateAssetData(HashMap<String, XmlStepAttribute> attrs, HashMap<String,XmlAsset> assets, GoldenRecordDocument doc, StreamingXmlStepProduct p) {
        if (p.getAssetReferences() == null || p.getAssetReferences().size() == 0) {
            return;
        }

        String[] valid_list_asset_types = null;

        if (valid_asset_types != null && !valid_asset_types.equals("")) {
            valid_list_asset_types = valid_asset_types.split(";");
        }

        doc.setAssetGroup(new ArrayList<AssetGroup>());

        for (XmlAssetReference assetreference : p.getAssetReferences()) {

            if (valid_list_asset_types == null || valid_list_asset_types.length == 0 || Arrays.asList(valid_list_asset_types).contains(assetreference.getType())) {


                AssetGroup storedAssetGroup = doc.getAssetGroups().stream().filter(x -> x.getAssetGroupId().equals(assetreference.getType())).findFirst().orElse(null);

                if (storedAssetGroup == null) {
                    storedAssetGroup = new AssetGroup();
                    storedAssetGroup.setAssetGroupId(assetreference.getType());
                    storedAssetGroup.setAssets(new ArrayList<Asset>());
                    doc.getAssetGroups().add(storedAssetGroup);
                }

                Asset jsonAsset = new Asset();
                jsonAsset.setAssetId(assetreference.getAssetId());

                ArrayList<XmlProductAttributeValue> attributeValues = new ArrayList<XmlProductAttributeValue>();
                XmlAsset asset = assets.get(assetreference.assetId);
                if( asset != null && asset.getValues() != null ) {
                    attributeValues.addAll(asset.getValues());
                }
                if( assetreference.getMetadata() != null ) {
                    attributeValues.addAll(assetreference.getMetadata());
                }
                for (XmlProductAttributeValue mdv : attributeValues) {
                    Attribute attribute = new Attribute();
                    attribute.setAttributeId(mdv.getAttributeId());
                    attribute.setName(attrs.get(attribute.getAttributeId()).getName());
                    //attribute.setMultiValue(attrs.get(attribute.getAttributeId()).getMultiValue());
                    attribute.setValue(new SingleValue());
                    attribute.getValue().setValue(mdv.getValue());
                    attribute.getValue().setValueId(mdv.getValueId());
                    attribute.getValue().setUnitId(mdv.getUnitId());

                    if ((jsonAsset.getMetadata() == null || jsonAsset.getMetadata().size() == 0))
                        jsonAsset.setMetadata(new ArrayList<Attribute>());

                    if( assetExternalIdAttributeId != null && assetExternalIdAttributeId.equals(mdv.getAttributeId())) {
                        jsonAsset.setDamIdentifier(mdv.getValue());
                    } else {
                        jsonAsset.getMetadata().add(attribute);
                    }
                }

                storedAssetGroup.getAssets().add(jsonAsset);
            }
        }
    }

    private void populateReferences(HashMap<String, XmlStepAttribute> attrs, GoldenRecordDocument doc, StreamingXmlStepProduct p) {
        if (p.getProductCrossReferences() == null || p.getProductCrossReferences().size() == 0) return;

        String[] valid_list_reference_types = null;

        //Initialize external sources e.g. SAP --> ECC
        Map<String, String> externalSourcesLookUpTable = initialize_ExternalSources();

        if (valid_reference_types != null && !valid_reference_types.equals("")) {
            valid_list_reference_types = valid_reference_types.split(";");
        }


        doc.setReferenceGroups(new ArrayList<ReferenceGroup>());
        for (XmlProductCrossReference reference : p.getProductCrossReferences()) {

            if (valid_list_reference_types == null || valid_list_reference_types.length == 0 || Arrays.asList(valid_list_reference_types).contains(reference.getType())) {


                ReferenceGroup storedReferenceGroup = doc.getReferenceGroups().stream().filter(x -> x.getReferenceGroupId().equals(reference.getType())).findFirst().orElse(null);
                if (storedReferenceGroup == null) {
                    storedReferenceGroup = new ReferenceGroup();
                    storedReferenceGroup.setReferenceGroupId(reference.getType());
                    storedReferenceGroup.setReferences(new ArrayList<Reference>());
                    doc.getReferenceGroups().add(storedReferenceGroup);
                }

                Reference r = new Reference();
                r.setTargetId(reference.getProductid());

                storedReferenceGroup.getReferences().add(r);

                //Reference
                if (reference.getMetadata() != null) {
                    for (XmlProductAttributeValue mdv : reference.getMetadata()) {
                        Attribute attribute = new Attribute();
                        attribute.setAttributeId(mdv.getAttributeId());
                        attribute.setName(attrs.get(attribute.getAttributeId()).getName());
                        //attribute.setMultiValue(attrs.get(attribute.getAttributeId()).getMultiValue());
                        attribute.setValue(new SingleValue());
                        attribute.getValue().setValue(mdv.getValue());
                        attribute.getValue().setValueId(mdv.getValueId());
                        attribute.getValue().setUnitId(mdv.getUnitId());

                        if ((r.getMetadata() == null || r.getMetadata().size() == 0))
                            r.setMetadata(new ArrayList<Attribute>());
                        r.getMetadata().add(attribute);
                    }
                }
            }
        }

    }

    private static void addClassficationLookupsRecursive(HashMap<String, XmlClassification> cls, XmlClassification parent) {
        if (cls.containsKey(parent.getStepId())) {
            cls.replace(parent.getStepId(), parent);
        }

        if (parent.getChildren() != null) {

            for (XmlClassification child : parent.getChildren()) {
                addClassficationLookupsRecursive(cls, child);
            }
        }
    }

    /*
    //MOVED PROCESSED FILES TO PROCESSED FOLDER
    private void processedFile(Path path) throws IOException {

        Files.move(path, Paths.get(hotfolder + "\\processed\\" + LocalDateTime.now().toString().replaceAll(":", "") + "_" + path.toFile().getName()), REPLACE_EXISTING);

    }*/

    //TIMED INTERVAL TO CHECK HOTFOLDER
    public void hotfolderTimeIntervalCheck(int seconds) {
        try {
            TimeUnit.SECONDS.sleep(seconds);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public Map<String, String> initialize_ExternalSources() {
        Map<String, String> externalSources = new HashMap<String, String>();
        externalSources.put("SAP", "ECC");
        return externalSources;
    }

    public String getTodaysISODate() {
        Date date = new Date();
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm'Z'");
        String isoDate = df.format(date);
        return isoDate;
    }

    public String getValid_metadata_attributes() {
        return valid_metadata_attributes;
    }

    public void setValid_metadata_attributes(String valid_metadata_attributes) {
        this.valid_metadata_attributes = valid_metadata_attributes;
    }


}
