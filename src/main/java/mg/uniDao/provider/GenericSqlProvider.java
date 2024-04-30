package mg.uniDao.provider;

import mg.uniDao.core.sql.GenericSqlDatabase;
import mg.uniDao.core.sql.Joiner;
import mg.uniDao.exception.DaoException;
import mg.uniDao.util.FileUtils;
import mg.uniDao.util.Format;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.stream.Collectors;

public class GenericSqlProvider extends GenericSqlDatabase {
    private String DEFAULT_DRIVER;
    private String DEFAULT_INSERT;
    private String INSERT_SQL;
    private String SELECT_SQL_ONE;
    private String SELECT_SQL;
    private String UPDATE_SQL;
    private String DELETE_SQL;
    private String NEXT_SEQ_SQL;
    private String CREATE_TABLE_SQL;
    private String ADD_COLUMN_SQL;
    private String ADD_FK_SQL;
    private String DROP_FK_SQL;
    private String DROP_TABLE_SQL;
    private String ADD_PK_SQL;
    private String DROP_PK_SQL;
    private String ALTER_COLUMN_TYPE_SQL;
    private String SET_COLUMN_NULLABLE_SQL;
    private String CREATE_SEQ_SQL;
    private String ADD_COLUMN_UNIQUE_SQL;
    private String DROP_COLUMN_UNIQUE_SQL;
    private String ADD_UNIQUE_SQL;
    private String DROP_UNIQUE_SQL;
    private LinkedHashMap<String, String> MAPPING;

    public static GenericSqlProvider get(String configPath) throws DaoException {
        return Format.fromJson(FileUtils.getFileContentAsString(configPath),
                GenericSqlProvider.class);
    }

    public static GenericSqlProvider get() throws DaoException {
        return get("database.json"); // default value
    }

    @Override
    public String getDriver(String configName) {
        String driver = super.getDriver(configName);
        return (driver == null || driver.isEmpty()) ? DEFAULT_DRIVER : driver;
    }

    @Override
    public String createSQL(String collectionName, LinkedHashMap<String, Object> attributes) {
        if (attributes == null || attributes.isEmpty()) {
            return String.format(DEFAULT_INSERT, collectionName);
        }

        String columnsSQL = String.join("\", \"", attributes.keySet());
        String valuesSQL = String.join(", ", Collections.nCopies(attributes.size(), "?"));

        return String.format(INSERT_SQL, collectionName, columnsSQL, valuesSQL);
    }

    private String joinSQL(List<Joiner> joiners) {
        if (joiners == null) {
            return "";
        }

        return joiners.stream()
                .map(joiner -> String.format(" JOIN \"%s\" ON \"%s\"%s\"%s\".\"%s\"",
                        joiner.getOutsideJoinCollection(),
                        joiner.getInsideJoinField(),
                        joiner.getOperator(),
                        joiner.getOutsideJoinCollection(),
                        joiner.getOutsideJoinFieldOrCondition()))
                .collect(Collectors.joining());
    }

    private String joinColumnSql(List<Joiner> joiners) {
        if (joiners == null) {
            return "";
        }

        return joiners.stream()
                .flatMap(joiner -> joiner.getColumns().stream()
                        .map(column -> String.format(", \"%s\".\"%s\" AS \"%s.%s\"",
                                joiner.getOutsideJoinCollection(), column, joiner.getOutsideJoinCollection(), column)))
                .collect(Collectors.joining());
    }

    public String findListWithLimitSQL(String collectionName, String extraCondition, List<Joiner> joiners) {
        String condition = (extraCondition == null || extraCondition.isEmpty()) ? "true" : extraCondition;
        String joinColumns = joinColumnSql(joiners);
        String joins = joinSQL(joiners);
        return String.format(SELECT_SQL, joinColumns, collectionName, joins, condition);
    }

    private String toConditionSQL(LinkedHashMap<String, Object> conditions) {
        if (conditions == null || conditions.isEmpty()) {
            return "true";
        }

        return conditions.keySet().stream()
                .map(column -> String.format("\"%s\" = ?", column))
                .collect(Collectors.joining(" AND "));
    }

