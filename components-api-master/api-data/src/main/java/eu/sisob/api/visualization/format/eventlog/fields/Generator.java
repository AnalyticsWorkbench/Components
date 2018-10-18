
package eu.sisob.api.visualization.format.eventlog.fields;


/**
 *
 * @author walter
 * @author goehnert
 */
public class Generator extends AbstractLogObject {

    /**
	 * 
	 */
	private static final long serialVersionUID = -3742537029539538460L;

	public Generator() {
    	this.generateId();
    }
    
    public Generator(String id) {
        if (id == null) {
        	throw new IllegalArgumentException("id must not be null");
        }
        this.setId(id);
    }
     
    @Override
    public boolean equals(Object o) {
        boolean isEqual = false;
        if (o instanceof Generator) {
            isEqual = ((Generator)o).getId().equals(this.getId());
        }
        return isEqual;
    }
    
}
