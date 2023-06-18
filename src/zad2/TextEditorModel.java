package zad2;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import zad2.utils.CursorObserver;
import zad2.utils.EditAction;
import zad2.utils.Location;
import zad2.utils.LocationRange;
import zad2.utils.TextObserver;
import zad2.utils.UndoManager;

public class TextEditorModel {
	
	private String[] lines;
	private LocationRange selectionRange;
	private Location cursorLocation;
	
	private List<CursorObserver> cursorObservers;
	private List<TextObserver> textObservers;
	private UndoManager undoManager;
	
	
	public TextEditorModel(String initialText) {
		lines = initialText.split("\n");
		cursorLocation = new Location(lines.length, lines[lines.length-1].length()+1);
		selectionRange = new LocationRange(new Location(lines.length, lines[lines.length-1].length()+1), new Location(lines.length, lines[lines.length-1].length()+1));
		cursorObservers = new ArrayList<>();
		textObservers = new ArrayList<>();
		undoManager = UndoManager.getInstance();
	}


	public String[] getLines() {
		return lines;
	}


	public void setLines(String[] lines) {
		this.lines = lines;
	}


	public LocationRange getSelectionRange() {
		return selectionRange;
	}


	public void setSelectionRange(LocationRange selectionRange) {
		this.selectionRange = selectionRange;
	}


	public Location getCursorLocation() {
		return cursorLocation;
	}


	public void setCursorLocation(Location cursorLocation) {
		this.cursorLocation = cursorLocation;
	}
	
	public Iterator<String> allLines(){
		return new Iterator<String>() {
			int current = 0;
			@Override
			public boolean hasNext() {
				return current<lines.length;
			}

			@Override
			public String next() {
				if(hasNext()) {
					return lines[current++];
				}
				throw new ArrayIndexOutOfBoundsException();
			}
			
		};
	}
	
	public Iterator<String> linesRange(int index1, int index2){
		return new Iterator<String>() {
			int current = index1;
			@Override
			public boolean hasNext() {
				if(index2 > lines.length) return current<lines.length;
				return current<index2;
			}

			@Override
			public String next() {
				if(hasNext()) {
					return lines[current++];
				}
				throw new ArrayIndexOutOfBoundsException();
			}
			
		};
	}

	public String getSelectedText(){
		if(selectionRange.getStart().getRow() == selectionRange.getEnd().getRow()) {
			return lines[selectionRange.getStart().getRow()-1].substring(selectionRange.getStart().getColumn()-1, selectionRange.getEnd().getColumn()-1);
		}
		StringBuilder sb = new StringBuilder();
		sb.append(lines[selectionRange.getStart().getRow()-1].substring(selectionRange.getStart().getColumn()-1));
		for(int i = selectionRange.getStart().getRow(); i < selectionRange.getEnd().getRow()-1; i++) {
			sb.append("\n");
			sb.append(lines[i]);
		}
		sb.append("\n");
		sb.append(lines[selectionRange.getEnd().getRow()-1].substring(0, selectionRange.getEnd().getColumn()-1));
		return sb.toString();
	}
	
	public void addCursorObserver(CursorObserver co) {
		cursorObservers.add(co);
	}
	
	public void removeCursorObserver(CursorObserver co) {
		cursorObservers.remove(co);
	}
	
	public void notifyCursorObservers(Location cursorLocation) {
		for(CursorObserver co : cursorObservers) {
			co.updateCursorLocation(cursorLocation);
		}
	}
	
	public void moveCursorLeft() {
		if(cursorLocation.getColumn() <= 1 && cursorLocation.getRow() == 1) return;
		if(cursorLocation.getColumn() <= 1) {
			cursorLocation.setRow(cursorLocation.getRow()-1);
			cursorLocation.setColumn(lines[cursorLocation.getRow()-1].length()+1);
			notifyCursorObservers(cursorLocation);
			return;
		}
		cursorLocation.setColumn(cursorLocation.getColumn()-1);
		notifyCursorObservers(cursorLocation);
	}
	
	public void moveCursorRight() {
		if(cursorLocation.getColumn() > lines[cursorLocation.getRow()-1].length() && cursorLocation.getRow() == lines.length) return;
		if(cursorLocation.getColumn() > lines[cursorLocation.getRow()-1].length()) {
			cursorLocation.setRow(cursorLocation.getRow()+1);
			cursorLocation.setColumn(1);
			notifyCursorObservers(cursorLocation);
			return;
		}
		cursorLocation.setColumn(cursorLocation.getColumn()+1);
		notifyCursorObservers(cursorLocation);
	}
	
