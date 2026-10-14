package vn.ptit.hotel.config;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Logger;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;

@WebListener
public class DbInitializer implements ServletContextListener {
    private static final Logger LOG = Logger.getLogger(DbInitializer.class.getName());

    @Override
    public void contextInitialized(ServletContextEvent event) {
        try (Connection c = Database.connection()) {
            if (tableExists(c, "rooms")) {
                return;
            }
            c.setAutoCommit(false);
            run(c, "/db/schema.sql");
            run(c, "/db/seed.sql");
            c.commit();
            LOG.info("Đã khởi tạo CSDL và dữ liệu mẫu");
        } catch (SQLException | IOException e) {
            throw new IllegalStateException("Không khởi tạo được CSDL", e);
        }
    }

    @Override
    public void contextDestroyed(ServletContextEvent event) {
        Database.close();
    }

    private static boolean tableExists(Connection c, String table) throws SQLException {
        try (ResultSet rs = c.getMetaData().getTables(null, null, table, new String[]{"TABLE"})) {
            return rs.next();
        }
    }

    private static void run(Connection c, String resource) throws IOException, SQLException {
        try (InputStream in = DbInitializer.class.getResourceAsStream(resource); Statement st = c.createStatement()) {
            st.execute(new String(in.readAllBytes(), StandardCharsets.UTF_8));
        }
    }
}
