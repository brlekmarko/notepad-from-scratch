package zad2.plugins;

import zad2.TextEditorModel;
import zad2.utils.ClipboardStack;
import zad2.utils.Plugin;
import zad2.utils.UndoManager;

public class VelikoSlovoPlugin implements Plugin{

	@Override
	public String getName() {
		return "Veliko slovo";
	}

	@Override
	public String getDescription() {
		return "Prolazi kroz dokument i svako prvo slovo riječi mijenja u veliko (\"ovo je tekst\" ==> \"Ovo Je Tekst\").";
	}

	@Override
	public void execute(TextEditorModel model, UndoManager undoManager, ClipboardStack clipboardStack) {

		String text = model.getText();

		StringBuilder sb = new StringBuilder();
		boolean prvoSlovo = true;

		// idemo po svim znakovima, ako je prije znaka bio razmak, tab ili novi red, znak je prvo slovo rijeci
		// u tom slucaju ga pretvaramo u veliko slovo
		for(int i = 0; i < text.length(); i++) {
			char c = text.charAt(i);
			if(prvoSlovo) {
				sb.append(Character.toUpperCase(c));
				prvoSlovo = false;
			} else {
				sb.append(c);
			}
			if(c == ' ' || c == '\n' || c == '\t') {
				prvoSlovo = true;
			}
		}
		
		model.setText(sb.toString());
	}

}
