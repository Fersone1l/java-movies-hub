package ru.practicum.moviehub.http;

import com.google.gson.JsonSyntaxException;
import com.sun.net.httpserver.HttpExchange;
import ru.practicum.moviehub.api.ErrorResponse;
import ru.practicum.moviehub.model.Movie;
import ru.practicum.moviehub.store.MoviesStore;

import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.Year;
import java.util.ArrayList;
import java.util.List;

public class MoviesHandler extends BaseHttpHandler {

    public MoviesHandler(MoviesStore store) {
        super(store);
    }

    @Override
    public void handle(HttpExchange ex) throws IOException {

        String method = ex.getRequestMethod();

        if (method.equalsIgnoreCase("GET")) {
            String query = ex.getRequestURI().getQuery();

            if (query == null) {
                List<Movie> movies = store.getAll();
                sendJson(ex, 200, gson.toJson(movies));
                return;
            }

            if (!query.startsWith("year=")) {
                sendJson(ex, 400,
                        gson.toJson(new ErrorResponse("Некорректный параметр запроса — 'year'")));
                return;
            }

            int year;

            try {
                year = Integer.parseInt(query.substring(5));
            } catch (NumberFormatException e) {
                sendJson(ex, 400,
                        gson.toJson(new ErrorResponse("Некорректный параметр запроса — 'year'")));
                return;
            }

            List<Movie> movies = store.getByYear(year);
            sendJson(ex, 200, gson.toJson(movies));
            return;
        }

        if (method.equalsIgnoreCase("POST")) {

            String contentType =
                    ex.getRequestHeaders().getFirst("Content-Type");

            if (contentType == null ||
                    !contentType.startsWith("application/json")) {

                sendJson(ex, 415,
                        gson.toJson(new ErrorResponse("Unsupported Media Type")));
                return;
            }

            Movie movie;

            try {
                movie = gson.fromJson(
                        new InputStreamReader(ex.getRequestBody(), StandardCharsets.UTF_8),
                        Movie.class
                );
            } catch (JsonSyntaxException e) {
                sendJson(ex, 422,
                        gson.toJson(new ErrorResponse("Ошибка валидации")));
                return;
            }

            List<String> errors = new ArrayList<>();

            if (movie.getTitle() == null || movie.getTitle().isBlank()) {
                errors.add("название не должно быть пустым");
            }

            if (movie.getTitle() != null && movie.getTitle().length() > 100) {
                errors.add("название не должно превышать 100 символов");
            }

            int currentYear = Year.now().getValue();

            if (movie.getYear() < 1888 || movie.getYear() > currentYear + 1) {
                errors.add("год должен быть между 1888 и " + (currentYear + 1));
            }

            if (!errors.isEmpty()) {
                ErrorResponse error =
                        new ErrorResponse("Ошибка валидации", errors);

                sendJson(ex, 422, gson.toJson(error));
                return;
            }

            try {
                Movie saved = store.save(movie);
                sendJson(ex, 201, gson.toJson(saved));
            } catch (IllegalArgumentException e) {
                sendJson(ex, 422, gson.toJson(new ErrorResponse("Ошибка валидации", List.of(e.getMessage()))));
            }
            return;
        }

        sendJson(ex, 405,
                gson.toJson(new ErrorResponse("Method Not Allowed")));
    }
}