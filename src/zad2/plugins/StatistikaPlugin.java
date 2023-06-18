package zad2.plugins;

import javax.swing.JOptionPane;

import zad2.TextEditorModel;
import zad2.utils.ClipboardStack;
import zad2.utils.Plugin;
import zad2.utils.UndoManager;

public class StatistikaPlugin implements Plugin{

	@Override
	public String getName() {
		return "Statistika";
	}

	@Override
	public String getDescription() {
		return "Plugin koji broji koliko ima redaka, riječi i slova u dokumentu i to prikazuje korisniku u dijalogu.";
	}

	@Override
	public void execute(TextEditorModel model, UndoManager undoManager, ClipboardStack clipboardStack) {
		int brojRedaka = model.getLines().length;
		int brojRijeci = model.getText().split("\\s+").length; // ovaj regex splita po svim whitespaceovima
		int brojSlova = model.getText().length();
		
		String message = "Broj redaka: " + brojRedaka + "\n" + 
						"Broj rijeci: " + brojRijeci + "\n" + 
						"Broj slova: " + brojSlova;

		JOptionPane.showMessageDialog(null, message, "Statistika", JOptionPane.INFORMATION_MESSAGE);
	}

}
