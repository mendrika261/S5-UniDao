package mg.uniDao.util;

import mg.uniDao.exception.DaoException;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.io.IOException;
import java.util.Objects;

public class FileUtils {
    public static String getFileContentAsString(String filePath) throws DaoException {
        try {
            return new String(Files.readAllBytes(Paths.get(filePath)));
        } catch (IOException e) {
            try {
                return new String(Objects.requireNonNull(FileUtils.class.getClassLoader().getResourceAsStream(filePath))
                        .readAllBytes());
            } catch (IOException e2) {
                throw new DaoException(e.getMessage());
            }
        }
    }
}
