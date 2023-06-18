package zad2;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

import zad2.utils.ActionListener;
import zad2.utils.ActionProvider;
import zad2.utils.ClipboardStack;
import zad2.utils.CursorObserver;
import zad2.utils.Location;
import zad2.utils.LocationRange;
import zad2.utils.StatusListener;
import zad2.utils.TextObserver;
import zad2.utils.UndoManager;

public class TextEditor extends JComponent implements CursorObserver, TextObserver, ActionProvider{

	private TextEditorModel textModel;
	private static final long serialVersionUID = 1L;
	private static int padding = 10;
	private ClipboardStack clipStack;
	private UndoManager undoManager;
	private List<ActionListener> actionListeners = new ArrayList<>();
	private List<StatusListener> statusListeners = new ArrayList<>();
	
	
	public TextEditor(TextEditorModel model) {
		this.textModel = model;
		addKeysListener();
		setFocusable(true);
		textModel.addCursorObserver(this);
		textModel.addTextObserver(this);
		clipStack = ClipboardStack.getInstance();
		undoManager = UndoManager.getInstance();
	}
	
	
	public TextEditor() {
		this(new TextEditorModel("Neki inicijalni tekst\nu vise redaka\ntreci red"));
	}
	
	public TextEditorModel getTextModel() {
		return textModel;
	}

	public void setTextModel(TextEditorModel textModel) {
		this.textModel = textModel;
	}

	
	public void addKeysListener() {
		this.addKeyListener(new KeyListener() {
			
			@Override
			public void keyTyped(KeyEvent e) {
			}
			
			@Override
			public void keyReleased(KeyEvent e) {
			}
			
			@Override
			public void keyPressed(KeyEvent e) {
				int keyCode = e.getKeyCode();
				switch (keyCode) {

					case KeyEvent.VK_UP:
						doMoveCursorUp(e);
						break;

					case KeyEvent.VK_DOWN:
						doMoveCursorDown(e);
						break;

					case KeyEvent.VK_LEFT:
						doMoveCursorLeft(e);
						break;

					case KeyEvent.VK_RIGHT :
						doMoveCursorRight(e);
						break;

					case KeyEvent.VK_BACK_SPACE:
						doDeleteBefore();
						break;

					case KeyEvent.VK_DELETE:
						doDeleteAfter();
						break;
					case KeyEvent.VK_ENTER:
						insertChar('\n');
						break;
					case KeyEvent.VK_C:
						if(e.isControlDown()) {
							doCopy();
							break;
						}
						insertChar(e.getKeyChar());
						break;
					case KeyEvent.VK_X:
						if(e.isControlDown()) {
							doCut();
							break;
						}
						insertChar(e.getKeyChar());
						break;
					case KeyEvent.VK_V:
						if(e.isControlDown() && e.isShiftDown()) {
							doPasteAndTake();
							break;
						}
						if(e.isControlDown()) {
							doPaste();
							break;
						}
						insertChar(e.getKeyChar());
						break;
					case KeyEvent.VK_Z:
						if(e.isControlDown()){
							doUndo();
							break;
						}
						insertChar(e.getKeyChar());
						break;
					case KeyEvent.VK_Y:
						if(e.isControlDown()){
							doRedo();
							break;
						}
						insertChar(e.getKeyChar());
						break;
					default:
						if(e.isControlDown()) break;
						if(e.getKeyChar() == KeyEvent.CHAR_UNDEFINED) break;
						insertChar(e.getKeyChar());
						//notifyActionListeners("all", true);
						break;
				}
			}
		});
	}

