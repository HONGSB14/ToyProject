package LDSS;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import java.text.SimpleDateFormat;
import java.util.Date;

@SpringBootApplication
public class Index {
    public static void main(String[] args) {
        SpringApplication.run(Index.class);
        Date date= new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hh:ss");
        String today=sdf.format(date);
        String springVer=org.springframework.core.SpringVersion.getVersion();

        System.out.println("Now Spring Version Check>>>>>>"+springVer);
        System.out.println("Server Start Time Check>>>>>>>>"+today);
        System.out.println("hello, Admin!!!");
        System.out.println("LOL DATA SEARCH SYSTEM ON");

    }
}