    public String findSQL(String collectionName, LinkedHashMap<String, Object> conditions,
                          String extraCondition, List<Joiner> joiners) {
        String condition = toConditionSQL(conditions);
        String joinColumns = joinColumnSql(joiners);
        String joins = joinSQL(joiners);
        return String.format(SELECT_SQL_ONE, joinColumns, collectionName, joins, condition);
    }

    public String updateSQL(String collectionName, LinkedHashMap<String, Object> attributes,
                            LinkedHashMap<String, Object> conditions, String extraCondition) {
        String setSQL = attributes.keySet().stream()
                .map(column -> String.format("\"%s\" = ?", column))
                .collect(Collectors.joining(", "));
        String condition = toConditionSQL(conditions);
        return String.format(UPDATE_SQL, collectionName, setSQL, condition, extraCondition);
    }

    @Override
    public String deleteSQL(String collectionName, LinkedHashMap<String, Object> conditions, String extraCondition) {
        String condition = toConditionSQL(conditions);
        return String.format(DELETE_SQL, collectionName, condition, extraCondition);
    }

    @Override
    public String getNextSequenceValueSql(String sequenceName) {
        return String.format(NEXT_SEQ_SQL, sequenceName);
    }

    @Override
    public String getMappingType(Class<?> className) {
        return MAPPING.getOrDefault(className.getSimpleName(), MAPPING.get("Object"));
    }

    @Override
    public String createCollectionSQL(String collectionName) {
        return String.format(CREATE_TABLE_SQL, collectionName);
    }

    @Override
    public String addColumnSQL(String collectionName, String columnName, String columnType) {
        return String.format(ADD_COLUMN_SQL, collectionName, columnName, columnType);
    }

    @Override
    public String addForeignKeySQL(String collectionName, String columnName, String referenceCollection,
                                  String referenceColumn) {
        return String.format(ADD_FK_SQL, collectionName, collectionName, columnName, columnName, referenceCollection, referenceColumn);
    }

    @Override
    public String dropForeignKeySQL(String collectionName, String columnName) {
        return String.format(DROP_FK_SQL, collectionName, collectionName, columnName);
    }

    @Override
    public String dropCollectionSQL(String collectionName) {
        return String.format(DROP_TABLE_SQL, collectionName);
    }

    @Override
    public String addPrimaryKeySQL(String collectionName, List<String> primaryKeyColumns) {
        return String.format(ADD_PK_SQL, collectionName, String.join("\", \"", primaryKeyColumns));
    }

    @Override
    public String dropPrimaryKeySQL(String collectionName) {
        return String.format(DROP_PK_SQL, collectionName, collectionName);
    }

    @Override
    public String alterColumnTypeSQL(String collectionName, String columnName, String columnType) {
        return String.format(ALTER_COLUMN_TYPE_SQL, collectionName, columnName, columnType, columnName, columnType);
    }

    @Override
    public String setColumnNullableSQL(String collectionName, String columnName, boolean nullable) {
        return String.format(SET_COLUMN_NULLABLE_SQL, collectionName, columnName, nullable ? "DROP" : "SET");
    }

    @Override
    public String createSequenceSQL(String sequenceName) {
        return String.format(CREATE_SEQ_SQL, sequenceName);
    }

    @Override
    public String addColumnUniqueSQL(String collectionName, String columnName) {
        return String.format(ADD_COLUMN_UNIQUE_SQL, collectionName, columnName);
    }

    @Override
    public String dropColumnUniqueSQL(String collectionName, String columnName) {
        return String.format(DROP_COLUMN_UNIQUE_SQL, collectionName, collectionName, columnName);
    }

    @Override
    public String addUniqueSQL(String collectionName, String[] columnName) {
        return String.format(ADD_UNIQUE_SQL, collectionName, collectionName, String.join("\", \"", columnName));
    }

    @Override
    public String dropUniqueSQL(String collectionName) {
        return String.format(DROP_UNIQUE_SQL, collectionName, collectionName);
    }
}
