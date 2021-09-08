import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

/**
 * @author tobing
 * @date 2021/9/8 11:32
 * @description
 */
public class Main {
    public static void main(String[] args) throws Exception {
        Class.forName("com.mysql.cj.jdbc.Driver");
        String url = "jdbc:mysql://localhost:3306/shopping?user=root&password=root";
        Connection con = DriverManager.getConnection(url);
        Statement stmt = con.createStatement();
        String query = "select * from test";
        ResultSet rs = stmt.executeQuery(query);
        while (rs.next()) {
            rs.getString(1);
            rs.getInt(2);
        }
    }
}
