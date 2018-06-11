package info.collide.components.spmanalysis;

public class EventAbstraction {
	
	String[] event;
	
	public EventAbstraction(String[] eventDescription) {
		event = new String[eventDescription.length];
		for (int i = 0; i < eventDescription.length; i++) {
			event[i] = eventDescription[i];
		}
	}
	
	@Override
	public boolean equals(Object o) {
		boolean equals = false;
		if (o instanceof EventAbstraction) {
			String[] secondEvent = ((EventAbstraction)o).event;
			if (secondEvent.length == event.length) {
				equals = true;
				for (int i = 0; i < event.length; i++) {
					if (event[i] != null && secondEvent[i] != null && !event[i].equals(secondEvent[i])) {
						equals = false;
					} else if (event[i] != null && secondEvent[i] == null) {
						equals = false;
					} else if (event[i] == null && secondEvent[i] != null) {
						equals = false;
					}
				}
			}
		}
		return equals;
	}
	
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("[");
		if (event.length > 0) {
			builder.append(" " + event[0]);
			for (int i = 1; i < event.length; i++) {
				if (event[i] != null) {
					builder.append(", " + event[i]);
				}
			}
		}
		builder.append(" ]");
		return builder.toString();
	}
	
	@Override
	public int hashCode() {
		return toString().hashCode();
	}

}
