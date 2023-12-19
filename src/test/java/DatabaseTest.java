import mg.uniDao.core.Service;
import mg.uniDao.exception.DatabaseException;
import mg.uniDao.provider.PostgresSql;
import org.junit.jupiter.api.Test;

public class DatabaseTest {
    @Test
    void testPostgresSqlConnect() throws DatabaseException {
        PostgresSql<?> postgresSql = new PostgresSql<>();
        Service<?> service = postgresSql.connect(
                "jdbc:postgresql://localhost:5432/postgres",
                "mendrika",
                ""
        );
        assert service.getAccess() != null: "PostgresSql connect() failed";
    }
}
