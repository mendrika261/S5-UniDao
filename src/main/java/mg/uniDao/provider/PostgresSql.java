package mg.uniDao.provider;

import mg.uniDao.core.GenericSqlDatabase;

import java.util.HashMap;

public class PostgresSql extends GenericSqlDatabase {

    @Override
    protected String createSQL(String collectionName, HashMap<String, Object> attributes) {
        StringBuilder columnsSQL = new StringBuilder();
        StringBuilder valuesSQL = new StringBuilder();
        if (attributes != null) {
            for (String attribute : attributes.keySet()) {
                columnsSQL.append(attribute).append(", ");
                valuesSQL.append("?, ");
            }
            columnsSQL.delete(columnsSQL.length() - 2, columnsSQL.length());
            valuesSQL.delete(valuesSQL.length() - 2, valuesSQL.length());
        }
        return "INSERT INTO \"" + collectionName + "\" (" + columnsSQL + ") VALUES (" + valuesSQL + ")";
    }

    @Override
    protected String findListWithLimitSQL(String collectionName, String extraCondition) {
        if(extraCondition == null || extraCondition.isEmpty()) extraCondition = "1 = 1";
        return "SELECT * FROM \"" + collectionName + "\" WHERE " + extraCondition + " LIMIT ? OFFSET ?";
    }

    @Override
    protected String findSQL(String collectionName, HashMap<String, Object> attributes, String extraCondition) {
        StringBuilder conditionSQL = new StringBuilder();
        if(attributes != null) {
            for (String attribute : attributes.keySet())
                conditionSQL.append(attribute).append(" = ").append("?").append(" AND ");
            conditionSQL.delete(conditionSQL.length() - 5, conditionSQL.length());
        } else conditionSQL.append("1 = 1");
        return "SELECT * FROM \"" + collectionName + "\" WHERE " + conditionSQL + " " + extraCondition;
    }
}