	public void paintComponent(Graphics g) {
		
		Graphics2D g2d = (Graphics2D)g; //specifikacija kaze da se ovo smije
		g2d.drawRect(0, 0, this.getWidth()-1, this.getHeight()-1);
		
		Font font = g2d.getFont();
		FontMetrics fontMetrics = getFontMetrics(font);
		
		int currentY = 0;
		Location cursorLocation = this.textModel.getCursorLocation();
		
		Iterator<String> iter = textModel.allLines();
		String s = "";
		int row = 1;
		while(iter.hasNext()) {
			s = iter.next();
			// ako je range samo jedan znak, ne treba bojati pozadinu
			if(textModel.getSelectionRange().getStart().equals(textModel.getSelectionRange().getEnd())) {
				g2d.drawString(s, padding, fontMetrics.getHeight() + currentY);
				currentY += fontMetrics.getHeight() + padding;
				row++;
				continue;
			}
			//inace, imamo range

			// ako je redak izvan selekcije, nacrtaj ga normalno
			if(row < textModel.getSelectionRange().getStart().getRow() || row > textModel.getSelectionRange().getEnd().getRow()) {
				g2d.drawString(s, padding, fontMetrics.getHeight() + currentY);
				currentY += fontMetrics.getHeight() + padding;
				row++;
				continue;
			}
			// inace, u selekciji je, stavi mu pozadinsku boju na dijelovima koji su u selekciji
			if(row > textModel.getSelectionRange().getStart().getRow() && row < textModel.getSelectionRange().getEnd().getRow()) {
				// ako je cijeli redak u selekciji, oboji ga cijelog
				g2d.setColor(Color.lightGray);
				g2d.fillRect(padding, currentY + padding/2, fontMetrics.stringWidth(s), fontMetrics.getHeight());
				g2d.setColor(Color.black);
				g2d.drawString(s, padding, fontMetrics.getHeight() + currentY);
				currentY += fontMetrics.getHeight() + padding;
				row++;
				continue;
			}
			// ako je samo dio reda u selekciji, oboji samo dio
			if(row == textModel.getSelectionRange().getStart().getRow()) {
				// oboji dio od pocetka selecije do kraja selekcije ili kraja reda
				if(row == textModel.getSelectionRange().getEnd().getRow()) {
					// bojamo od pocetka selekcije do kraja selekcije
					g2d.setColor(Color.lightGray);
					g2d.fillRect(padding + fontMetrics.stringWidth(s.substring(0, textModel.getSelectionRange().getStart().getColumn()-1))
							, currentY + padding/2, 
							fontMetrics.stringWidth(s.substring(textModel.getSelectionRange().getStart().getColumn()-1, textModel.getSelectionRange().getEnd().getColumn()-1)), 
							fontMetrics.getHeight());
					g2d.setColor(Color.black);
					g2d.drawString(s, padding, fontMetrics.getHeight() + currentY);
					currentY += fontMetrics.getHeight() + padding;
					row++;
					continue;
				}
				// bojamo od pocetka selekcije do kraja reda
				g2d.setColor(Color.lightGray);
				g2d.fillRect(padding + fontMetrics.stringWidth(s.substring(0, textModel.getSelectionRange().getStart().getColumn()-1))
						, currentY + padding/2, 
						fontMetrics.stringWidth(s.substring(textModel.getSelectionRange().getStart().getColumn()-1)), 
						fontMetrics.getHeight());
				g2d.setColor(Color.black);
				g2d.drawString(s, padding, fontMetrics.getHeight() + currentY);
				currentY += fontMetrics.getHeight() + padding;
				row++;
				continue;
			}

			if(row == textModel.getSelectionRange().getEnd().getRow()) {
				// bojamo od pocetka reda do kraja selekcije
				g2d.setColor(Color.lightGray);
				g2d.fillRect(padding, currentY + padding/2, 
						fontMetrics.stringWidth(s.substring(0, textModel.getSelectionRange().getEnd().getColumn()-1)), 
						fontMetrics.getHeight());
				g2d.setColor(Color.black);
				g2d.drawString(s, padding, fontMetrics.getHeight() + currentY);
				currentY += fontMetrics.getHeight() + padding;
				row++;
				continue;
			}
		}

		//crtanje kursora
		s = textModel.getLines()[cursorLocation.getRow()-1];
		int cursorX = fontMetrics.stringWidth(s.substring(0, cursorLocation.getColumn()-1)) + padding;
		int cursorY = (fontMetrics.getHeight() + padding) * (cursorLocation.getRow()-1);
		g2d.drawLine(cursorX, cursorY + padding/2, cursorX, cursorY + fontMetrics.getHeight());
		
		
	}


