

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.regex.Pattern;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.JTree;
import javax.swing.UIManager;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.InputSource;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;


public class PseudoJigen extends MouseAdapter implements ActionListener, TreeSelectionListener {

	public static void main(String[] sArgs) {
		try {
//			String sFontName = "MS PMincho";
			String sFontName = "Simsun (Founder Extended)";
//			String sFontName = "PMingLiU-ExtB";
//			String sFontName = "PMingLiU";
			if (System.getProperty("jigen.fontName") != null) {
				sFontName = System.getProperty("jigen.fontName");
			}
			UIManager.put("TextPane.font",
					new Font(sFontName,
					UIManager.getFont("TextPane.font").getStyle(),
					UIManager.getFont("TextPane.font").getSize()));
			UIManager.put("List.font",
					new Font(sFontName,
					UIManager.getFont("List.font").getStyle(),
					UIManager.getFont("List.font").getSize()));
			UIManager.put("TextField.font",
					new Font(sFontName,
					UIManager.getFont("TextField.font").getStyle(),
					UIManager.getFont("TextField.font").getSize()));
			UIManager.put("Tree.font",
					new Font(sFontName,
					UIManager.getFont("Tree.font").getStyle(),
					UIManager.getFont("Tree.font").getSize()));
			new PseudoJigen();
		} catch (Exception e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(null, e.toString(), "error", JOptionPane.ERROR_MESSAGE);
			System.exit(1);
		}
	}
	
	DocumentBuilder documentBuilder;
	JFrame frame;
	JTextField textField;
	JList selectingList;
	JTabbedPane tabbedPane;
	JTextPane descriptionTextPane;
	JProgressBar progressBar;

	public PseudoJigen() throws Exception {
		documentBuilder = createDocumentBuilder();
		frame = new JFrame("字源もどき");
		{
			JMenuBar menubar = new JMenuBar();
			{
				JMenu menu = new JMenu("File");
				{
					JMenuItem menuItem = new JMenuItem("Exit");
					menuItem.addActionListener(new ActionListener() {
						@Override
						public void actionPerformed(ActionEvent event) {
							System.exit(0);
						}
					});
					menu.add(menuItem);
				}
				menubar.add(menu);
			}
			{
				JMenu menu = new JMenu("Help");
				{
					JMenuItem menuItem = new JMenuItem("About...");
					menuItem.addActionListener(new ActionListener() {
						@Override
						public void actionPerformed(ActionEvent e) {
							JOptionPane.showMessageDialog(frame, "字源もどき version 0.02 by Beu\narbitrary refering: http://wagang.econ.hc.keio.ac.jp/zigen/", "about", JOptionPane.INFORMATION_MESSAGE);
						}
					});
					menu.add(menuItem);
				}
				menubar.add(menu);
			}
			frame.setJMenuBar(menubar);
		}
		{
			JPanel panel = new JPanel(new BorderLayout());
			{
				JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
				{
					JSplitPane splitPane2 = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
					{
						tabbedPane = new JTabbedPane();
						{
							JPanel panel2 = new JPanel();
							BoxLayout layout = new BoxLayout(panel2, BoxLayout.Y_AXIS);
							panel2.setLayout(layout);
							{
								JPanel panel3 = new JPanel();
								{
									textField = new JTextField(8);
									Font font = textField.getFont();
									font = font.deriveFont(font.getSize2D() * 3);
									textField.setFont(font);
									panel3.add(textField);
								}
								panel2.add(panel3);
							}
							{
								JPanel panel3 = new JPanel();
								{
									JButton button = new JButton("字");
									button.setActionCommand("searchCharacter");
									button.addActionListener(this);
									panel3.add(button);
								}
								{
									JButton button = new JButton("音");
									button.setActionCommand("searchReading");
									button.addActionListener(this);
									panel3.add(button);
								}
								{
									JButton button = new JButton("熟語");
									button.setActionCommand("searchWord");
									button.addActionListener(this);
									panel3.add(button);
								}
								{
									JButton button = new JButton("文");
									button.setActionCommand("searchSentence");
									button.addActionListener(this);
									panel3.add(button);
								}
								panel2.add(panel3);
							}
							tabbedPane.addTab("search", panel2);
						}
						splitPane2.add(tabbedPane, JSplitPane.TOP);
					}
					{
						selectingList = new JList();
						Font font = selectingList.getFont();
						font = font.deriveFont(Font.PLAIN, font.getSize2D() * 2.5F);
						selectingList.setFont(font);
						selectingList.addMouseListener(this);
						JScrollPane scrollPane = new JScrollPane(selectingList);
						splitPane2.add(scrollPane, JSplitPane.BOTTOM);
					}
					splitPane.add(splitPane2, JSplitPane.LEFT);
				}
				{
					descriptionTextPane = new JTextPane();
					Font font = descriptionTextPane.getFont();
					font = font.deriveFont(font.getSize2D() * 2);
					descriptionTextPane.setFont(font);
					descriptionTextPane.setEditable(false);
					JScrollPane scrollPane = new JScrollPane(descriptionTextPane);
					splitPane.add(scrollPane, JSplitPane.RIGHT);
				}
				panel.add(splitPane, BorderLayout.CENTER);
			}
			{
				progressBar = new JProgressBar(0, 214 - 1);
				panel.add(progressBar, BorderLayout.SOUTH);
			}
			frame.setContentPane(panel);
		}
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setPreferredSize(new Dimension(800, 600));
		frame.pack();
		frame.setVisible(true);

		{
			Cursor cursor = frame.getCursor();
			frame.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
			JTree tree = new JTree(createTree());
			tree.setFont(tree.getFont().deriveFont(tree.getFont().getSize2D() * 1.5F));
			tree.addTreeSelectionListener(this);
			JScrollPane scrollPane = new JScrollPane(tree);
			tabbedPane.addTab("radical", scrollPane);
			frame.setCursor(cursor);
		}
	}

