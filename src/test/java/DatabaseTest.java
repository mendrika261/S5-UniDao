import mg.uniDao.core.Config;
import mg.uniDao.core.Database;
import mg.uniDao.core.Service;
import mg.uniDao.core.Utils;
import mg.uniDao.exception.DatabaseException;
import mg.uniDao.provider.PostgresSql;
import org.junit.jupiter.api.Test;

public class DatabaseTest {
    @Test
    void testPostgresSqlConnect() throws DatabaseException {
        Database<?> postgresSql = new PostgresSql<>();
        Service<?> service = postgresSql.connect(
                Config.DOTENV.get("DB_URL"),
                Config.DOTENV.get("DB_USERNAME"),
                Config.DOTENV.get("DB_PASSWORD")
        );
        assert service.getAccess() != null: "PostgresSql connect() failed";
    }
}
