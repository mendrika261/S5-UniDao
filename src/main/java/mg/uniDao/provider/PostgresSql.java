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

    private String toConditionSQL(HashMap<String, Object> conditions) {
        StringBuilder conditionSQL = new StringBuilder();
        if(conditions != null) {
            for (String condition : conditions.keySet())
                conditionSQL.append(condition).append(" = ").append("?").append(" AND ");
            conditionSQL.delete(conditionSQL.length() - 5, conditionSQL.length());
        } else conditionSQL.append("1 = 1");
        return conditionSQL.toString();
    }

    @Override
    protected String findSQL(String collectionName, HashMap<String, Object> conditions, String extraCondition) {
        return "SELECT * FROM \"" + collectionName + "\" WHERE " + toConditionSQL(conditions) + " " + extraCondition + " LIMIT 1";
    }

    @Override
    protected String updateSQL(String collectionName, HashMap<String, Object> attributes, HashMap<String, Object> conditions, String extraCondition) {
        StringBuilder setSQL = new StringBuilder();
        if(attributes != null) {
            for (String attribute : attributes.keySet())
                setSQL.append(attribute).append(" = ").append("?").append(", ");
            setSQL.delete(setSQL.length() - 2, setSQL.length());
        }
        return "UPDATE \"" + collectionName + "\" SET " + setSQL + " WHERE " + toConditionSQL(conditions) + " " + extraCondition;
    }
}
