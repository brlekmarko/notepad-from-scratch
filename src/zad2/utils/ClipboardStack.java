package zad2.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

public class ClipboardStack {
	
	private Stack<String> texts;
	private List<ClipboardObserver> clipboardObservers;
	private static ClipboardStack instance = new ClipboardStack();
	
	private ClipboardStack() {
		texts = new Stack<>();
		clipboardObservers = new ArrayList<>();
	}

	public static ClipboardStack getInstance() {
		return instance;
	}

	public Stack<String> getTexts() {
		return texts;
	}

	public void setTexts(Stack<String> texts) {
		this.texts = texts;
	}
	
	public String pop() {
		String toReturn = texts.pop();
		notifyClipboardObservers();
		return toReturn;
	}
	
	public String peek() {
		String toReturn = texts.peek();
		notifyClipboardObservers();
		return toReturn;
	}
	
	public String push(String item) {
		String toReturn = texts.push(item);
		notifyClipboardObservers();
		return toReturn;
	}
	
	public void addClipboardObserver(ClipboardObserver co) {
		clipboardObservers.add(co);
	}
	
	public void removeClipboardObserver(ClipboardObserver co) {
		clipboardObservers.remove(co);
	}
	
	public void notifyClipboardObservers() {
		for(ClipboardObserver co : clipboardObservers) {
			co.updateClipboard(texts);
		}
	}

	public boolean isEmpty() {
		return texts.isEmpty();
	}
	

}
