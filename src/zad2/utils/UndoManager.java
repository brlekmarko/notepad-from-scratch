package zad2.utils;

import java.util.Stack;

public class UndoManager {
	
	private Stack<EditAction> undoStack, redoStack;
	private static UndoManager instance = new UndoManager();
	private static int maxStackSize = 50;
	
	private UndoManager() {
		this.undoStack = new Stack<>();
		this.redoStack = new Stack<>();
	}
	
	public static UndoManager getInstance() {
		return instance;
	}
	
	public void undo() {
		if(undoStack.isEmpty()) {
			System.out.println("Undo stack is empty");
			return;
		}
		EditAction action = undoStack.pop();
		redoStack.push(action);
		if(redoStack.size() > maxStackSize) {
			redoStack.remove(0);
		}
		action.execute_undo();

	}

	public void redo() {
		if(redoStack.isEmpty()) {
			System.out.println("Redo stack is empty");
			return;
		}
		EditAction action = redoStack.pop();
		action.execute_do();
		undoStack.push(action);
		if(undoStack.size() > maxStackSize) {
			undoStack.remove(0);
		}
	}
	
	public void push(EditAction c) {
		redoStack.clear();
		undoStack.push(c);
		if(undoStack.size() > maxStackSize) {
			undoStack.remove(0);
		}
	}
	
	public boolean isUndoEmpty() {
		return undoStack.isEmpty();
	}
	
	public boolean isRedoEmpty() {
		return redoStack.isEmpty();
	}

	public Stack<EditAction> getUndoStack() {
		return undoStack;
	}

	public void setUndoStack(Stack<EditAction> undoStack) {
		this.undoStack = undoStack;
	}

	public Stack<EditAction> getRedoStack() {
		return redoStack;
	}

	public void setRedoStack(Stack<EditAction> redoStack) {
		this.redoStack = redoStack;
	}

	public void clear(){
		undoStack.clear();
		redoStack.clear();
	}
}
