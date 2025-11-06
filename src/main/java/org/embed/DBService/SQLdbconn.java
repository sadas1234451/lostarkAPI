package org.embed.DBService;

import java.sql.Connection;
import java.sql.DriverManager;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

public class SQLdbconn {
    @Controller
public class DbTestController {

    @GetMapping("/dbtest")
    public String dbTest(Model model) {
        boolean connected = false;

        try {
            String url = "jdbc:mysql://localhost:3306/embed";
            String user = "root";
            String password = "1111";

            Class.forName("com.mysql.cj.jdbc.Driver");
            Connection conn = DriverManager.getConnection(url, user, password);
            connected = true;
            conn.close();
        } catch (Exception e) {
            connected = false;
        }

        model.addAttribute("connected", connected);
        return "db_test";
    }
}
}
