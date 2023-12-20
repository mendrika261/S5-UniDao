package mg.uniDao.core;

import io.github.cdimascio.dotenv.Dotenv;

public class Config {
    public static final Dotenv DOTENV = Dotenv.configure().systemProperties().load();
}
