package zad2;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;
import javax.swing.AbstractAction;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JPanel;
import javax.swing.JToolBar;
import zad2.utils.ActionListener;
import zad2.utils.ActionProvider;
import zad2.utils.ClipboardStack;
import zad2.utils.Plugin;
import zad2.utils.StatusListener;
import zad2.utils.UndoManager;

public class Main extends JFrame{


	private static final long serialVersionUID = -6999130523326522853L;
	private TextEditor textEditor;

	public Main() {
		setTitle("Text Editor");
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		setSize(500, 500);
		setVisible(true);
		initGUI();
	}
	
	
	private void initGUI() {
		
		Container pane = this.getContentPane();
		pane.setLayout(new BorderLayout());
		
		JPanel midPanel = new JPanel(new BorderLayout());
		textEditor = new TextEditor();
		midPanel.add(textEditor, BorderLayout.CENTER);

		pane.add(midPanel, BorderLayout.CENTER);


		ActionProvider actionProvider = (ActionProvider) textEditor;

		JPanel topLabel = new JPanel(new BorderLayout());

		JMenuBar menuBar = new JMenuBar();
		
		JMenu fileMenu = new JMenu("File");
		createFileMenu(fileMenu, actionProvider);
		menuBar.add(fileMenu);

		JMenu editMenu = new JMenu("Edit");
		createEditMenu(editMenu, actionProvider);
		menuBar.add(editMenu);

		JMenu moveMenu = new JMenu("Move");
		createMoveMenu(moveMenu, actionProvider);
		menuBar.add(moveMenu);

		JMenu pluginMenu = new JMenu("Plugins");
		createPluginMenu(pluginMenu);
		menuBar.add(pluginMenu);

		topLabel.add(menuBar, BorderLayout.NORTH);

		JToolBar toolBar = new JToolBar();
		toolBar.setFloatable(false);

		createToolBarItem(toolBar, actionProvider, "Undo", actionProvider::doUndo);
		createToolBarItem(toolBar, actionProvider, "Redo", actionProvider::doRedo);
		createToolBarItem(toolBar, actionProvider, "Cut", actionProvider::doCut);
		createToolBarItem(toolBar, actionProvider, "Copy", actionProvider::doCopy);
		createToolBarItem(toolBar, actionProvider, "Paste", actionProvider::doPaste);

		topLabel.add(toolBar, BorderLayout.CENTER);


		JLabel statusLabel = new JLabel("Status: ");
		textEditor.addStatusListener(new StatusListener() {
			@Override
			public void statusChanged(String status) {
				statusLabel.setText(status);
			}
		});
		
		pane.add(statusLabel, BorderLayout.SOUTH);


		pane.add(topLabel, BorderLayout.NORTH);
		
	}


	private void createFileMenu(JMenu fileMenu, ActionProvider actionProvider) {
		addMenuItem(fileMenu, actionProvider, "Open", true, actionProvider::doOpen);
		addMenuItem(fileMenu, actionProvider, "Save", true, actionProvider::doSave);
		addMenuItem(fileMenu, actionProvider, "Exit", true, () -> {
			this.dispose();
		});
	}

	private void createEditMenu(JMenu editMenu, ActionProvider actionProvider) {
		addMenuItem(editMenu, actionProvider, "Undo", false, actionProvider::doUndo);
		addMenuItem(editMenu, actionProvider, "Redo", false, actionProvider::doRedo);
		addMenuItem(editMenu, actionProvider, "Cut", false, actionProvider::doCut);
		addMenuItem(editMenu, actionProvider, "Copy", false, actionProvider::doCopy);
		addMenuItem(editMenu, actionProvider, "Paste", false, actionProvider::doPaste);
		addMenuItem(editMenu, actionProvider, "Paste and Take", false, actionProvider::doPasteAndTake);
		addMenuItem(editMenu, actionProvider, "Delete Selection", false, actionProvider::doDeleteSelection);
		addMenuItem(editMenu, actionProvider, "Clear", true, actionProvider::doClear);
	}

	private void createMoveMenu(JMenu moveMenu, ActionProvider actionProvider) {
		addMenuItem(moveMenu, actionProvider, "Move Cursor to Start", true, actionProvider::doMoveCursorToStart);
		addMenuItem(moveMenu, actionProvider, "Move Cursor to End", true, actionProvider::doMoveCursorToEnd);
	}

	private void addMenuItem(JMenu menu, ActionProvider actionProvider, String name, boolean initEnabled, Runnable action) {
		menu.add(new AbstractAction() {
			private static final long serialVersionUID = 1L;
			private ActionListener actionListener = new ActionListener() {
				public void setEnable(String actionName, boolean enable) {
					if(actionName.equals(name)) {
						setEnabled(enable);
					}
				}
			};
			{
				init();
			}
			public AbstractAction init() {
				putValue(NAME, name);
				setEnabled(initEnabled);
				actionProvider.addActionListener(actionListener);
				return this;
			}

			public void actionPerformed(ActionEvent e) {
				if(isEnabled()) action.run();
			};

		});
	}

	private void createToolBarItem(JToolBar toolBar, ActionProvider actionProvider, String name, Runnable action) {
		toolBar.add(new AbstractAction() {
			private static final long serialVersionUID = 1L;
			private ActionListener actionListener = new ActionListener() {
				public void setEnable(String actionName, boolean enable) {
					if(actionName.equals(name)) {
						setEnabled(enable);
					}
				}
			};
			{
				init();
			}
			public AbstractAction init() {
				putValue(NAME, name);
				setEnabled(false);
				actionProvider.addActionListener(actionListener);
				return this;
			}

			public void actionPerformed(ActionEvent e) {
				if(isEnabled()) action.run();
			};

		});
	}

	private void createPluginMenu(JMenu pluginMenu) {
		List<Plugin> plugins = loadPlugins();
		//List<Plugin> plugins = new ArrayList<>();
		for(Plugin plugin : plugins) {
			pluginMenu.add(new AbstractAction() {
				private static final long serialVersionUID = 1L;
				{
					putValue(NAME, plugin.getName());
				}
				public void actionPerformed(ActionEvent e) {
					plugin.execute(textEditor.getTextModel(), UndoManager.getInstance(), ClipboardStack.getInstance());
				};
			});
		}
	}

	private List<Plugin> loadPlugins(){
		// https://stackoverflow.com/questions/32222151/how-to-load-all-compiled-class-from-a-folder

		String location = "C:/Java/plugins-texteditor/";
		List<Plugin> plugins = new ArrayList<>();
		
		
		ClassLoader parent = Main.class.getClassLoader();
		try {
			try (URLClassLoader loader = new URLClassLoader(new URL[] {new File(location).toURI().toURL()}, parent)) {
				for (String f : new File(location).list()) {
					if (f.endsWith(".class")) {
						// listamo po svim fajlovima u folderu, trazimo .class
						String name = f.substring(0, f.length() - 6);
						Class<?> c = loader.loadClass("zad2.plugins." + name);
						// ako je klasa tipa Plugin, dodajemo je u listu
						if (Plugin.class.isAssignableFrom(c)) {
							plugins.add((Plugin)c.getConstructor().newInstance());
						}
					}
				}
			}
			catch (MalformedURLException e) {
				e.printStackTrace();
			}
		} catch (NoClassDefFoundError | Exception e) {
			e.printStackTrace();
		}

		return plugins;
	}

	
	public static void main(String args[]) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				new Main();
			}
		});
	}
}
