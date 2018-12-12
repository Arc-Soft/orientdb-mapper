package mapper.orient;

import java.util.Map;

public class Query {

    private String query;

    private Map<String, Object> params;

    public Query(String query) {
        this.query = query;
    }

    public Query(String query, Map<String, Object> params) {
        this.query = query;
        this.params = params;
    }

    public String getQuery() {
        return query;
    }

    public Map<String, Object> getParams() {
        return params;
    }
}