	final static String RADICALS
			= "一丨丶丿乙亅二亠人儿入八冂冖冫几"
			+ "凵刀力勹匕匚匸十卜卩厂厶又口囗土"
			+ "士夂夊夕大女子宀寸小尢尸屮山巛工"
			+ "己巾干幺广廴廾弋弓彐彡彳心戈戶手"
			+ "支攴文斗斤方无日曰月木欠止歹殳毋"
			+ "比毛氏气水火爪父爻爿片牙牛犬玄玉"
			+ "瓜瓦甘生用田疋疒癶白皮皿目矛矢石"
			+ "示禸禾穴立竹米糸缶网羊羽老而耒耳"
			+ "聿肉臣自至臼舌舛舟艮色艸虍虫血行"
			+ "衣襾見角言谷豆豕豸貝赤走足身車辛"
			+ "辰辵邑酉釆里金長門阜隶隹雨靑非面"
			+ "革韋韭音頁風飛食首香馬骨高髟鬥鬯"
			+ "鬲鬼魚鳥鹵鹿麥麻黃黍黑黹黽鼎鼓鼠"
			+ "鼻齊齒龍龜龠";
	final static int[] RADICAL_STROKES = {
		1, 1, 1, 1, 1, 1, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2,
		2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 3, 3, 3,
		3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3,
		3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 4, 4, 4, 4,
		4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4,
		4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 5, 5,
		5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5,
		5, 5, 5, 5, 5, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6,
		6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6,
		6, 6, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7,
		7, 7, 7, 7, 7, 7, 8, 8, 8, 8, 8, 8, 8, 8, 8, 9,
		9, 9, 9, 9, 9, 9, 9, 9, 9, 9, 10, 10, 10, 10, 10, 10,
		10, 10, 11, 11, 11, 11, 11, 11, 12, 12, 12, 12, 13, 13, 13, 13,
		14, 14, 15, 16, 16, 17
	};
	
	final static String XML_FOLDER = "data";
	TreeMap<String, Set<Integer>> characterMap = new TreeMap<String, Set<Integer>>();
	TreeMap<String, String> wordMap = new TreeMap<String, String>();
	TreeMap<String, Set<String>> readingMap = new TreeMap<String, Set<String>>();

