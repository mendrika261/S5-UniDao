package mg.uniDao.util;

import io.github.cdimascio.dotenv.Dotenv;

import java.net.URL;
import java.util.Objects;

public class Config {
    public static final Dotenv DOTENV = Dotenv.configure().systemProperties().load();
    public static final String SEQUENCE_SUFFIX = DOTENV.get("SEQUENCE_SUFFIX", "_seq");
    public static final String POSTGRES = "postgresql.json";
}
