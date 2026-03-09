package ru.practicum.moviehub.http;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import ru.practicum.moviehub.api.ErrorResponse;
import ru.practicum.moviehub.model.Movie;
import ru.practicum.moviehub.store.MoviesStore;

import java.io.IOException;
import java.util.Optional;

public class MovieHandler extends BaseHttpHandler {

    private final MoviesStore store;
    private final Gson gson = new Gson();

    public MovieHandler(MoviesStore store) {
        this.store = store;
    }

    @Override
    public void handle(HttpExchange ex) throws IOException {
        String path = ex.getRequestURI().getPath();
        String[] parts = path.split("/");

        if (parts.length < 3) {
            sendJson(ex, 400, gson.toJson(new ErrorResponse("Некорректный ID")));
            return;
        }

        int id;

        try {
            id = Integer.parseInt(parts[2]);
        } catch (NumberFormatException e) {
            sendJson(ex, 400, gson.toJson(new ErrorResponse("Некорректный ID")));
            return;
        }

        String method = ex.getRequestMethod();

        if (method.equalsIgnoreCase("GET")) {
            Optional<Movie> movie = store.getById(id);
            if (movie.isEmpty()) {
                sendJson(ex, 404, gson.toJson(new ErrorResponse("Фильм не найден")));
                return;
            }

            sendJson(ex, 200, gson.toJson(movie.get()));
            return;
        }

        if (method.equalsIgnoreCase("DELETE")) {
            boolean removed = store.delete(id);
            if (!removed) {
                sendJson(ex, 404, gson.toJson(new ErrorResponse("Фильм не найден")));
                return;
            }

            sendNoContent(ex);
            return;
        }

        sendJson(ex, 405, gson.toJson(new ErrorResponse("Method Not Allowed")));
    }
}
