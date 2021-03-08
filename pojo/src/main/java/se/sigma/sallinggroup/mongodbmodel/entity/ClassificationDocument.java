package se.sigma.sallinggroup.mongodbmodel.entity;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;

public class ClassificationDocument implements Serializable {

    private static final long serialVersionUID = 5385217222573210788L;

    public String id;
    private ClassificationHeader header;

    @JsonProperty("_id")
    public String getId() {
        return id;
    }

    @JsonProperty("_id")
    public void setId(String id) {
        this.id = id;
    }

    @JsonProperty("header")
    public ClassificationHeader getHeader() {
        return header;
    }

    public void setHeader(ClassificationHeader header) {
        this.header = header;
    }

}
