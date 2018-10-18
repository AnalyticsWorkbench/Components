package eu.sisob.components.framework.json.util;

public class IllegalContentTypeException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = -7356273822476451096L;

	public IllegalContentTypeException(String neededType, String currentType) {
		super("Wrong content type, " + currentType + " is available, " + neededType + " is needed.");
	}
	
	public IllegalContentTypeException(String message) {
		super(message);
	}
	
}
