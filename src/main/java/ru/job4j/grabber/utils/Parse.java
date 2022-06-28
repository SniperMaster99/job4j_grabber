package ru.job4j.grabber.utils;

import ru.job4j.quartz.Post;

import java.io.IOException;
import java.util.List;

public interface Parse {
    List<Post> list(String link) throws IOException;
}
