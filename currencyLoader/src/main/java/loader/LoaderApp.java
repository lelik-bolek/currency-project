package loader;

import java.io.IOException;

//import common.*;
import common.utils.FileUtils;

public class LoaderApp {
    public static void main(String[] args) {

        System.out.println("Загрузка курса валют началась...");

        System.out.println("Запуск метода updateTablesFCML()...");
        LoaderService.updateTablesFCML();

        System.out.println("Запуск метода updateTablesFCM()...");
        LoaderService.updateTablesFCM();

        System.out.println("Запуск метода removeFiles()...");
        try{
            FileUtils.removeFiles();
        } catch(IOException e) {
            System.err.println("Ошибка в removeFiles(): " + e.getMessage());
        }
        

        System.out.println("Загрузка курса валют успешно завершена!");
    }
}