	@Override
	public void updateCursorLocation(Location loc) {
		repaint();
	}


	@Override
	public void updateText() {
		repaint();
	}


	@Override
	public void doOpen() {
		// https://stackoverflow.com/questions/3548140/how-to-open-and-save-using-java
		JFileChooser jfc = new JFileChooser();
		jfc.setDialogTitle("Open file");
		if(jfc.showOpenDialog(null) != JFileChooser.APPROVE_OPTION) {
			JOptionPane.showMessageDialog(null, "Odaberite datoteku", "Warning", JOptionPane.WARNING_MESSAGE);
			return;
		}
		Path path = jfc.getSelectedFile().toPath();
		if(!Files.isReadable(path)) {
			JOptionPane.showMessageDialog(null, "Datoteka nije citljiva", "Warning", JOptionPane.WARNING_MESSAGE);
			return;
		}
		try {
			List<String> lines = Files.readAllLines(path);
			StringBuilder sb = new StringBuilder();
			for(String s : lines) {
				sb.append(s);
				sb.append("\n");
			}
			textModel.setText(sb.toString());
			undoManager.clear();
			notifyListeners();
		} catch (IOException e) {
			JOptionPane.showMessageDialog(null, "Greska prilikom citanja datoteke", "Warning", JOptionPane.WARNING_MESSAGE);
			return;
		}
		
	}


	@Override
	public void doSave() {
		// https://stackoverflow.com/questions/3548140/how-to-open-and-save-using-java
		JFileChooser jfc = new JFileChooser();
		jfc.setDialogTitle("Save file");
		if(jfc.showSaveDialog(null) != JFileChooser.APPROVE_OPTION) {
			JOptionPane.showMessageDialog(null, "Odaberite datoteku", "Warning", JOptionPane.WARNING_MESSAGE);
			return;
		}
		Path path = jfc.getSelectedFile().toPath();
		if(Files.exists(path)) {
			int rez = JOptionPane.showConfirmDialog(null, "Datoteka vec postoji, zelite li prebrisati?", "Warning", JOptionPane.YES_NO_OPTION);
			if(rez != JOptionPane.YES_OPTION) {
				return;
			}
		}
		try {
			Files.write(path, textModel.getText().getBytes(StandardCharsets.UTF_8));
			notifyListeners();
		} catch (IOException e) {
			JOptionPane.showMessageDialog(null, "Greska prilikom spremanja", "Error", JOptionPane.ERROR_MESSAGE);
			return;
		}
		
	}

	private void doMoveCursorUp(KeyEvent e){
		// ako drzimo shift, potrebno je promjeniti selectionRange
		if(e.isShiftDown()) {
			textModel.moveCursorUp();
			// ako je vec nesto selektirano iznad kursora, pomicemo pocetak selekcije na gore
			// inace pomicemo kraj selekcije dolje
			if(textModel.getSelectionRange().getStart().compareTo(textModel.getCursorLocation()) < 0) {
				textModel.setSelectionRange(new LocationRange(textModel.getSelectionRange().getStart(), textModel.getCursorLocation().copy()));
			}else {
				textModel.setSelectionRange(new LocationRange(textModel.getCursorLocation().copy(), textModel.getSelectionRange().getEnd()));
			}
			notifyActionListeners("Cut", textModel.isSomethingSelected());
			notifyActionListeners("Copy", textModel.isSomethingSelected());
		}else {
			textModel.moveCursorUp();
			textModel.setSelectionRange(new LocationRange(textModel.getCursorLocation().copy(), textModel.getCursorLocation().copy()));
		}
		notifyListeners();
	}