	public void moveCursorUp() {
		if(cursorLocation.getRow() <= 1) return;
		cursorLocation.setRow(cursorLocation.getRow()-1);
		if(cursorLocation.getColumn() > lines[cursorLocation.getRow()-1].length()) {
			cursorLocation.setColumn(lines[cursorLocation.getRow()-1].length()+1);
		}
		notifyCursorObservers(cursorLocation);
	}
	
	public void moveCursorDown() {
		if(cursorLocation.getRow() >= lines.length) return;
		cursorLocation.setRow(cursorLocation.getRow()+1);
		if(cursorLocation.getColumn() > lines[cursorLocation.getRow()-1].length()) {
			cursorLocation.setColumn(lines[cursorLocation.getRow()-1].length()+1);
		}
		notifyCursorObservers(cursorLocation);
	}
	
	
	public void addTextObserver(TextObserver to) {
		textObservers.add(to);
	}
	
	public void removeTextObserver(TextObserver to) {
		textObservers.remove(to);
	}
	
	public void notifyTextObservers() {
		for(TextObserver to : textObservers) {
			to.updateText();
		}
	}
	
	
	public void deleteBefore(){
		EditAction deleteBeforeAction = new EditAction() {

			String[] oldLines = lines.clone();
			Location oldCursorLocation = cursorLocation.copy();
			LocationRange oldSelectionRange = selectionRange.copy();

			@Override
			public void execute_do() {
				if(cursorLocation.getColumn() == 1 && cursorLocation.getRow() == 1) return;
				if(cursorLocation.getColumn() == 1) {
					//na pocetku reda smo, trebamo obrisati newline
					int oldLength = lines[cursorLocation.getRow()-2].length();
					String line = lines[cursorLocation.getRow()-2];
					line = line + lines[cursorLocation.getRow()-1];
					lines[cursorLocation.getRow()-2] = line;
					String[] newLines = new String[lines.length-1];
					// kopiramo sve linije do trenutne
					for(int i = 0; i < cursorLocation.getRow()-1; i++) {
						newLines[i] = lines[i];
					}
					// kopiramo sve linije poslije trenutne
					for(int i = cursorLocation.getRow(); i < lines.length; i++) {
						newLines[i-1] = lines[i];
					}
					lines = newLines;
					cursorLocation.setRow(cursorLocation.getRow()-1);
					cursorLocation.setColumn(oldLength+1);
					selectionRange = new LocationRange(cursorLocation.copy(), cursorLocation.copy());
					notifyTextObservers();
					notifyCursorObservers(cursorLocation);
				}
				else {
					//negdje usred reda smo, treba samo obrisati jedan znak
					String line = lines[cursorLocation.getRow()-1];
					line = line.substring(0, cursorLocation.getColumn()-2) + line.substring(cursorLocation.getColumn()-1); //obrisemo taj znak
					lines[cursorLocation.getRow()-1] = line;
					cursorLocation.setColumn(cursorLocation.getColumn()-1);
					selectionRange = new LocationRange(cursorLocation.copy(), cursorLocation.copy());
					notifyTextObservers();
					notifyCursorObservers(cursorLocation);
				}
			}

			@Override
			public void execute_undo() {
				lines = oldLines.clone();
				cursorLocation = oldCursorLocation.copy();
				selectionRange = oldSelectionRange.copy();
				notifyTextObservers();
				notifyCursorObservers(cursorLocation);
			}		
		};
		undoManager.push(deleteBeforeAction);
		deleteBeforeAction.execute_do();
	}
	
	public void deleteAfter() {

		EditAction deleteAfterAction = new EditAction() {

			String[] oldLines = lines.clone();
			Location oldCursorLocation = cursorLocation.copy();
			LocationRange oldSelectionRange = selectionRange.copy();

			@Override
			public void execute_do() {
				if(cursorLocation.getColumn() == lines[cursorLocation.getRow()-1].length()+1 && cursorLocation.getRow() == lines.length) return;
				if(cursorLocation.getColumn() == lines[cursorLocation.getRow()-1].length()+1) {
					//na kraju reda smo, trebamo obrisati newline
					String line = lines[cursorLocation.getRow()-1];
					line = line + lines[cursorLocation.getRow()];
					lines[cursorLocation.getRow()-1] = line;
					String[] newLines = new String[lines.length-1];
					// kopiramo sve linije do trenutne
					for(int i = 0; i < cursorLocation.getRow(); i++) {
						newLines[i] = lines[i];
					}
					// kopiramo sve linije poslije trenutne
					for(int i = cursorLocation.getRow()+1; i < lines.length; i++) {
						newLines[i-1] = lines[i];
					}
					lines = newLines;
					selectionRange = new LocationRange(cursorLocation.copy(), cursorLocation.copy());
					notifyTextObservers();
					notifyCursorObservers(cursorLocation);
				}
				else {
					//negdje usred reda smo, treba samo obrisati jedan znak
					String line = lines[cursorLocation.getRow()-1];
					line = line.substring(0, cursorLocation.getColumn()-1) + line.substring(cursorLocation.getColumn()); //obrisemo taj znak
					lines[cursorLocation.getRow()-1] = line;
					selectionRange = new LocationRange(cursorLocation.copy(), cursorLocation.copy());
					notifyTextObservers();
					notifyCursorObservers(cursorLocation);
				}
			}

			@Override
			public void execute_undo() {
				lines = oldLines.clone();
				cursorLocation = oldCursorLocation.copy();
				selectionRange = oldSelectionRange.copy();
				notifyTextObservers();
				notifyCursorObservers(cursorLocation);
			}
		};

		undoManager.push(deleteAfterAction);
		deleteAfterAction.execute_do();
	}

