
package eu.sisob.api.visualization.format.eventlog.fields;


/**
 * 
 * @author hecking
 * @author goehnert
 */
public class Actor extends AbstractLogObject {

	/**
	 * 
	 */
	private static final long serialVersionUID = -314323521906627016L;

	public Actor() {
		this.generateId();
	}

	public Actor(String id) {
		if (id == null) {
        	throw new IllegalArgumentException("id must not be null");
        }
        this.setId(id);
	}

	@Override
	public boolean equals(Object o) {
		boolean isEqual = false;
		if (o instanceof Actor) {
			isEqual = ((Actor) o).getId().equals(this.getId());
		}
		return isEqual;
	}

}
