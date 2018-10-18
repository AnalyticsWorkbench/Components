package eu.sisob.components.relationalextractor.converter;

import eu.sisob.api.visualization.format.graph.fields.EdgeSet;
import eu.sisob.api.visualization.format.graph.fields.NodeSet;

public abstract class Converter {
	
	public abstract void convertDataSet(String data);
	
	public abstract NodeSet requestNodeSet();
	
	public abstract EdgeSet requestEdgeSet();

}