	public void deleteRange(LocationRange r){

		EditAction deleteRangeAction = new EditAction() {

			String[] oldLines = lines.clone();
			Location oldCursorLocation = cursorLocation.copy();
			LocationRange oldSelectionRange = selectionRange.copy();

			@Override
			public void execute_do() {
				if(r.getStart().getRow() == r.getEnd().getRow()) {
					//brisanje u istom redu
					String line = lines[r.getStart().getRow()-1];
					line = line.substring(0, r.getStart().getColumn()-1) + line.substring(r.getEnd().getColumn()-1); // obrisemo taj dio
					lines[r.getStart().getRow()-1] = line;
					cursorLocation = r.getStart(); // postavimo kursor na pocetak brisanog dijela
					selectionRange = new LocationRange(cursorLocation.copy(), cursorLocation.copy());
					notifyTextObservers();
					notifyCursorObservers(cursorLocation);
				}
				else {
					//brisanje u vise redova
					String line = lines[r.getStart().getRow()-1];
					line = line.substring(0, r.getStart().getColumn()-1); // obrisemo cijeli ostatak prve linije
					lines[r.getStart().getRow()-1] = line;

					line = lines[r.getEnd().getRow()-1];
					line = line.substring(r.getEnd().getColumn()-1); // obrisemo pocetak zadnje linije do zadanog stupca
					lines[r.getStart().getRow()-1] += line; // dodamo ostatak zadnje linije na prvu

					String[] newLines = new String[lines.length-(r.getEnd().getRow()-r.getStart().getRow())];
					// kopiramo sve linije do brisanog dijela
					for(int i = 0; i < r.getStart().getRow(); i++) {
						newLines[i] = lines[i];
					}
					// kopiramo sve linije poslije brisanog dijela
					for(int i = r.getEnd().getRow(); i < lines.length; i++) {
						newLines[i-(r.getEnd().getRow()-r.getStart().getRow()+1)] = lines[i];
					}
					lines = newLines;
					cursorLocation = r.getStart();
					selectionRange = new LocationRange(cursorLocation.copy(), cursorLocation.copy());
					notifyTextObservers();
					notifyCursorObservers(cursorLocation);
				}
			}

			@Override
			public void execute_undo() {
				lines = oldLines.clone();
				cursorLocation = oldCursorLocation.copy();
				selectionRange = oldSelectionRange.copy();
				notifyTextObservers();
				notifyCursorObservers(cursorLocation);
			}
		};

		undoManager.push(deleteRangeAction);
		deleteRangeAction.execute_do();
	}

	public void insert(char c){

		EditAction insertAction = new EditAction() {

			String[] oldLines = lines.clone();
			Location oldCursorLocation = cursorLocation.copy();
			LocationRange oldSelectionRange = selectionRange.copy();

			@Override
			public void execute_do() {
				//umece znak na mjesto gdje je kursor
				if(c == '\n'){
					//umece novi red
					String[] newLines = new String[lines.length+1];
					// kopiramo sve linije do trenutne
					for(int i = 0; i < cursorLocation.getRow()-1; i++) {
						newLines[i] = lines[i];
					}
					newLines[cursorLocation.getRow()-1] = lines[cursorLocation.getRow()-1].substring(0, cursorLocation.getColumn()-1);
					newLines[cursorLocation.getRow()] = lines[cursorLocation.getRow()-1].substring(cursorLocation.getColumn()-1);
					// kopiramo sve linije poslije trenutne
					for(int i = cursorLocation.getRow(); i < lines.length; i++) {
						newLines[i+1] = lines[i];
					}
					lines = newLines;
					cursorLocation.setRow(cursorLocation.getRow()+1);
					cursorLocation.setColumn(1);
					selectionRange = new LocationRange(cursorLocation.copy(), cursorLocation.copy());
					notifyTextObservers();
					notifyCursorObservers(cursorLocation);
					return;
				}
				String line = lines[cursorLocation.getRow()-1];
				line = line.substring(0, cursorLocation.getColumn()-1) + c + line.substring(cursorLocation.getColumn()-1); //umece taj znak
				lines[cursorLocation.getRow()-1] = line;
				cursorLocation.setColumn(cursorLocation.getColumn()+1);
				selectionRange = new LocationRange(cursorLocation.copy(), cursorLocation.copy());
				notifyTextObservers();
				notifyCursorObservers(cursorLocation);
			}

			@Override
			public void execute_undo() {
				lines = oldLines.clone();
				cursorLocation = oldCursorLocation.copy();
				selectionRange = oldSelectionRange.copy();
				notifyTextObservers();
				notifyCursorObservers(cursorLocation);
			}
		};

		undoManager.push(insertAction);
		insertAction.execute_do();
	}