	DefaultMutableTreeNode createTree() throws SAXException, IOException, XPathExpressionException {
		TreeMap<Integer, TreeMap<Integer, TreeMap<Integer, String>>> strokesRadicalStrokesCharactersMap = new TreeMap<Integer, TreeMap<Integer, TreeMap<Integer, String>>>();

		progressBar.setValue(0);
		for (int i = 1;  i <= 214;  ++i) {
			String sRadical = RADICALS.substring(i - 1, i);
			int radicalStrokes = RADICAL_STROKES[i - 1];
			
			TreeMap<Integer, TreeMap<Integer, String>> radicalStrokesCharactersMap = strokesRadicalStrokesCharactersMap.get(radicalStrokes);
			if (radicalStrokesCharactersMap == null) {
				radicalStrokesCharactersMap = new TreeMap<Integer, TreeMap<Integer, String>>();
				strokesRadicalStrokesCharactersMap.put(radicalStrokes, radicalStrokesCharactersMap);
			}
			TreeMap<Integer, String> strokesCharactersMap = radicalStrokesCharactersMap.get(i - 1);
			if (strokesCharactersMap == null) {
				strokesCharactersMap = new TreeMap<Integer, String>();
				radicalStrokesCharactersMap.put(i - 1, strokesCharactersMap);
			}
			int strokes = 0;
			String sCharacters = "";

			String sFile = XML_FOLDER + File.separator + String.format("%03d.xml", i);
			URL url = PseudoJigen.class.getClassLoader().getResource(sFile.replaceAll(Pattern.quote(File.separator), "/"));
			Document document;
			if (url == null) {
				File file = new File(sFile);
				document = documentBuilder.parse(file);
			} else {
				document = documentBuilder.parse(url.openStream());
			}
			// /部首/筆畫/畫數
			// /部首/筆畫/漢字/見出字
			// /部首/筆畫/漢字/字解/音韻/音
			// /部首/筆畫/漢字/熟語/見出語
			XPath xpath = XPathFactory.newInstance().newXPath();
			NodeList nodeList = (NodeList)xpath.evaluate("/部首/筆畫", document, XPathConstants.NODESET);
			for (int j = 0;  j < nodeList.getLength();  ++j) {
				Element element = (Element)nodeList.item(j);
				for (Node node = element.getFirstChild();  node != null;  node = node.getNextSibling()) {
					if (node.getNodeType() != Node.ELEMENT_NODE) {
						continue;
					}
					String sTagName = ((Element)node).getTagName();
					if (sTagName.equals("畫數")) {
						strokes = Integer.parseInt(((Element)node).getTextContent());
						sCharacters = "";
					} else if (sTagName.equals("漢字")) {
						String sCharacter = "";
						for (Node node2 = node.getFirstChild();  node2 != null;  node2 = node2.getNextSibling()) {
							if (node2.getNodeType() != Node.ELEMENT_NODE) {
								continue;
							}
							String sTagName2 = ((Element)node2).getTagName();
							if (sTagName2.equals("見出字")) {
								sCharacter = ((Element)node2).getTextContent();
								if (!sCharacter.equals("？")) {
									sCharacters += sCharacter;
									strokesCharactersMap.put(strokes, sCharacters);
									Set<Integer> radicalSet = characterMap.get(sCharacter);
									if (radicalSet == null) {
										radicalSet = new HashSet<Integer>();
										characterMap.put(sCharacter, radicalSet);
									}
									radicalSet.add(i - 1);
								}
							} else if (sTagName2.equals("熟語")) {
								for (Node node3 = node2.getFirstChild();  node3 != null;  node3 = node3.getNextSibling()) {
									if (node3.getNodeType() != Node.ELEMENT_NODE) {
										continue;
									}
									String sTagName3 = ((Element)node3).getTagName();
									if (sTagName3.equals("見出語")) {
										String sWord = ((Element)node3).getTextContent();
										if (sWord.indexOf("？") < 0) {
											wordMap.put(sWord, sCharacter);
										}
									}
								}
							} else if (sTagName2.equals("字解")) {
								for (Node node3 = node2.getFirstChild();  node3 != null;  node3 = node3.getNextSibling()) {
									if (node3.getNodeType() != Node.ELEMENT_NODE) {
										continue;
									}
									String sTagName3 = ((Element)node3).getTagName();
									if (sTagName3.equals("音韻")) {
										for (Node node4 = node3.getFirstChild();  node4 != null;  node4 = node4.getNextSibling()) {
											if (node4.getNodeType() != Node.ELEMENT_NODE) {
												continue;
											}
											String sTagName4 = ((Element)node4).getTagName();
											if (sTagName4.equals("音")) {
												String sReading = ((Element)node4).getTextContent();
												Set<String> sCharacterSet = readingMap.get(sReading);
												if (sCharacterSet == null) {
													readingMap.put(sReading, sCharacterSet = new TreeSet<String>());
												}
												sCharacterSet.add(sCharacter);
											}
										}
									}
								}
							}
						}
					}
				}
			}
			progressBar.setValue(i - 1);
		}
		progressBar.setValue(0);

		DefaultMutableTreeNode root = new DefaultMutableTreeNode("畫數 / 部首 / 畫數");
		for (Integer radicalStrokes: strokesRadicalStrokesCharactersMap.keySet()) {
			TreeMap<Integer, TreeMap<Integer, String>> radicalStrokesCharactersMap = strokesRadicalStrokesCharactersMap.get(radicalStrokes);
			String sRadicals = "";
			DefaultMutableTreeNode radicalStrokesNode = new DefaultMutableTreeNode(radicalStrokes.toString());
			for (Integer radical: radicalStrokesCharactersMap.keySet()) {
				TreeMap<Integer, String> strokesCharacters = radicalStrokesCharactersMap.get(radical);
				String sRadical = "" + RADICALS.charAt(radical);
				DefaultMutableTreeNode radicalNode = new DefaultMutableTreeNode(sRadical);
				for (Integer strokes: strokesCharacters.keySet()) {
					String sCharacters = strokesCharacters.get(strokes);
					DefaultMutableTreeNode charactersNode = new DefaultMutableTreeNode(strokes.toString() + ":" + sCharacters);
					radicalNode.add(charactersNode);
				}
				sRadicals += sRadical;
				radicalStrokesNode.add(radicalNode);
			}
			radicalStrokesNode.setUserObject(radicalStrokes.toString() + ":" + sRadicals);
			root.add(radicalStrokesNode);
		}
		
		return root;
	}

