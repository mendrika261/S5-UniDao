package mg.uniDao.provider;

import mg.uniDao.core.GenericSqlDatabase;

import java.util.HashMap;

public class PostgresSql<T> extends GenericSqlDatabase<T> {

    @Override
    protected String createObjectSQL(String collectionName, HashMap<String, Object> attributes) {
        StringBuilder sql = new StringBuilder("INSERT INTO ")
                .append(collectionName)
                .append(" (");
        for(String attribute: attributes.keySet()) sql.append(attribute).append(", ");
        sql.delete(sql.length()-2, sql.length());
        sql.append(") VALUES (");
        sql.append("?, ".repeat(attributes.size()));
        sql.delete(sql.length()-2, sql.length());
        sql.append(")");
        return sql.toString();
    }
}
