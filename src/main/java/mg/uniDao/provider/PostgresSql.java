package mg.uniDao.provider;

import mg.uniDao.core.sql.GenericSqlDatabase;
import mg.uniDao.core.sql.Joiner;
import mg.uniDao.exception.DaoException;
import mg.uniDao.util.ObjectUtils;

import java.lang.reflect.Field;
import java.math.BigInteger;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;

public class PostgresSql extends GenericSqlDatabase {

    @Override
    protected String createSQL(String collectionName, HashMap<Field, Object> attributes) {
        StringBuilder columnsSQL = new StringBuilder();
        StringBuilder valuesSQL = new StringBuilder();
        if (attributes != null) {
            for (Field attribute : attributes.keySet()) {
                columnsSQL.append("\"").append(ObjectUtils.getAnnotatedFieldName(attribute)).append("\", ");
                valuesSQL.append("?, ");
            }
            columnsSQL.delete(columnsSQL.length() - 2, columnsSQL.length());
            valuesSQL.delete(valuesSQL.length() - 2, valuesSQL.length());
        }
        return "INSERT INTO \"" + collectionName + "\" (" + columnsSQL + ") VALUES (" + valuesSQL + ")";
    }

    private String joinSQL(List<Joiner> joiners) {
        StringBuilder joinSQL = new StringBuilder();
        if(joiners != null) {
            for (Joiner joiner : joiners)
                joinSQL.append(" JOIN \"").append(joiner.getOutsideJoinCollection()).append("\" ON ")
                        .append("\"").append(joiner.getInsideJoinField()).append("\"")
                        .append(joiner.getOperator())
                        .append("\"").append(joiner.getOutsideJoinCollection()).append("\".\"")
                        .append(joiner.getOutsideJoinFieldOrCondition()).append("\"");
        }
        return joinSQL.toString();
    }

    private String joinColumnSql(List<Joiner> joiners) {
        StringBuilder joinSQL = new StringBuilder();
        if(joiners != null) {
            for (Joiner joiner : joiners) {
                List<String> columns = joiner.getColumns();
                for (String column : columns) {
                    joinSQL.append(", \"").append(joiner.getOutsideJoinCollection()).append("\".\"").append(column)
                            .append("\" AS \"").append(joiner.getOutsideJoinCollection()).append(".").append(column)
                            .append("\"");
                }
            }
        }
        return joinSQL.toString();
    }

    @Override
    protected String findListWithLimitSQL(String collectionName, String extraCondition, List<Joiner> joiners) {
        if(extraCondition == null || extraCondition.isEmpty()) extraCondition = "true";
        return "SELECT * " + joinColumnSql(joiners) + " FROM \"" + collectionName + "\"" + joinSQL(joiners) +
                " WHERE " + extraCondition + " LIMIT ? OFFSET ?";
    }

    private String toConditionSQL(HashMap<Field, Object> conditions) {
        StringBuilder conditionSQL = new StringBuilder();
        if(conditions != null) {
            for (Field condition : conditions.keySet())
                conditionSQL.append("\"").append(ObjectUtils.getAnnotatedFieldName(condition)).append("\" = ")
                        .append("?").append(" AND ");
            conditionSQL.delete(conditionSQL.length() - 5, conditionSQL.length());
        } else conditionSQL.append("true");
        return conditionSQL.toString();
    }

    @Override
    protected String findSQL(String collectionName, HashMap<Field, Object> conditions,
                             String extraCondition, List<Joiner> joiners) {
        return "SELECT * " + joinColumnSql(joiners) + " FROM \"" + collectionName + "\"" + joinSQL(joiners) +
                " WHERE " + toConditionSQL(conditions) + " " + extraCondition + " LIMIT 1";
    }

    @Override
    protected String updateSQL(String collectionName, HashMap<Field, Object> attributes,
                               HashMap<Field, Object> conditions, String extraCondition) {
        StringBuilder setSQL = new StringBuilder();
        if(attributes != null) {
            for (Field attribute : attributes.keySet())
                setSQL.append("\"").append(ObjectUtils.getAnnotatedFieldName(attribute))
                        .append("\" = ").append("?").append(", ");
            setSQL.delete(setSQL.length() - 2, setSQL.length());
        }
        return "UPDATE \"" + collectionName + "\" SET " + setSQL + " WHERE " + toConditionSQL(conditions) + " "
                + extraCondition;
    }

    @Override
    protected String deleteSQL(String collectionName, HashMap<Field, Object> conditions, String extraCondition) {
        return "DELETE FROM \"" + collectionName + "\" WHERE " + toConditionSQL(conditions) + " " + extraCondition;
    }

