-- Вставка данных в таблицу Rating
MERGE INTO rating (rating_id, mpa_rating) KEY(rating_id) VALUES (1, 'G');
MERGE INTO rating (rating_id, mpa_rating) KEY(rating_id) VALUES (2, 'PG');
MERGE INTO rating (rating_id, mpa_rating) KEY(rating_id) VALUES (3, 'PG-13');
MERGE INTO rating (rating_id, mpa_rating) KEY(rating_id) VALUES (4, 'R');
MERGE INTO rating (rating_id, mpa_rating) KEY(rating_id) VALUES (5, 'NC-17');

-- Вставка данных в таблицу Genre
MERGE INTO genre (genre_id, genre_name) KEY(genre_id) VALUES (1, 'Комедия');
MERGE INTO genre (genre_id, genre_name) KEY(genre_id) VALUES (2, 'Драма');
MERGE INTO genre (genre_id, genre_name) KEY(genre_id) VALUES (3, 'Мультфильм');
MERGE INTO genre (genre_id, genre_name) KEY(genre_id) VALUES (4, 'Триллер');
MERGE INTO genre (genre_id, genre_name) KEY(genre_id) VALUES (5, 'Документальный');
MERGE INTO genre (genre_id, genre_name) KEY(genre_id) VALUES (6, 'Боевик');