	private void doMoveCursorDown(KeyEvent e){
		if(e.isShiftDown()) {
			textModel.moveCursorDown();
			// ako je vec nesto selektirano ispod kursora, pomicemo pocetak selekcije na dolje
			// inace pomicemo kraj selekcije gore
			if(textModel.getSelectionRange().getEnd().compareTo(textModel.getCursorLocation()) > 0) {
				textModel.setSelectionRange(new LocationRange(textModel.getCursorLocation().copy(), textModel.getSelectionRange().getEnd()));
			}else {
				textModel.setSelectionRange(new LocationRange(textModel.getSelectionRange().getStart(), textModel.getCursorLocation().copy()));
			}
		}else {
			textModel.moveCursorDown();
			textModel.setSelectionRange(new LocationRange(textModel.getCursorLocation().copy(), textModel.getCursorLocation().copy()));
		}
		notifyListeners();
	}

	private void doMoveCursorLeft(KeyEvent e){
		if(e.isShiftDown()) {
			textModel.moveCursorLeft();
			// ako je vec nesto selektirano lijevo od kursora, pomicemo kraj selekcije na lijevo
			// inace pomicemo pocetak selekcije na lijevo
			if(textModel.getSelectionRange().getStart().compareTo(textModel.getCursorLocation()) < 0) {
				textModel.setSelectionRange(new LocationRange(textModel.getSelectionRange().getStart(), textModel.getCursorLocation().copy()));
			}else {
				textModel.setSelectionRange(new LocationRange(textModel.getCursorLocation().copy(), textModel.getSelectionRange().getEnd()));
			}
		}else {
			textModel.moveCursorLeft();
			textModel.setSelectionRange(new LocationRange(textModel.getCursorLocation().copy(), textModel.getCursorLocation().copy()));
		}
		notifyListeners();
	}

	private void doMoveCursorRight(KeyEvent e){
		if(e.isShiftDown()) {
			textModel.moveCursorRight();
			// ako je vec nesto selektirano desno od kursora, pomicemo pocetak selekcije na desno
			// inace pomicemo kraj selekcije na desno
			if(textModel.getSelectionRange().getEnd().compareTo(textModel.getCursorLocation()) > 0) {
				textModel.setSelectionRange(new LocationRange(textModel.getCursorLocation().copy(), textModel.getSelectionRange().getEnd()));
			}else {
				textModel.setSelectionRange(new LocationRange(textModel.getSelectionRange().getStart(), textModel.getCursorLocation().copy()));
			}

		}else {
			textModel.moveCursorRight();
			textModel.setSelectionRange(new LocationRange(textModel.getCursorLocation().copy(), textModel.getCursorLocation().copy()));
		}
		notifyListeners();
	}

	private void doDeleteBefore(){
		if(textModel.getSelectionRange().getStart().equals(textModel.getSelectionRange().getEnd())) {
			textModel.deleteBefore();
		}else {
			doDeleteSelection();
		}
	}

	private void doDeleteAfter(){
		if(textModel.getSelectionRange().getStart().equals(textModel.getSelectionRange().getEnd())) {
			textModel.deleteAfter();
		}else {
			doDeleteSelection();
		}
	}

	@Override
	public void doUndo() {
		undoManager.undo();
		notifyListeners();
	}


	@Override
	public void doRedo() {
		undoManager.redo();
		notifyListeners();
	}


	@Override
	public void doCut() {
		if(!textModel.getSelectionRange().getStart().equals(textModel.getSelectionRange().getEnd())) {
			// kopiramo selekciju u clipboard
			clipStack.push(textModel.getSelectedText());
			// brisemo selekciju
			textModel.deleteRange(textModel.getSelectionRange());
			notifyListeners();
		}
	}
	
