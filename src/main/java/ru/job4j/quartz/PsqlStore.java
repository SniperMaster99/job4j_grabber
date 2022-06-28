package ru.job4j.quartz;

import ru.job4j.grabber.utils.Store;

import java.sql.*;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class PsqlStore implements Store, AutoCloseable {

    private Connection connection;

    public PsqlStore(Properties configure) throws SQLException {
        try {
            Class.forName(configure.getProperty("jdbc.driver"));
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException(e);
        }
        connection = DriverManager.getConnection(configure.getProperty("url"), configure.getProperty("login"),
                configure.getProperty("password"));
    }

    @Override
    public void close() throws Exception {
        if (connection != null) {
            connection.close();
        }
    }

    @Override
    public void save(Post post) {
        try (PreparedStatement preparedStatement =
                     connection.prepareStatement(("insert into post(id, name, vacancy, link, created) " +
                                     "values (?, ?, ?, ?)"),
                             Statement.RETURN_GENERATED_KEYS)
        ) {
            preparedStatement.setString(1, post.getTitle());
            preparedStatement.setString(2, post.getDescription());
            preparedStatement.setString(3, post.getLink());
            preparedStatement.setString(4, post.getCreated().format(DateTimeFormatter.ofPattern("dd LLLL yyyyy")));
            preparedStatement.execute();
            try (ResultSet genKeys = preparedStatement.getGeneratedKeys()) {
                if (genKeys.next()) {
                    post.setId(genKeys.getInt(1));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public List<Post> getAll() {
        List<Post> list = new ArrayList<>();
        try {
            PreparedStatement preparedStatement = connection.prepareStatement("select * from post");
            ResultSet result = preparedStatement.executeQuery();
            while (result.next()) {
                list.add(new Post(result.getInt("id"),
                        result.getString("name"),
                        result.getString("vacancy"),
                        result.getString("link"),
                        result.getTimestamp("created").toLocalDateTime()));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    @Override
    public Post findByID(int id) {
        Post post = null;
        try {
            PreparedStatement preparedStatement = connection.prepareStatement("select * from post where id = ?");
            preparedStatement.setInt(1, id);
            ResultSet result = preparedStatement.executeQuery();
            post = new Post(result.getInt("id"),
                    result.getString("name"),
                    result.getString("vacancy"),
                    result.getString("link"),
                    result.getTimestamp("created").toLocalDateTime());
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return post;
    }
}
