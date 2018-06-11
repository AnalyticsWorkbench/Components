package eu.sisob.components.framework;

import info.collide.sqlspaces.commons.TupleSpaceException;

public interface ComponentInterface {
	
	public String getManagerName();
	
	public void initialize() throws TupleSpaceException;

}