    @Override
    protected String getNextSequenceValueSql(String sequenceName) {
        return "SELECT nextval('" + sequenceName + "') AS result";
    }

    @Override
    protected String getMappingType(Field field) throws DaoException {
        final HashMap<Class<?>, String> mappings = new HashMap<>();
        mappings.put(String.class, "TEXT");
        mappings.put(Integer.class, "INT");
        mappings.put(int.class, "INT");
        mappings.put(Double.class, "DOUBLE PRECISION");
        mappings.put(double.class, "DOUBLE PRECISION");
        mappings.put(LocalDate.class, "DATE");
        mappings.put(LocalDateTime.class, "TIMESTAMP");
        mappings.put(Boolean.class, "BOOLEAN");
        mappings.put(BigInteger.class, "BIGINT");
        mappings.put(float.class, "FLOAT");
        mappings.put(Float.class, "FLOAT");
        // mappings.put(BigDecimal.class, "DECIMAL");
        // mappings.put(LocalTime.class, "TIME");
        try {
            return super.getMappingType(field);
        } catch (DaoException e) {
            return mappings.getOrDefault(field.getType(), "TEXT");
        }
    }


    @Override
    protected String createCollectionSQL(String collectionName) {
        return "CREATE TABLE IF NOT EXISTS \"" + collectionName + "\" ()";
    }

    @Override
    protected String addColumnSQL(String collectionName, String columnName, String columnType) {
        return "ALTER TABLE \"" + collectionName + "\" ADD COLUMN IF NOT EXISTS \"" + columnName + "\" "
                + columnType;
    }

    @Override
    protected String addForeignKeySQL(String collectionName, String columnName, String referenceCollection,
                                      String referenceColumn) {
        return "ALTER TABLE \"" + collectionName + "\" ADD CONSTRAINT \"" + collectionName + "_" + columnName + "_fkey\" "
                + "FOREIGN KEY (\"" + columnName + "\") REFERENCES \"" + referenceCollection + "\" (\"" + referenceColumn + "\")";
    }

    @Override
    protected String dropForeignKeySQL(String collectionName, String columnName) {
        return "ALTER TABLE \"" + collectionName + "\" DROP CONSTRAINT IF EXISTS \"" + collectionName + "_"
                + columnName + "_fkey\" CASCADE";
    }

    @Override
    protected String dropCollectionSQL(String collectionName) {
        return "DROP TABLE IF EXISTS \"" + collectionName + "\" CASCADE";
    }

    @Override
    protected String addPrimaryKeySQL(String collectionName, List<String> primaryKeyColumns) {
        return "ALTER TABLE \"" + collectionName + "\" ADD PRIMARY KEY (" + String.join(", ",
                primaryKeyColumns)+ ")";
    }

    @Override
    protected  String dropPrimaryKeySQL(String collectionName) {
        return "ALTER TABLE \"" + collectionName + "\" DROP CONSTRAINT IF EXISTS " + collectionName + "_pkey CASCADE";
    }

    @Override
    protected String alterColumnTypeSQL(String collectionName, String columnName, String columnType) {
        return "ALTER TABLE \"" + collectionName + "\" ALTER COLUMN \"" + columnName + "\" TYPE "
                + columnType + " USING \"" + columnName + "\"::" + columnType;
    }

    @Override
    protected String setColumnNullableSQL(String collectionName, String columnName, boolean nullable) {
        return "ALTER TABLE \"" + collectionName + "\" ALTER COLUMN \"" + columnName + "\" "
                + (nullable ? "DROP" : "SET") + " NOT NULL";
    }

    @Override
    protected String createSequenceSQL(String sequenceName) {
        return "CREATE SEQUENCE IF NOT EXISTS \"" + sequenceName + "\" START 1 INCREMENT 1";
    }

    @Override
    protected String addColumnUniqueSQL(String collectionName, String columnName) {
        return "ALTER TABLE \"" + collectionName + "\" ADD UNIQUE (" + columnName + ")";
    }

    @Override
    protected String dropColumnUniqueSQL(String collectionName, String columnName) {
        return "ALTER TABLE \"" + collectionName + "\" DROP CONSTRAINT IF EXISTS " + collectionName + "_"
                + columnName + "_key CASCADE";
    }

    @Override
    protected String addUniqueSQL(String collectionName, String[] columnName) {
        return "ALTER TABLE \"" + collectionName + "\" ADD CONSTRAINT " + collectionName +
                "_unique UNIQUE (" + String.join(", ", columnName) + ")";
    }

    @Override
    protected String dropUniqueSQL(String collectionName) {
        return "ALTER TABLE \"" + collectionName + "\" DROP CONSTRAINT IF EXISTS " + collectionName + "_unique CASCADE";
    }
}
