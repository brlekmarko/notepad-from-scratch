package zad2.utils;

public interface ActionProvider {

	void doOpen();
	void doSave();
	void doUndo();
	void doRedo();
	void doCut();
	void doCopy();
	void doPaste();
	void doPasteAndTake();
	void doDeleteSelection();
	void doClear();
	void doMoveCursorToStart();
	void doMoveCursorToEnd();


	void addActionListener(ActionListener al);
	void removeActionListener(ActionListener al);
}