	public void insert(String text){

		EditAction insertStringAction = new EditAction() {

			String[] oldLines = lines.clone();
			Location oldCursorLocation = cursorLocation.copy();
			LocationRange oldSelectionRange = selectionRange.copy();

			@Override
			public void execute_do() {
				//umece proizvoljan tekst na mjesto gdje je kursor
				String[] newText = text.split("\n");
				String line = lines[cursorLocation.getRow()-1];
				if (newText.length == 1) {
					//umece tekst u isti red
					line = line.substring(0, cursorLocation.getColumn()-1) + newText[0] + line.substring(cursorLocation.getColumn()-1); //umece taj tekst
					lines[cursorLocation.getRow()-1] = line;
					cursorLocation.setColumn(cursorLocation.getColumn()+newText[0].length());
					selectionRange = new LocationRange(cursorLocation.copy(), cursorLocation.copy());
					notifyTextObservers();
					notifyCursorObservers(cursorLocation);
				}
				else {
					//umece tekst u vise redova
					String visak = line.substring(cursorLocation.getColumn()-1); //spremimo visak teksta u trenutnom redu
					line = line.substring(0, cursorLocation.getColumn()-1) + newText[0]; //umece taj tekst
					lines[cursorLocation.getRow()-1] = line;
					String[] newLines = new String[lines.length+newText.length-1];
					// kopiramo sve linije do selekcije
					for(int i = 0; i < cursorLocation.getRow(); i++) {
						newLines[i] = lines[i];
					}
					// kopiramo sve linije poslije selekcije
					for(int i = cursorLocation.getRow(); i < lines.length; i++) {
						newLines[i+newText.length-1] = lines[i];
					}
					// kopiramo nove linije
					for(int i = 1; i < newText.length; i++) {
						newLines[cursorLocation.getRow()+i-1] = newText[i];
					}
					newLines[cursorLocation.getRow()] = newLines[cursorLocation.getRow()] + visak;
					lines = newLines;
					cursorLocation.setRow(cursorLocation.getRow()+newText.length-1);
					cursorLocation.setColumn(newText[newText.length-1].length()+1);
					selectionRange = new LocationRange(cursorLocation.copy(), cursorLocation.copy());
					notifyTextObservers();
					notifyCursorObservers(cursorLocation);
				}
			}

			@Override
			public void execute_undo() {
				lines = oldLines.clone();
				cursorLocation = oldCursorLocation.copy();
				selectionRange = oldSelectionRange.copy();
				notifyTextObservers();
				notifyCursorObservers(cursorLocation);
			}
		};

		undoManager.push(insertStringAction);
		insertStringAction.execute_do();
	}


	public void moveCursorToStart() {
		cursorLocation = new Location(1, 1);
		selectionRange = new LocationRange(cursorLocation.copy(), cursorLocation.copy());
		notifyCursorObservers(cursorLocation);
	}

	public void moveCursorToEnd() {
		cursorLocation = new Location(lines.length, lines[lines.length-1].length()+1);
		selectionRange = new LocationRange(cursorLocation.copy(), cursorLocation.copy());
		notifyCursorObservers(cursorLocation);
	}

	public String getText(){
		StringBuilder sb = new StringBuilder();
		for (String line : lines) {
			sb.append(line);
			sb.append("\n");
		}
		return sb.toString();
	}

	public void setText(String text){
		String[] newText = text.split("\n");
		lines = newText;
		cursorLocation = new Location(lines.length, lines[lines.length-1].length()+1);
		selectionRange = new LocationRange(cursorLocation.copy(), cursorLocation.copy());
		notifyTextObservers();
		notifyCursorObservers(cursorLocation);
	}

	public boolean isSomethingSelected() {
		return !selectionRange.getStart().equals(selectionRange.getEnd());
	}
	
}
