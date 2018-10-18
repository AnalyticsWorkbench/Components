
package eu.sisob.api.visualization.format.eventlog.fields;


/**
 * 
 * @author hecking
 * @author goehnert
 * 
 */
public class ObjectLog extends AbstractLogObject {

	/**
	 * 
	 */
	private static final long serialVersionUID = -3921553812971646162L;

	public ObjectLog() {
		this.generateId();
	}

	public ObjectLog(String id) {
		if (id == null) {
        	throw new IllegalArgumentException("id must not be null");
        }
        this.setId(id);
	}

	@Override
	public boolean equals(java.lang.Object o) {
		boolean isEqual = false;
        if (o instanceof ObjectLog) {
            isEqual = ((ObjectLog)o).getId().equals(this.getId());
        }
        return isEqual;
	}

}
