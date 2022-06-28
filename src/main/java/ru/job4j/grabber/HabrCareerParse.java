package ru.job4j.grabber;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import ru.job4j.grabber.utils.DateTimeParser;
import ru.job4j.grabber.utils.Parse;
import ru.job4j.quartz.AlertRabbit;
import ru.job4j.quartz.Post;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;


public class HabrCareerParse implements DateTimeParser, Parse {
    private static final String SOURCE_LINK = "https://career.habr.com";
    private final DateTimeParser dateTimeParser;

    public HabrCareerParse(DateTimeParser dateTimeParser) {
        this.dateTimeParser = dateTimeParser;
    }

    @Override
    public LocalDateTime parse(String parse) {
        ZonedDateTime utc = ZonedDateTime.parse(parse);
        return utc.toLocalDateTime();

    }

    public Post getPost(Element element, String link) throws IOException {
        String title = element.select(".page-title_title").first().text();
        String description = element.select("style-ugc").first().text();
        String dateTime = element.select("job_show_header__date").first().attr("datetime");
        LocalDateTime time = dateTimeParser.parse(dateTime);
        return new Post(1, title, link, description, time);
    }

    @Override
    public List<Post> list(String link) throws IOException {
        List<Post> list = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            String PAGE_LINK = String.format("%s/vacancies/java_developer?page=" + (i + 1), SOURCE_LINK);
            Connection connection = Jsoup.connect(PAGE_LINK);
            Document document = connection.get();
            Elements rows = document.select(".vacancy-card__inner");
            rows.forEach(row -> {
                Element infoElement = row.select(".vacancy-card__title").first();
                String infoCard = infoElement.attr("href");
                String connectLink = "https://career.habr.com/" + infoCard;
                Connection connect = Jsoup.connect(connectLink);
                    try {
                        Document doc = connect.get();
                        Element innerPost = doc.select("vacancy-show").first();
                        list.add(getPost(innerPost,connectLink));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
            });
        }
        return list;
    }
}