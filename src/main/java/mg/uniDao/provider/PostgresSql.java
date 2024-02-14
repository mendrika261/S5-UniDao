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
        return "SELECT * FROM \"" + collectionName + "\" WHERE " + toConditionSQL(conditions) + " " + extraCondition
                + " LIMIT 1";
    }

    @Override
    protected String updateSQL(String collectionName, HashMap<String, Object> attributes, HashMap<String,
            Object> conditions, String extraCondition) {
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
    protected String createCollectionSQL(String collectionName) {
        return "CREATE TABLE IF NOT EXISTS \"" + collectionName + "\" ()";
    }

    @Override
    protected String addColumnSQL(String collectionName, String columnName, String columnType) throws DatabaseException {
        return "ALTER TABLE \"" + collectionName + "\" ADD COLUMN IF NOT EXISTS " + columnName + " "
                + getMappingType(columnType);
    }

    @Override
    protected String dropCollectionSQL(String collectionName) {
        return "DROP TABLE IF EXISTS \"" + collectionName + "\"";
    }

    @Override
    protected String addPrimaryKeySQL(String collectionName, List<String> primaryKeyColumns) {
        return "ALTER TABLE \"" + collectionName + "\" ADD PRIMARY KEY (" + String.join(", ", primaryKeyColumns)+ ")";
    }

    @Override
    protected  String dropPrimaryKeySQL(String collectionName) {
        return "ALTER TABLE \"" + collectionName + "\" DROP CONSTRAINT IF EXISTS " + collectionName + "_pkey";
    }

    @Override
    protected String alterColumnTypeSQL(String collectionName, String columnName, String columnType) throws DatabaseException {
        return "ALTER TABLE \"" + collectionName + "\" ALTER COLUMN " + columnName + " TYPE " + getMappingType(columnType)
                + " USING " + columnName + "::" + getMappingType(columnType);
    }

    @Override
    protected String setColumnNullableSQL(String collectionName, String columnName, boolean nullable) throws DatabaseException {
        return "ALTER TABLE \"" + collectionName + "\" ALTER COLUMN " + columnName + " " + (nullable ? "DROP" : "SET") + " NOT NULL";
    }

    @Override
    protected String createSequenceSQL(String sequenceName) {
        return "CREATE SEQUENCE IF NOT EXISTS " + sequenceName + " START 1 INCREMENT 1";
    }

    @Override
    protected String setColumnUniqueSQL(String collectionName, String columnName, boolean unique) {
        return "ALTER TABLE \"" + collectionName + "\" ADD CONSTRAINT " + collectionName + "_" + columnName + "_unique "
                + (unique ? "UNIQUE" : "DROP UNIQUE") + " (" + columnName + ")";
    }
}
