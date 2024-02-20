package mg.uniDao.core.sql;

import java.util.List;

public class Joiner {
    private String insideJoinField;
    private String outsideJoinCollection;
    private String outsideJoinFieldOrCondition;
    private String operator;
    private List<String> columns;

    public Joiner(String insideJoinField, String outsideJoinCollection, String outsideJoinFieldOrCondition,
                  List<String> columns, String operator) {
        setInsideJoinField(insideJoinField);
        setOutsideJoinCollection(outsideJoinCollection);
        setOutsideJoinFieldOrCondition(outsideJoinFieldOrCondition);
        setColumns(columns);
        setOperator(operator);
    }

    public Joiner(String insideJoinField, String outsideJoinCollection, String outsideJoinFieldOrCondition,
                  List<String> columns) {
        this(insideJoinField, outsideJoinCollection, outsideJoinFieldOrCondition, columns, "=");
    }

    public String getInsideJoinField() {
        return insideJoinField;
    }

    public void setInsideJoinField(String insideJoinField) {
        this.insideJoinField = insideJoinField;
    }

    public String getOutsideJoinCollection() {
        return outsideJoinCollection;
    }

    public void setOutsideJoinCollection(String outsideJoinCollection) {
        this.outsideJoinCollection = outsideJoinCollection;
    }

    public String getOutsideJoinFieldOrCondition() {
        return outsideJoinFieldOrCondition;
    }

    public void setOutsideJoinFieldOrCondition(String outsideJoinFieldOrCondition) {
        this.outsideJoinFieldOrCondition = outsideJoinFieldOrCondition;
    }

    public String getOperator() {
        return operator;
    }

    public void setOperator(String operator) {
        this.operator = operator;
    }

    public List<String> getColumns() {
        return columns;
    }

    public void setColumns(List<String> columns) {
        this.columns = columns;
    }

    @Override
    public String toString() {
        return "Joiner{" +
                "insideJoinField='" + insideJoinField + '\'' +
                ", outsideJoinCollection='" + outsideJoinCollection + '\'' +
                ", outsideJoinFieldOrCondition='" + outsideJoinFieldOrCondition + '\'' +
                ", operator='" + operator + '\'' +
                '}';
    }
}
