package ru.job4j.grabber.utils;

import ru.job4j.quartz.Post;

import java.util.List;

public interface Store {
    void save(Post post);
    List<Post> getAll();
    Post findByID(int id);
}
