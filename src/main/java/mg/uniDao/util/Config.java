package mg.uniDao.util;

import io.github.cdimascio.dotenv.Dotenv;

public class Config {
    public static final Dotenv DOTENV = Dotenv.configure().systemProperties().load();
    public static final String SEQUENCE_SUFFIX = DOTENV.get("SEQUENCE_SUFFIX", "_seq");
}
