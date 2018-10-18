package eu.sisob.api.visualization.format.metadata.fields;

import org.json.simple.JSONObject;

public class Property extends JSONObject {

    /**
     *
     */
    private static final long serialVersionUID = -766246454640503224L;

    public static final String KEY_PROPERTY = "property";
    public static final String KEY_PARSINGTYPE = "parsingtype";
    public static final String KEY_TITLE = "title";
    public static final String KEY_DESCRIPTION = "description";
    public static final String KEY_SPECIFICTYPE = "specifictype";

    public static String DEGREE_CENTRALITY = "dc";
    public static String BETWEENNESS_CENTRALITY = "bc";
    public static String CLOSENESS_CENTRALITY = "cc";

    public static String NORMALIZED_DEGREE_CENTRALITY = "ndc";
    public static String NORMALIZED_BETWEENNESS_CENTRALITY = "nbc";
    public static String NORMALIZED_CLOSENESS_CENTRALITY = "ncc";

    public static final String PRODUCTIVITY = "p";
    public static final String COLLABORATION = "c";

    public static final String MAX_PRODUCTIVITY = "maxP";
    public static final String MAX_COLLABORATION = "maxC";

    public static final String WORD_FREQUENCY = "wf";
    public static final String WORD_SIMILARITY = "sim";

    public static final String EDGE_BETWEENNESS_CENTRALITY = "ebc";

    @SuppressWarnings("unchecked")
    public Property() {
        super();
        super.put(KEY_PARSINGTYPE, "string");
    }

    @SuppressWarnings("unchecked")
    public Property(String property) {
        this();
        super.put(KEY_PROPERTY, property);
    }

    @SuppressWarnings("unchecked")
    public Property(String property, String parsingType) {
        this(property);
        if (parsingType != null) {
            super.put(KEY_PARSINGTYPE, parsingType);
        }
    }

    @SuppressWarnings("unchecked")
    public Property(String property, String parsingType, String title, String description, String specificType) {
        this(property, parsingType);
        if (title != null) {
            super.put(KEY_TITLE, title);
        }
        if (description != null) {
            super.put(KEY_DESCRIPTION, description);
        }
        if (specificType != null) {
            super.put(KEY_SPECIFICTYPE, specificType);
        }
    }

    public Property(JSONObject prop) {
        this((String) prop.get(KEY_PROPERTY), (String) prop.get(KEY_PARSINGTYPE), (String) prop.get(KEY_TITLE),
                (String) prop.get(KEY_DESCRIPTION), (String) prop.get(KEY_SPECIFICTYPE));
    }

    @SuppressWarnings("unchecked")
    public void setParsingType(String parsingType) {
        super.put(KEY_PARSINGTYPE, parsingType);
    }

    /**
     *
     * @return the value or null, if not set
     */
    public String getParsingType() {
        return (String) super.get(KEY_PARSINGTYPE);
    }

    @SuppressWarnings("unchecked")
    public void setTitle(String title) {
        super.put(KEY_TITLE, title);
    }

    /**
     *
     * @return the value or null, if not set
     */
    public String getTitle() {
        return (String) super.get(KEY_TITLE);
    }

    @SuppressWarnings("unchecked")
    public void setDescription(String description) {
        super.put(KEY_DESCRIPTION, description);
    }

    /**
     *
     * @return the value or null, if not set
     */
    public String getDescription() {
        return (String) super.get(KEY_DESCRIPTION);
    }

    @SuppressWarnings("unchecked")
    public void setSpecificType(String specificType) {
        super.put(KEY_SPECIFICTYPE, specificType);
    }

    /**
     *
     * @return the value or null, if not set
     */
    public String getSpecificType() {
        return (String) super.get(KEY_SPECIFICTYPE);
    }

    @SuppressWarnings("unchecked")
    public void setPropertyKey(String property) {
        super.put(KEY_PROPERTY, property);
    }

    public String getPropertyKey() {
        return (String) super.get(KEY_PROPERTY);
    }

    @Override
    public boolean equals(Object obj) {
        
        boolean isEqual = false;
        
        if (obj instanceof Property) {
            
            isEqual = ((Property)obj).getPropertyKey().equals(this.getPropertyKey());
        }
        return isEqual;
    }

    @Override
    public int hashCode() {
        
        return this.getPropertyKey().hashCode();
    }
}
