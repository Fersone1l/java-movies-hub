package ru.practicum.moviehub.store;

import ru.practicum.moviehub.model.Movie;

import java.util.*;
import java.util.stream.Collectors;

public class MoviesStore {
    private final Map<Integer, Movie> movies = new HashMap<>();

    private Integer generatedId = 0;

    public Movie save(Movie movie) {
        if (Objects.nonNull(movie)) {
            movie.setId(generateId());
        }
        movies.put(movie.getId(), movie);
        return movie;
    }

    public void clear() {
        this.movies.clear();
        this.generatedId = 0;
    }

    public Integer generateId() {
        return ++generatedId;
    }

    public List<Movie> getAll() {
        return new ArrayList<>(movies.values());
    }

    public Optional<Movie> getById(Integer id) {
        return Optional.ofNullable(movies.get(id));
    }

    public boolean delete(Integer id) {
        return movies.remove(id) != null;
    }

    public List<Movie> getByYear(int year) {
        return movies.values().stream()
                .filter(movie -> movie.getYear() == year)
                .collect(Collectors.toList());
    }
}