package ru.worksolutions.newsrestservice.controllers;

import org.json.JSONStringer;
import org.json.JSONWriter;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

@ControllerAdvice
public class ExceptionsController {
    @ExceptionHandler(Throwable.class)
    @ResponseBody
    @ResponseStatus(HttpStatus.OK)
    public String ExceptionsController(Throwable e) {
        JSONWriter jw = new JSONStringer();
        jw.object();
        jw.key("success");
        jw.value(false);
        jw.key("error");
        jw.value(e);
        jw.endObject();
        return jw.toString();
    }
}
