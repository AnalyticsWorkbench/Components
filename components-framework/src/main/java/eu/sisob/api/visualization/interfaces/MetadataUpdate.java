package eu.sisob.api.visualization.technique.interfaces;

import eu.sisob.api.visualization.format.metadata.Metadata;

public interface MetadataUpdate {
	
	public abstract Metadata updatedMetadata() throws Exception;

}
