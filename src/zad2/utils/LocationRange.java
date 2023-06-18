package zad2.utils;

import java.util.Objects;

public class LocationRange {
	
	private Location start, end;
	
	public LocationRange(Location start, Location end) {
		this.start = start;
		this.end = end;
	}

	public Location getStart() {
		return start;
	}

	public void setStart(Location start) {
		this.start = start;
	}

	public Location getEnd() {
		return end;
	}

	public void setEnd(Location end) {
		this.end = end;
	}

	@Override
	public int hashCode() {
		return Objects.hash(end, start);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		LocationRange other = (LocationRange) obj;
		return Objects.equals(end, other.end) && Objects.equals(start, other.start);
	}

	public LocationRange copy() {
		return new LocationRange(start.copy(), end.copy());
	}
	
	
}
