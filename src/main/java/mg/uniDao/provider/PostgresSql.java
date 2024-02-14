package mg.uniDao.provider;

import mg.uniDao.core.GenericSqlDatabase;
import mg.uniDao.exception.DatabaseException;

import java.util.HashMap;
import java.util.List;

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

    @Override
    protected String deleteSQL(String collectionName, HashMap<String, Object> conditions, String extraCondition) {
        return "DELETE FROM \"" + collectionName + "\" WHERE " + toConditionSQL(conditions) + " " + extraCondition;
    }

    @Override
    protected String getNextSequenceValueSql(String sequenceName) {
        return "SELECT nextval('" + sequenceName + "') AS result";
    }

    public String getMappingType(String type) throws DatabaseException {
        return switch (type) {
            case "java.lang.Integer", "int" -> "INT";
            case "java.lang.Double", "double" -> "DOUBLE PRECISION";
            case "java.time.LocalDate" -> "DATE";
            case "java.time.LocalDateTime" -> "TIMESTAMP";
            case "java.lang.String" -> "TEXT";
            case "java.lang.Boolean" -> "BOOLEAN";
            case "java.math.BigInteger", "java.lang.Long" -> "BIGINT";
            case "java.math.BigDecimal" -> "DECIMAL";
            case "java.util.UUID" -> "UUID";
            case "java.lang.Object" -> "JSONB";
            case "java.time.LocalTime" -> "TIME";
            case "java.lang.Float" -> "FLOAT";
            case "java.lang.Short", "java.lang.Byte" -> "SMALLINT";
            case "java.lang.Character" -> "CHAR";
            default -> throw new DatabaseException("Type not supported: " + type);
        };
    }


    @Override
    protected String createCollectionSQL(String collectionName, HashMap<String, String> attributes) throws DatabaseException {
        final StringBuilder columnsSQL = new StringBuilder();
        if (attributes != null) {
            for (String attribute : attributes.keySet()) {
                columnsSQL.append(attribute)
                        .append(" ")
                        .append(getMappingType(attributes.get(attribute)))
                        .append(", ");
            }
            columnsSQL.delete(columnsSQL.length() - 2, columnsSQL.length());
        }
        return "CREATE TABLE IF NOT EXISTS \"" + collectionName + "\" (" + columnsSQL + ")";
    }

    @Override
    protected String addPrimaryKeySQL(String collectionName, List<String> primaryKeyColumns) {
        return "ALTER TABLE \"" + collectionName + "\" ADD PRIMARY KEY (" + String.join(", ", primaryKeyColumns) + ")";
    }
}
