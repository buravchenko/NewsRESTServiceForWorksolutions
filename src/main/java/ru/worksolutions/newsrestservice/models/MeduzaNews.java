package ru.worksolutions.newsrestservice.models;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

@Document(collection = "MeduzaNews")
public class MeduzaNews {
    @Id
    public String newsID;
    @Indexed(unique = false)
    @Field(value = "Day")
    public String day;
    @Field(value = "Title")
    public String title;
    @Field(value = "URL")
    public String url;
    @Field(value = "Image_Small_URL")
    public String imageSmallURL;
}
