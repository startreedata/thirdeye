package org.apache.pinot.thirdeye.spi.datalayer.pojo;

public class LogicalView {

    private String name;
    private String query;


    public String getName() {
        return name;
    }

    public LogicalView setName(String name) {
        this.name = name;
        return this;
    }

    public String getQuery() {
        return query;
    }

    public LogicalView setQuery(String query) {
        this.query = query;
        return this;
    }
}
