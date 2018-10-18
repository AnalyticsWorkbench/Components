
package eu.sisob.api.visualization.format.eventlog.fields;


/**
 *
 * @author hecking
 * @author goehnert
 */
public class Target extends AbstractLogObject {
    
	/**
	 * 
	 */
	private static final long serialVersionUID = -6341874658373013645L;

	public Target() {
		this.generateId();
	}

	public Target(String id) {
		if (id == null) {
        	throw new IllegalArgumentException("id must not be null");
        }
        this.setId(id);
	}
    
    @Override
    public boolean equals(Object o) {
        boolean isEqual = false;
        if (o instanceof Target) {
            isEqual = ((Target)o).getId().equals(this.getId());
        }
        return isEqual;
    }

}