	DocumentBuilder createDocumentBuilder() {
		DocumentBuilder documentBuilder = null;
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		factory.setNamespaceAware(false);
		factory.setValidating(true);
		factory.setValidating(false);
		factory.setExpandEntityReferences(true);
		factory.setIgnoringElementContentWhitespace(true);
		factory.setIgnoringComments(true);
		factory.setCoalescing(false);
		try {
			documentBuilder = factory.newDocumentBuilder();
		} catch (javax.xml.parsers.ParserConfigurationException e) {
			e.printStackTrace();
			System.exit(1);
		}
		return documentBuilder;
	}

	@Override
	public void actionPerformed(ActionEvent event) {
		String sCommand = event.getActionCommand();
		if (sCommand.equals("searchCharacter")) {
			String sText = textField.getText();
			ArrayList<String> list = new ArrayList<String>();
			for (int i = 0;  i < sText.length();  ++i) {
				String sCharacter = "" + sText.charAt(i);
				int cp = sText.codePointAt(i);
				if (cp > 0xffff) {
					// surrogate pair
					sCharacter = sText.substring(i, i + 2);
					++i;
				}
				if (characterMap.containsKey(sCharacter)) {
					list.add(sCharacter);
				}
			}
			selectingList.setListData(list.toArray(new String[list.size()]));
		} else if (sCommand.equals("searchReading")) {
			String sText = textField.getText();
			Set<String> sCharacterSet = readingMap.get(sText);
			selectingList.setListData(sCharacterSet != null ? sCharacterSet.toArray(new String[sCharacterSet.size()]) : new String[0]);
		} else if (sCommand.equals("searchWord")) {
			String sText = textField.getText();
			if (sText.equals("")) {
				return;
			}
			ArrayList<String> list = new ArrayList<String>();
			for (String sWord: wordMap.keySet()) {
				if (sWord.indexOf(sText) >= 0) {
					list.add(sWord);
				}
			}
			selectingList.setListData(list.toArray(new String[list.size()]));
		} else if (sCommand.equals("searchSentence")) {
			String sText = textField.getText();
			JOptionPane.showMessageDialog(frame, "not supported yet", "error", JOptionPane.ERROR_MESSAGE);
		}
	}

