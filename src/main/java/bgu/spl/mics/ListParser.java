package bgu.spl.mics;

import com.google.gson.Gson;
import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;

import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.List;

public class ListParser<T> {
    public static <T> List<T> parse(String filePath, Type typeOfT) throws IOException, JsonIOException, JsonSyntaxException {
        Gson gson = new Gson();
        try (FileReader reader = new FileReader(filePath)) {
            // Deserialize JSON to list of T
            return gson.fromJson(reader, typeOfT);
        }
    }
}
