package zad2.utils;

import java.util.Objects;

public class Location {
	
	private int row,column;
	
	public Location(int row, int column) {
		this.row = row;
		this.column = column;
	}

	public int getRow() {
		return row;
	}

	public void setRow(int row) {
		this.row = row;
	}

	public int getColumn() {
		return column;
	}

	public void setColumn(int column) {
		this.column = column;
	}

	public Location copy() {
		return new Location(row, column);
	}

	@Override
	public int hashCode() {
		return Objects.hash(column, row);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Location other = (Location) obj;
		return column == other.column && row == other.row;
	}

	public int compareTo(Location o) {
		if(row == o.row) {
			return column - o.column;
		}
		return row - o.row;
	}
	

}