	@Override
	public void valueChanged(TreeSelectionEvent event) {
		TreePath path = event.getPath();
		DefaultMutableTreeNode node = (DefaultMutableTreeNode)path.getLastPathComponent();
		if (!node.isLeaf()) {
			return;
		}
		String s = (String)node.getUserObject();
		String sCharacters = s.split(":")[1];
		ArrayList<String> list = new ArrayList<String>();
		for (int i = 0;  i < sCharacters.length();  ++i) {
			String sCharacter = "" + sCharacters.charAt(i);
			int cp = sCharacters.codePointAt(i);
			if (cp > 0xffff) {
				// surrogate pair
				sCharacter = sCharacters.substring(i, i + 2);
				++i;
			}
			list.add(sCharacter);
		}
		selectingList.setListData(list.toArray(new String[list.size()]));
	}

	@Override
	public void mouseClicked(MouseEvent event) {
		if (event.getClickCount() == 2 && event.getSource() instanceof JList) {
			StyledDocument document = descriptionTextPane.getStyledDocument();
			try {
				document.remove(0, document.getLength());
			} catch (BadLocationException e) {
				e.printStackTrace();
				return;
			}
			String sCharacter = (String)selectingList.getSelectedValue();
			String sWord = null;
			if (sCharacter.length() == 1
					|| sCharacter.length() == 2 && sCharacter.codePointAt(0) > 0xffff) {
				// 單字
			} else {
				// 熟語
				sWord = sCharacter;
				sCharacter = wordMap.get(sWord);
			}
			Set<Integer> radicalSet = characterMap.get(sCharacter);
			for (Integer radical: radicalSet) {
				String sFile = XML_FOLDER + File.separator + String.format("%03d.xml", radical.intValue() + 1);
				try {
					XMLReader xmlReader = XMLReaderFactory.createXMLReader();
					MyContentHandler2 handler = new MyContentHandler2(sCharacter, sWord);
					xmlReader.setContentHandler(handler);
URL url = PseudoJigen.class.getClassLoader().getResource(sFile.replaceAll(Pattern.quote(File.separator), "/"));
if (url == null) {
					xmlReader.parse(new InputSource(new FileInputStream(sFile)));
} else {
					xmlReader.parse(new InputSource(url.openStream()));
}
					descriptionTextPane.setCaretPosition(handler.getPosition());
				} catch (SAXException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	enum State {START_ELEMENT, END_ELEMENT, CHARACTERS};
	abstract class MyContentHandler implements ContentHandler {
		String sPath;
		Attributes attributes;
		@Override
		public void startDocument() throws SAXException {
			sPath = "";
		}
		@Override
		public void endDocument() throws SAXException {
		}
		@Override
		public void startElement(String sUri, String sLocalName, String sName, Attributes attributes) throws SAXException {
			sPath += "/" + sName;
			this.attributes = attributes;
			try {
				path(State.START_ELEMENT, sPath, null);
			} catch (BadLocationException e) {
				e.printStackTrace();
			}
		}
		@Override
		public void endElement(String sUri, String sLocalName, String sName) throws SAXException {
			try {
				path(State.END_ELEMENT, sPath, null);
			} catch (BadLocationException e) {
				e.printStackTrace();
			}
			sPath = sPath.substring(0, sPath.lastIndexOf("/" + sName));
		}
		@Override
		public void characters(char[] characters, int start, int length) throws SAXException {
			try {
				path(State.CHARACTERS, sPath, new String(characters, start, length));
			} catch (BadLocationException e) {
				e.printStackTrace();
			}
		}
		@Override
		public void startPrefixMapping(String sPrefix, String sUri) throws SAXException {
		}
		@Override
		public void endPrefixMapping(String sPrefix) throws SAXException {
		}
		@Override
		public void ignorableWhitespace(char[] characters, int start, int length) throws SAXException {
		}
		@Override
		public void processingInstruction(String sTarget, String sData) throws SAXException {
		}
		@Override
		public void setDocumentLocator(Locator locator) {
		}
		@Override
		public void skippedEntity(String sName) throws SAXException {
		}

		abstract public void path(State state, String sPath, String sData) throws BadLocationException;
	}

	enum State2 {BEFORE, JUST, AFTER};
	class MyContentHandler2 extends MyContentHandler {
		String sCharacter;
		String sWord;
		int position = 0;
		String sRadical;
		State2 state2;
		int strokes;
		StyledDocument document = descriptionTextPane.getStyledDocument();
		SimpleAttributeSet attributeSet0;
		SimpleAttributeSet attributeSet;
		String sText;
		public int getPosition() {
			return position;
		}
		public MyContentHandler2(String sCharacter, String sWord) {
			this.sCharacter = sCharacter;
			this.sWord = sWord;
			state2 = State2.BEFORE;
			attributeSet0 = new SimpleAttributeSet();
			StyleConstants.setForeground(attributeSet0, Color.BLACK);
			StyleConstants.setBackground(attributeSet0, Color.WHITE);
			StyleConstants.setLineSpacing(attributeSet0, 4);
			StyleConstants.setFontSize(attributeSet0, 24);
		}
		@Override
		public void path(State state, String sPath, String sData) throws BadLocationException {
			switch (state2) {
			case BEFORE:
				if (sPath.equals("/部首/部首字") && state == State.CHARACTERS) {
					sRadical = sData;
				} else if (sPath.equals("/部首/筆畫/畫數") && state == State.CHARACTERS) {
					strokes = Integer.parseInt(sData);
				} else if (sPath.equals("/部首/筆畫/漢字/見出字") && state == State.CHARACTERS) {
					if (sData.equals(sCharacter)) {
						state2 = State2.JUST;
						break;
					}
				}
				return;
			case AFTER:
				return;
			case JUST:
				break;
			}
			if (sPath.equals("/部首/筆畫/漢字") && state == State.END_ELEMENT) {
				state2 = State2.AFTER;
				return;
			}
			if (sPath.equals("/部首/筆畫/漢字/見出字") && state == State.CHARACTERS) {
				attributeSet = new SimpleAttributeSet(attributeSet0);
				StyleConstants.setFontSize(attributeSet, StyleConstants.getFontSize(attributeSet) * 2);
				StyleConstants.setBold(attributeSet, true);
				sText = "【" + sCharacter + "】";
				document.insertString(document.getLength(), sText, attributeSet);
				sText = sRadical + Integer.toString(strokes) + "畫\n";
				document.insertString(document.getLength(), sText, attributeSet0);
			} else if (sPath.equals("/部首/筆畫/漢字/字解")) {
			} else if (sPath.equals("/部首/筆畫/漢字/字解/音韻") && state == State.START_ELEMENT) {
				sText = "[音韻] ";
				document.insertString(document.getLength(), sText, attributeSet0);
			} else if (sPath.equals("/部首/筆畫/漢字/字解/音韻/號") && state == State.CHARACTERS) {
				sText = sData;
				document.insertString(document.getLength(), sText, attributeSet0);
			} else if (sPath.equals("/部首/筆畫/漢字/字解/音韻/音") && state == State.CHARACTERS) {
				sText = sData + " ";
				document.insertString(document.getLength(), sText, attributeSet0);
			} else if (sPath.equals("/部首/筆畫/漢字/字解/音韻/韻") && state == State.CHARACTERS) {
				sText = "(" + sData + ") ";
				document.insertString(document.getLength(), sText, attributeSet0);
			} else if (sPath.equals("/部首/筆畫/漢字/字解/音韻") && state == State.END_ELEMENT) {
				sText = "\n";
				document.insertString(document.getLength(), sText, attributeSet0);
			} else if (sPath.equals("/部首/筆畫/漢字/字解/字解註") && state == State.START_ELEMENT) {
				sText = "※ ";
				document.insertString(document.getLength(), sText, attributeSet0);
			} else if (sPath.equals("/部首/筆畫/漢字/字解/字解註") && state == State.CHARACTERS) {
				sText = sData.replaceAll("^\\s+|\\s+$", "");
				document.insertString(document.getLength(), sText, attributeSet0);
			} else if (sPath.equals("/部首/筆畫/漢字/字解/字解註/標識") && state == State.CHARACTERS) {
				attributeSet = new SimpleAttributeSet(attributeSet0);
				StyleConstants.setForeground(attributeSet, StyleConstants.getBackground(attributeSet0));
				StyleConstants.setBackground(attributeSet, StyleConstants.getForeground(attributeSet0));
				sText = " " + sData + " ";
				document.insertString(document.getLength(), sText, attributeSet);
			} else if (sPath.equals("/部首/筆畫/漢字/字解/字解註") && state == State.END_ELEMENT) {
				sText = "\n";
				document.insertString(document.getLength(), sText, attributeSet0);
			} else if (sPath.equals("/部首/筆畫/漢字/字解/解") && state == State.START_ELEMENT) {
				sText = "[解]\n";
				document.insertString(document.getLength(), sText, attributeSet0);
			} else if (sPath.equals("/部首/筆畫/漢字/字解/解/號") && state == State.CHARACTERS) {
				sText = sData;
				document.insertString(document.getLength(), sText, attributeSet0);
			} else if (sPath.equals("/部首/筆畫/漢字/字解/解/義") && state == State.CHARACTERS) {
				sText = sData.replaceAll("^\\s+|\\s+$", "");
				document.insertString(document.getLength(), sText, attributeSet0);
			} else if (sPath.equals("/部首/筆畫/漢字/字解/解/義/音") && state == State.CHARACTERS) {
				sText = "(" + sData + ") ";
				document.insertString(document.getLength(), sText, attributeSet0);
			} else if (sPath.equals("/部首/筆畫/漢字/字解/解/義/標識") && state == State.CHARACTERS) {
				attributeSet = new SimpleAttributeSet(attributeSet0);
				StyleConstants.setForeground(attributeSet, StyleConstants.getBackground(attributeSet0));
				StyleConstants.setBackground(attributeSet, StyleConstants.getForeground(attributeSet0));
				sText = " " + sData + " ";
				document.insertString(document.getLength(), sText, attributeSet);
			} else if (sPath.equals("/部首/筆畫/漢字/字解/解/義/返点") && state == State.START_ELEMENT) {
				attributeSet = new SimpleAttributeSet(attributeSet0);
				StyleConstants.setSuperscript(attributeSet, true);
				StyleConstants.setFontSize(attributeSet, StyleConstants.getFontSize(attributeSet) * 3 / 5);
				StyleConstants.setForeground(attributeSet, Color.DARK_GRAY);
				sText = attributes.getValue("type");
				document.insertString(document.getLength(), sText, attributeSet);
			} else if (sPath.equals("/部首/筆畫/漢字/字解/解/義") && state == State.END_ELEMENT) {
				sText = "\n";
				document.insertString(document.getLength(), sText, attributeSet0);
			} else if (sPath.equals("/部首/筆畫/漢字/字解/解") && state == State.END_ELEMENT) {
				sText = "\n";
				document.insertString(document.getLength(), sText, attributeSet0);
//			} else if (sPath.equals("/部首/筆畫/漢字/字解/解字") && state == State.START_ELEMENT) {
//				sText = "[解字]\n";
//				document.insertString(document.getLength(), sText, attributeSet0);
			} else if (sPath.equals("/部首/筆畫/漢字/字解/解字") && state == State.CHARACTERS) {
				sText = sData.replaceAll("^\\s+|\\s+$", "");
				document.insertString(document.getLength(), sText, attributeSet0);
			} else if (sPath.equals("/部首/筆畫/漢字/字解/解字/標識") && state == State.CHARACTERS) {
				attributeSet = new SimpleAttributeSet(attributeSet0);
				StyleConstants.setForeground(attributeSet, StyleConstants.getBackground(attributeSet0));
				StyleConstants.setBackground(attributeSet, StyleConstants.getForeground(attributeSet0));
				sText = " " + sData + " ";
				document.insertString(document.getLength(), sText, attributeSet);
			} else if (sPath.equals("/部首/筆畫/漢字/字解/解字") && state == State.END_ELEMENT) {
				sText = "\n";
				document.insertString(document.getLength(), sText, attributeSet0);
			} else if (sPath.equals("/部首/筆畫/漢字/字解/同訓") && state == State.CHARACTERS) {
			} else if (sPath.equals("/部首/筆畫/漢字/字解/同訓/標識") && state == State.CHARACTERS) {
				attributeSet = new SimpleAttributeSet(attributeSet0);
				StyleConstants.setForeground(attributeSet, StyleConstants.getBackground(attributeSet0));
				StyleConstants.setBackground(attributeSet, StyleConstants.getForeground(attributeSet0));
				sText = " " + sData + " ";
				document.insertString(document.getLength(), sText, attributeSet);
			} else if (sPath.equals("/部首/筆畫/漢字/字解/同訓/訓") && state == State.CHARACTERS) {
				sText = "【" + sData + "】\n";
				document.insertString(document.getLength(), sText, attributeSet0);
			} else if (sPath.equals("/部首/筆畫/漢字/字解/同訓/同訓字") && state == State.CHARACTERS) {
				sText = sData;
				document.insertString(document.getLength(), sText, attributeSet0);
			} else if (sPath.equals("/部首/筆畫/漢字/字解/同訓/同訓解") && state == State.CHARACTERS) {
				sText = sData + "\n";
				document.insertString(document.getLength(), sText, attributeSet0);
			} else if (sPath.equals("/部首/筆畫/漢字/字解/同訓") && state == State.END_ELEMENT) {
			} else if (sPath.equals("/部首/筆畫/漢字/熟語") && state == State.START_ELEMENT) {
				sText = "------------\n";
				document.insertString(document.getLength(), sText, attributeSet0);
			} else if (sPath.equals("/部首/筆畫/漢字/熟語/見出語") && state == State.START_ELEMENT) {
				sText = "【";
				document.insertString(document.getLength(), sText, attributeSet0);
			} else if (sPath.equals("/部首/筆畫/漢字/熟語/見出語") && state == State.CHARACTERS) {
				sText = sData.replaceAll("^\\s+|\\s+$", "");
				if (sWord != null && sWord.equals(sData)) {
					position = document.getLength();
				}
				document.insertString(document.getLength(), sText, attributeSet0);
			} else if (sPath.equals("/部首/筆畫/漢字/熟語/見出語/返点") && state == State.START_ELEMENT) {
				attributeSet = new SimpleAttributeSet(attributeSet0);
				StyleConstants.setSuperscript(attributeSet, true);
				StyleConstants.setFontSize(attributeSet, StyleConstants.getFontSize(attributeSet) * 3 / 5);
				StyleConstants.setForeground(attributeSet, Color.DARK_GRAY);
				sText = attributes.getValue("type");
				document.insertString(document.getLength(), sText, attributeSet);
			} else if (sPath.equals("/部首/筆畫/漢字/熟語/見出語") && state == State.END_ELEMENT) {
				sText = "】 ";
				document.insertString(document.getLength(), sText, attributeSet0);
			} else if (sPath.equals("/部首/筆畫/漢字/熟語/音") && state == State.CHARACTERS) {
				attributeSet = new SimpleAttributeSet(attributeSet0);
				StyleConstants.setFontSize(attributeSet, StyleConstants.getFontSize(attributeSet) * 4 / 5);
				sText = sData + "\n";
				document.insertString(document.getLength(), sText, attributeSet);
			} else if (sPath.equals("/部首/筆畫/漢字/熟語/解")) {
			} else if (sPath.equals("/部首/筆畫/漢字/熟語/解/義") && state == State.CHARACTERS) {
				sText = sData.replaceAll("^\\s+|\\s+$", "");
				document.insertString(document.getLength(), sText, attributeSet0);
			} else if (sPath.equals("/部首/筆畫/漢字/熟語/解/義/音") && state == State.CHARACTERS) {
				sText = "(" + sData + ") ";
				document.insertString(document.getLength(), sText, attributeSet0);
			} else if (sPath.equals("/部首/筆畫/漢字/熟語/解/義/標識") && state == State.CHARACTERS) {
				attributeSet = new SimpleAttributeSet(attributeSet0);
				StyleConstants.setForeground(attributeSet, StyleConstants.getBackground(attributeSet0));
				StyleConstants.setBackground(attributeSet, StyleConstants.getForeground(attributeSet0));
				sText = " " + sData + " ";
				document.insertString(document.getLength(), sText, attributeSet);
			} else if (sPath.equals("/部首/筆畫/漢字/熟語/解/義/返点") && state == State.START_ELEMENT) {
				attributeSet = new SimpleAttributeSet(attributeSet0);
				StyleConstants.setSuperscript(attributeSet, true);
				StyleConstants.setFontSize(attributeSet, StyleConstants.getFontSize(attributeSet) * 3 / 5);
				StyleConstants.setForeground(attributeSet, Color.DARK_GRAY);
				sText = attributes.getValue("type");
				document.insertString(document.getLength(), sText, attributeSet);
			} else if (sPath.equals("/部首/筆畫/漢字/熟語/解/義") && state == State.END_ELEMENT) {
				sText = "\n";
				document.insertString(document.getLength(), sText, attributeSet0);
			} else if (sPath.equals("/部首/筆畫/漢字/熟語/解/图")) {
			}
		}
	}
}
