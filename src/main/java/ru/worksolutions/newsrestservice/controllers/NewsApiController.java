package ru.worksolutions.newsrestservice.controllers;

import org.json.JSONObject;
import org.json.JSONStringer;
import org.json.JSONWriter;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.worksolutions.newsrestservice.models.MeduzaNews;
import ru.worksolutions.newsrestservice.repositories.MeduzaNewsRepository;

import javax.servlet.http.HttpServletRequest;
import java.io.*;
import java.net.URL;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.GZIPInputStream;

@RestController
@RequestMapping("/news")
public class NewsApiController {

    private final static String MEDUZA_API_URL = "https://meduza.io/api/v3/index";

    private MeduzaNewsRepository meduzaNewsRepository;

    public NewsApiController(MeduzaNewsRepository meduzaNewsRepository) {
        this.meduzaNewsRepository = meduzaNewsRepository;
    }

    private String readMeduzaJSON() throws IOException {
        URL url = new URL(MEDUZA_API_URL);
        try (
                InputStream inputStream = url.openStream();
                GZIPInputStream gzipInputStream = new GZIPInputStream(inputStream);
                InputStreamReader inputStreamReader = new InputStreamReader(gzipInputStream);
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader)
        ) {
            StringBuilder content = new StringBuilder();
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                content.append(line);
            }
            return content.toString();
        }
    }

    private List<MeduzaNews> parseMeduzaJSON(String json) {
        List<MeduzaNews> list = new ArrayList<>();
        JSONObject entireResponce = new JSONObject(json);
        JSONObject documents = entireResponce.getJSONObject("documents");
        for (String id : documents.keySet()) {
            JSONObject document = documents.getJSONObject(id);
            MeduzaNews meduzaNews = new MeduzaNews();
            meduzaNews.newsID = id;
            if (document.has("pub_date")) {
                meduzaNews.day = document.getString("pub_date");
            }
            if (document.has("title")) {
                meduzaNews.title = document.getString("title");
            }
            if (document.has("url")) {
                meduzaNews.url = document.getString("url");
            }
            if (document.has("image")) {
                meduzaNews.imageSmallURL = document.getJSONObject("image").getString("small_url");
            }
            list.add(meduzaNews);
        }
        return list;
    }

    @PostMapping(value = "/update", produces = MediaType.APPLICATION_JSON_VALUE)
    public String updateNews() throws IOException {
        List<MeduzaNews> meduzaNews = parseMeduzaJSON(readMeduzaJSON());
        meduzaNewsRepository.saveAll(meduzaNews);
        return generateJSONResponse();
    }

    @GetMapping(value = "/list", produces = MediaType.APPLICATION_JSON_VALUE)
    public String listNews(@RequestParam(value = "day", required = false) String day) throws Exception {
        if (day == null) {
            throw new Exception("В запросе не была указана дата - параметр day.");
        }
        if (day.toLowerCase().equals("today")) {
            day = LocalDate.now().toString();
        } else {
            day = LocalDate.parse(day).toString(); // проверяем дату на допустимость (DateTimeParseException)
        }
        List<MeduzaNews> list = meduzaNewsRepository.findNewsByDay(day);
        if (list.isEmpty()) {
            throw new Exception("Нет новостей за запрошенную дату.");
        }
        return generateJSONResponse(list);
    }

    @GetMapping(value = "/image/**")
    public ResponseEntity<String> getNewsImage(HttpServletRequest request) throws Exception {
        String fullPath = request.getRequestURI();
        String subPath = fullPath.substring(fullPath.indexOf("/image/") + 7);
        MeduzaNews news = meduzaNewsRepository.findNewsByNewsID(subPath);
        if (news == null) {
            throw new Exception("Не найдено новости с указанным идентификатором.");
        }
        if (news.imageSmallURL == null) {
            throw new Exception("У этой новости нет картинки.");
        }
        String imageURL = "https://meduza.io" + news.imageSmallURL;
        HttpHeaders header = new HttpHeaders();
        header.set("location", imageURL);
        return new ResponseEntity<>("", header, HttpStatus.TEMPORARY_REDIRECT);
    }

    private String generateJSONResponse() {
        return generateJSONResponse(null);
    }

    private String generateJSONResponse(List<MeduzaNews> list) {
        JSONWriter jw = new JSONStringer();
        jw.object();
        jw.key("success");
        jw.value(true);
        if(list != null) {
            jw.key("news");
            jw.array();
            for (MeduzaNews news : list) {
                jw.object();
                jw.key("title");
                jw.value(news.title);
                jw.key("url");
                jw.value(news.url);
                jw.endObject();
            }
            jw.endArray();
        }
        jw.endObject();
        return jw.toString();
    }

}
