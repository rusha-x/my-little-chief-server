-- Создать тип buildings
CREATE TABLE buildings
(
-- обявляем колонку "_id" целочисленного типа, которая является
-- основаным ключом (идентификатором строчки таблицы)
-- AUTOINCREMENT означает что если не передать значение
-- этой колонки при добавлении строчки, то оно будет равно
-- значению _id в предыдущей строчки+1
    _id            INTEGER PRIMARY KEY AUTOINCREMENT,
-- NOT NULL означает, что значение для этой колонки
-- всегда должно быть передано и оно не может быть NULL
    address        TEXT NOT NULL,
-- REAL - это дробные числа
    weight_in_tons REAL NOT NULL
);

CREATE TABLE doors
(
    _id         INTEGER PRIMARY KEY AUTOINCREMENT,
-- REFERENCES означает, что в этой колонке будет лежать
-- PRIMARY KEY из другой таблицы. Так в SQL выражаются
-- связи между таблицами
    building_id INTEGER NOT NULL REFERENCES buildings
-- при удалении здания дверь тоже удалится
        ON UPDATE CASCADE
-- при обновлении PRIMARY KEY здания building_id тоже обновится
        ON DELETE CASCADE,
    text        TEXT
);

-- добавить строчку в таблицу buildings
-- (_id, address, weight_in_tons) - названия параметров
-- (0, 'Lenina 15', 15.0) - значения параметров
INSERT INTO buildings (_id, address, weight_in_tons)
VALUES (0, 'Lenina 15', 15.0);

INSERT INTO doors (building_id, text)
VALUES (0, 'Do not disturb');

INSERT INTO doors (building_id, text)
VALUES (0, NULL);

-- обновить все записи в таблице doors
UPDATE doors
-- и устаноить у них text в 'Disturb please'
SET text = 'Disturb please'
-- где _id равен 0
WHERE _id = 0;


SELECT text FROM doors WHERE building_id = 0;

SELECT * FROM doors WHERE building_id IN(SELECT _id FROM buildings WHERE address = 'Lenina 15');