	@Override
	public void doCopy() {
		// kopiramo selekciju u clipboard
		if(!textModel.getSelectionRange().getStart().equals(textModel.getSelectionRange().getEnd())) {
			clipStack.push(textModel.getSelectedText());
			notifyListeners();
		}
	}


	@Override
	public void doPaste() {
		if(clipStack.isEmpty()) {
			return;
		}
		if(textModel.getSelectionRange().getStart().equals(textModel.getSelectionRange().getEnd())) {
			textModel.insert(clipStack.peek());
		}else {
			// prvo brisemo sve sto je selektirano
			textModel.deleteRange(textModel.getSelectionRange());
			textModel.insert(clipStack.peek());
		}
		notifyListeners();
	}


	@Override
	public void doPasteAndTake() {
		if(clipStack.isEmpty()) {
			return;
		}
		if(textModel.getSelectionRange().getStart().equals(textModel.getSelectionRange().getEnd())) {
			textModel.insert(clipStack.pop());
		}else {
			// prvo brisemo sve sto je selektirano
			textModel.deleteRange(textModel.getSelectionRange());
			textModel.insert(clipStack.pop());
		}
		notifyListeners();
	}


	@Override
	public void doDeleteSelection() {
		textModel.deleteRange(textModel.getSelectionRange());
		notifyListeners();
	}


	@Override
	public void doClear() {
		textModel.deleteRange(new LocationRange(new Location(1, 1),
							new Location(textModel.getLines().length, textModel.getLines()[textModel.getLines().length-1].length()+1)));
		notifyListeners();
	}



	@Override
	public void doMoveCursorToStart() {
		textModel.moveCursorToStart();
		notifyListeners();
	}


	@Override
	public void doMoveCursorToEnd() {
		textModel.moveCursorToEnd();
		notifyListeners();
	}
	

	@Override
	public void addActionListener(ActionListener l) {
		actionListeners.add(l);
	}

	@Override
	public void removeActionListener(ActionListener l) {
		actionListeners.remove(l);
	}

	public void notifyActionListeners(String actionName, boolean enable) {
		for(ActionListener l : actionListeners) {
			l.setEnable(actionName, enable);
		}
	}

	public void insertChar(char c) {
		if(textModel.getSelectionRange().getStart().equals(textModel.getSelectionRange().getEnd())) {
			textModel.insert(c);
		}else {
			// prvo brisemo sve sto je selektirano
			doDeleteSelection();
			textModel.insert(c);
		}
		notifyListeners();
	}

	public void notifyListeners(){
		notifyActionListeners("Cut", textModel.isSomethingSelected());
		notifyActionListeners("Copy", textModel.isSomethingSelected());
		notifyActionListeners("Paste", !clipStack.isEmpty());
		notifyActionListeners("Paste and Take", !clipStack.isEmpty());
		notifyActionListeners("Delete Selection", textModel.isSomethingSelected());
		notifyActionListeners("Undo", !undoManager.isUndoEmpty());
		notifyActionListeners("Redo", !undoManager.isRedoEmpty());
		notifyStatusListeners();
	}

	public void addStatusListener(StatusListener l) {
		statusListeners.add(l);
		String initialText = "Cursor is in row " + textModel.getCursorLocation().getRow() + " column " + textModel.getCursorLocation().getColumn() + ". ";
		initialText += "Text has " + textModel.getLines().length + " rows.";
		l.statusChanged(initialText);
	}

	public void removeStatusListener(StatusListener l) {
		statusListeners.remove(l);
	}

	public void notifyStatusListeners() {
		for(StatusListener l : statusListeners) {
			String statusMessage = "Cursor is in row " + textModel.getCursorLocation().getRow() + " column " + textModel.getCursorLocation().getColumn() + ". ";
			statusMessage += "Text has " + textModel.getLines().length + " rows.";
			l.statusChanged(statusMessage);
		}
	}
}
