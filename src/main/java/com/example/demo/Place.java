package com.example.demo;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "places")
public class Place {
    @Id
    private String id;
    private String name;
    private String description;
    private String code;

    public Place() {}

    public Place(String name) {
        this.name = name;
    }
    
    public Place(String name, String code, String description) {
        this.name = name;
        this.code = code;
        this.description = description;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
}