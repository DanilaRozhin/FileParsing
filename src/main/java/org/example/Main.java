package org.example;
/*
Подключение различных библиотек. Будем работать с:
JsonSimple - для работы с .json файлами (парсинг, запись)
Jackson - для красивого вывода .json файла, иначе всё будет в одну строчку и нечитаемо
OpenCSV - для работы с .csv файлами (excel) (чтение, запись)
FileReader и FileWriter - для чтения/записи информации из .json и .csv файлов
*/

import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;
import com.opencsv.exceptions.CsvException;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

public class Main {
    public static void main(String[] args) {
        //try-catch - дефолтная структура для работы с файлами и не только
        try {
            //Парсим файлы file.json и task.json, используя JSONParser и FileReader. По итогу данные из файлов превращаются в объекты
            Object jsonObjectsFromFile = new JSONParser().parse(new FileReader("Files/file.json"));
            Object jsonListsFromTask = new JSONParser().parse(new FileReader("Files/task.json"));

            //Полученные объекты превращаем в JSONObject, чтобы дальше с ними можно было работать
            JSONObject objectsFromFile = (JSONObject) jsonObjectsFromFile;
            JSONObject listsFromTask = (JSONObject) jsonListsFromTask;

            //Создаём два массива, в одном будем хранить задачи, которые есть в файле file.json, во втором - пулы с файла task.json
            //Это необходимо для того, чтобы в дальнейшем, в циклах, можно было удобно обращаться как к задачам, так и к пулам по отдельности
            Object[] arrayObjectsFromFile = objectsFromFile.keySet().toArray();
            Object[] arrayListsFromTask = listsFromTask.keySet().toArray();

            //Создаём список, в котором будем хранить задачи, которые используются в list.json
            List<Object> listTrueObjects = new ArrayList<Object>();

            //Цикл: проходим по каждой задачи (из файла file.json) из каждого пула (из файла task.json)
            for (Object objectFromFile: arrayObjectsFromFile) { //цикл по каждой задаче из файла file.json
                for (Object numberListFromTask: arrayListsFromTask) { //цикл по каждому пулу из файла task.json
                    /*
                    каждый пул содержит задачи, которые тоже находятся "в своём .json", поэтому, чтобы до них добраться
                    необходимо сначала "распарсить распаршенный" task.json, а уже потом получить список задач в виде массива.
                    Используем JSONArray, чтобы из .json получить массив (так как в файле он также хранится в виде массива)
                    */
                    JSONObject listFromTask = (JSONObject) listsFromTask.get(numberListFromTask);
                    JSONArray objectsFromListFromTask = (JSONArray) listFromTask.get("list");

                    //Проверяем, содержится ли наша задача из файла file.json в задачах из пула из файла task.json
                    if (objectsFromListFromTask.contains(objectFromFile)) {
                        /*
                        Если да - добавляем задачу в ранее созданный список listTrueObjects и выходим из цикла,
                        потому что нет смысла проверять остальные задачи из пула, если одна из них уже совпала.
                        */
                        listTrueObjects.add(objectFromFile);
                        break;
                    };
                }
            }

            /*
            Создаём json-объект, чтобы в него записывать объекты из списка listTrueObjects и необходимое
            описание к ним (reward, money, details, reputation). Т.е. у нас будет объект, содержащий информацию
            в виде ключ-значение, где ключ - объект из listTrueObjects, а значение - характеристики
            reward, money, details, reputation (которые тоже будут в виде "своего json", которые будем вытаскивать из файла items.csv
            */
            JSONObject trueJson = new JSONObject();

            /*
            try-catch для Reader - база. Нам сейчас нужно вытащить данные (reward, money, details, reputation) из
            файла items.csv. Поэтому открываем его с помощью CSVReader и FileReader
            */
            try (CSVReader csvReader = new CSVReader(new FileReader("Files/items.csv"));) {
                String[] reward; //сюда будем записывать данные, которые считал CSVReader из файла items.csv

                //Пока в файле есть информация, записываем её построчно и переменную reward
                while ((reward = csvReader.readNext()) != null) {
                    /*
                    Наши данные (reward, money, details, reputation) хранятся просто в виде строки, а нам надо получить
                    данные в виде ключ-значение. Для этого нам необходимо сделать словарь и туда записывать все полученные
                    данные. Будем использовать словарь, он же HashMap в Java (хоть и не называется словарём...)
                    */
                    Map<String, String> dataRewards = new HashMap<String, String>();
                    /*
                    Итак, в переменной reward у нас хранится строчка из файла items.csv в виде массива. Каждое значение
                    параметра - отдельная ячейка в массиве. У нас таких параметров 4 (reward, money, details, reputation),
                    поэтому размерность массива - 3 (0, 1, 2, 3). Под индексом 0 - reward (точнее, его значение),
                    под 1 - money и т.д. Для того, чтобы записать полученную информацию, используем ранее созданный HashMap dataRewards,
                    в котором, в качестве ключа устанавливаем параметр (reward, money, details, reputation), а в
                    качестве значения - значение, которое хранится в соответствующей ячейке нашего массива reward.
                    */
                    dataRewards.put("reward", reward[0]);
                    dataRewards.put("money", reward[1]);
                    dataRewards.put("details", reward[2]);
                    dataRewards.put("reputation", reward[3]);

                    /*
                    Теперь у нас есть параметры (описание) для каждой задачи. Однако, каждую задачу нужно сопоставить
                    с правильной наградой. Для этого используем связь между items.csv (параметр reward) и ранее нами
                    заполненным listTrueObjects (так же по параметру reward).
                    */
                    for (Object trueObject: listTrueObjects) { //цикл по всем задачам в заполненном нами списке listTrueObjects
                        JSONObject infoTrueObject = (JSONObject) objectsFromFile.get(trueObject); //получаем информацию о параметрах задачи
                        //сравниваем поля reward у задачи в listTrueObjects и параметра reward у файла items.csv
                        if (infoTrueObject.get("reward").equals(reward[0])) {
                            /*
                            Если параметры совпали - значит, мы нашли нужный объект под наши параметры. Записываем
                            его в ранее созданный json-объект. В качестве ключа указываем наш объект из listTrueObjects,
                            в качестве значения - ранее созданный и заполненный HashMap dataRewards. Наши задачи в списке
                            listTrueObjects не повторяются, а так, как параметр reward - уникальный, то идти по остальным
                            задачам не имеет смысла, так как второй такой задачи, подходяшей под эти же параметры,
                            мы уже не найдём.
                            */
                            trueJson.put(trueObject, dataRewards);
                            break;
                        }
                    }
                }
            } catch (CsvException e) {
                throw new RuntimeException(e);
            }

            /*
            ObjectMapper нужен для сериализованного вывода нашего составленного trueJson.json, если будем просто
            выводить через FileWriter, то получим кашу в виде одной строчки. Фактически, задача всё равно будет
            выполнена, но какой от этого смысл, если это невозможно адекватно прочитать)
            */
            ObjectMapper mapper = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);
            //Наш "красивый" trueJson будем хранить в переменной prettyTrueJson.
            String prettyTrueJson = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(trueJson);

            /*
            Файл trueJson полностью заполнен, но только в качестве объекта в программе. Выведем всю информацию из
            этого объекта в файл .json, чтобы можно было его посмотреть и прочитать. Используем для этого старый добрый
            FileWriter, в качестве объекта передаём наш "красивый" prettyTrueJson.
            */
            FileWriter fileTrueJson = new FileWriter("Files/trueJson.json"); //создаём файл
            fileTrueJson.write(prettyTrueJson); //записываем в файл наш "красивый" prettyTrueJson.
            /*
            Не забываем закрывать файлы, если они не были открыты в блоке try. Сборщик мусора рано или поздно это сделает
            за нас, но давайте не будем нагружать его лишней работой.
             */
            fileTrueJson.close();

            /*
            Итак, последняя задача - нужно создать excel (.csv) файл, который необходимо заполнить следующими данными:
            list_name, object_name, reward_key, money, details, reputation и isUsed.
            */
            FileWriter fileCSV = new FileWriter("Files/fileCSV.csv"); //создаём файл
            /*
            Для записи информации в созданный файл будем использовать CSVWriter. Немного настроим его,
            чтобы данные выводились без лишних кавычек, пробелов и т.д.
            */
            CSVWriter writerForFileCSV = new CSVWriter(fileCSV, ',', CSVWriter.NO_QUOTE_CHARACTER, CSVWriter.DEFAULT_ESCAPE_CHARACTER, CSVWriter.DEFAULT_LINE_END);

            /*
            Сделаем в качестве первой записи в наш файл - заголовок. Так сказать "шапочку", чтобы было понятно,
            какое значение к какому параметру относится.
             */
            String[] headerForFileCSV = { "list_name", "object_name", "reward_key", "money", "details", "reputation", "isUsed"}; //шапка
            writerForFileCSV.writeNext(headerForFileCSV); //запись шапки в качестве первой записи в файл

            /*
            И тут у нас небольшая проблема. Мы не можем адекватно использовать наш файл trueJson в виде объекта trueJson,
            потому что он содержит внутри себя HashMap, который не очень то желает становиться JSONObject-ом.
            Поэтому, придётся распарсить наш не так давно созданный файл trueJson.
            */
            Object parsingTrueJson = new JSONParser().parse(new FileReader("Files/trueJson.json")); //парсим файл trueJson.json
            JSONObject parsedTrueJson = (JSONObject) parsingTrueJson; //получаем из файла объект типа JSONObject

            /*
            Первым, необходимым для нас параметром, является пулы (list_name), поэтому запускаем цикл по всем нашим пулам.
            Используем, созданный ещё в начале, удобный массив arrayListsFromTask.
            */
            for (Object numberListFromTask: arrayListsFromTask) { //цикл по всем пулам из файла task.json
                /*
                Первый, необходимый нам параметр, мы можем получить сразу, поэтому запишем его в соответствующую переменную.
                В будущем, для записи всех полученных параметров, нам будет необходимо записать их виде массива строк,
                поэтому создаваемую переменную сразу сделаем String и делаем необходимые преобразования
                */
                String list_name = numberListFromTask.toString();

                //Получаем список задач из текущего пула, ранее мы уже так делали.
                JSONObject listFromTask = (JSONObject) listsFromTask.get(numberListFromTask);
                JSONArray objectsFromListFromTask = (JSONArray) listFromTask.get("list");

                //Запускаем цикл по всем задачам, полученным из пула
                for (Object objectFromListFromTask: objectsFromListFromTask) {
                    //Здесь можем получить второй необходимый параметр - object_name. Не забываем про преобразования в String
                    String  object_name = objectFromListFromTask.toString();

                    //Из ранее распарсенного trueJson мы можем вытащить остальные, необходимые нам параметры, используя текущую задачу
                    JSONObject infoObject = (JSONObject) parsedTrueJson.get(object_name);

                    //Получаем необходимые нам данные: reward_key, money, details и reputation. Не забываем про String и преобразования
                    String reward_key = infoObject.get("reward").toString();
                    String money = infoObject.get("money").toString();
                    String details = infoObject.get("details").toString();
                    String reputation = infoObject.get("reputation").toString();

                    /*
                    И последний нужный нам параметр - isUsed. Он должен быть равен 1, если наш текущий объект есть
                    в списке объектов из файла file.json, и 0 - в противном случае. Сделаем его также String, чтобы
                    потом не делать лишних преобразований.
                     */
                    String isUsed = "";

                    //если текущий объект содержится в списке объектов из файла file.json (используем раннее распарсенную его версию)
                    if (objectsFromFile.containsKey(object_name)) {
                        isUsed = "1";
                    }
                    else {
                        isUsed = "0";
                    }

                    /*
                    Осталось записать все полученные данные в наш файл fileCSV.csv, создадим для этого отдельный
                    массив строк dataForFileCSV и поместим в него все наши данные
                    */
                    String[] dataForFileCSV = {list_name, object_name, reward_key, money, details, reputation, isUsed};
                    /*
                    При помощи CSVWriter, а точнее его функции writeNext(), запишем в наш файл fileCSV.csv данные
                    с только что созданного массива строк dataForFileCSV
                    */
                    writerForFileCSV.writeNext(dataForFileCSV);
                    //Так как мы заполняем данные строго по циклу пулов, то наши данные в файле будут уже отсортированы по как раз-таки пулам (list_name)
                }
            }
            //Не забываем закрывать файлы и Writer'ы, нечего нагружать сборщик мусора)
            writerForFileCSV.close();
            fileCSV.close();

        } catch (IOException | ParseException e) {
            throw new RuntimeException(e);
        }

    }
}