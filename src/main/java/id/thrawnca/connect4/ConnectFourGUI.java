package id.thrawnca.connect4;

import java.awt.*;
import java.awt.event.*;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import javax.swing.*;
import java.util.Map;
import java.util.HashMap;

//import org.antuar.carl.net.*;

import static id.thrawnca.connect4.ConnectFourGrid.Colour;

public class ConnectFourGUI extends JFrame {

  /** The size, in pixels, of a cell in the Connect Four grid. */
  private static final int CELL_SIZE = 50;

  /** The types of player that can participate in a game.
  private static final int COMPUTER = 0,
               HUMAN = 1,
               NETWORK = 2;*/

  /** The port number used for network games. */
  //private static final int PORT = 4321;

  /** Whether or not to generate debugging messages. */
  //private boolean debug = true;

  /** Whether the game is active. */
  private boolean active;

  /** The currently-active player. */
  private Colour currentPlayer;

  private PlayerChoiceDialog dlgWhiteChoice;

  private PlayerChoiceDialog dlgBlackChoice;

  /** The human/computer status of the two players. */
  private Map<Colour, Player> players = new HashMap<Colour, Player>();

  /** The AI for the two players (AI of human players is null). */
  private Map<Colour, AbstractAI> playerAI = new HashMap<Colour, AbstractAI>();

  /** The win/loss history of this session. */
  private Map<Colour, Integer> history = new HashMap<Colour, Integer>();

  // GUI components

  /** Dialog displaying the session history. */
  private GameHistoryDialog dlgHistory;

  /** Backing grid. */
  private ConnectFourGrid grid;

  private JMenuBar menuBar;
    private JMenu mnuGame;
      /** Starts a new game. */
      private JMenuItem mniNewGame;

      /** Displays the session history. */
      private JMenuItem mniHistory;

      /** Exits the game. */
      private JMenuItem mniExit;

  /** Contains the other components in the GUI. */
  private Box contents;

  //~ private Box gridBox;
    private JPanel viewPanel;
    private JLabel[][] gridDisplay;


  private Box columnChoiceBox;
    // Column choice via buttons (one over each column)
    private Box columnChoiceButtons;

    // Column choice via text box
    private JPanel textColumnChoicePanel;
      private JTextField txtColumnChoice;
      private JLabel lblColumnChoice;

  private JPanel statusPanel;
    /** Displays a status message. */
    private JLabel lblStatusMessage;

  // The exit button
  private JPanel exitPanel;
  private JButton btnExit;

  public ConnectFourGUI() {
    super("Connect Four");

    // create game history
    history.put(Colour.Black, 0);
    history.put(Colour.White, 0);
    history.put(null, 0);

    dlgHistory = new GameHistoryDialog();

    // create dialogs to choose player types
    dlgWhiteChoice = new PlayerChoiceDialog(Colour.White);
    dlgBlackChoice = new PlayerChoiceDialog(Colour.Black);

    // game starts off disabled
    active = false;

    // create backing grid
    grid = new ConnectFourGrid();

    // create menu bar
    menuBar = new JMenuBar();
    mnuGame = new JMenu("Game");

    // 'new game' option
    mniNewGame = new JMenuItem("New");
    mniNewGame.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          // ask for confirmation if a game is active
          if (!active || JOptionPane.showConfirmDialog(ConnectFourGUI.this, "Are you sure to want to start a new game?", "Confirm new game", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
            startGame();
          }
        }
      }
    );
    mniNewGame.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N, ActionEvent.CTRL_MASK));

    // 'history' option
    mniHistory = new JMenuItem("Session History");
    mniHistory.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) { dlgHistory.setVisible(true); }
      }
    );
    mniHistory.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_H, ActionEvent.CTRL_MASK));

    // 'exit' option
    mniExit = new JMenuItem("Exit");
    mniExit.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) { btnExit.doClick(); }
      }
    );
    mniExit.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Q, ActionEvent.CTRL_MASK));

    mnuGame.add(mniNewGame);
    mnuGame.add(mniHistory);
    mnuGame.add(mniExit);

    menuBar.add(mnuGame);

    setJMenuBar(menuBar);

    // create content box
    contents = Box.createVerticalBox();

    // create status message panel
    statusPanel = new JPanel();
    lblStatusMessage = new JLabel("To start a new game, click Game-New.");
    statusPanel.add(lblStatusMessage);

    // create game view panel
    viewPanel = new JPanel();
    viewPanel.setLayout(new GridLayout(grid.getRows(), grid.getColumns()));
    gridDisplay = new JLabel[grid.getRows()][grid.getColumns()];

    // populate game view panel
    for (int i = grid.getRows() - 1; i >= 0; i--) {
      for (int j = 0; j < grid.getColumns(); j++) {
        gridDisplay[i][j] = new GridLabel(i, j);
        gridDisplay[i][j].setPreferredSize(new Dimension(CELL_SIZE, CELL_SIZE));
        viewPanel.add(gridDisplay[i][j]);
      }
    }

    // resize view panel
    viewPanel.setPreferredSize(new Dimension(CELL_SIZE * grid.getColumns(), CELL_SIZE * grid.getRows()));

    // create column choice box
    columnChoiceBox = Box.createVerticalBox();

    // create column choice buttons
    columnChoiceButtons = Box.createHorizontalBox();
    for (int i = 0; i < grid.getColumns(); i++) {
      JButton button = new ColumnChoiceButton(i);
      button.setPreferredSize(new Dimension(CELL_SIZE, (int) button.getPreferredSize().getHeight()));
      columnChoiceButtons.add(button);
    }

    // create textual column choice panel
    textColumnChoicePanel = new JPanel();

    // creat column choice text field
    lblColumnChoice = new JLabel("Choose a column (1-"+grid.getColumns()+"):");
    lblColumnChoice.addMouseListener(new MouseAdapter() {
        public void mouseClicked(MouseEvent e) { txtColumnChoice.requestFocus(); }
      }
    );
    txtColumnChoice = new JTextField(10);
    txtColumnChoice.addKeyListener(new KeyAdapter() {
        public void keyPressed(KeyEvent e) {
          // add piece on enter
          if (e.getKeyCode() == KeyEvent.VK_ENTER) {
            if (!active) return;  // shouldn't happen but be on safe side
            // check column choice for validity
            if (checkColumnChoice()) {
              // determine column
              int column = Integer.parseInt(txtColumnChoice.getText()) - 1;

              // add piece
              addPiece(column);
              if (active) computerTurn();
            }

            // clear text field
            txtColumnChoice.setText("");
            txtColumnChoice.requestFocus();
          }
        }
      }
    );

    // add components to panel
    textColumnChoicePanel.add(lblColumnChoice);
    textColumnChoicePanel.add(txtColumnChoice);

    // add components to column choice box
    columnChoiceBox.add(textColumnChoicePanel);
    columnChoiceBox.add(columnChoiceButtons);

    // create exit button
    exitPanel = new JPanel();

    btnExit = new JButton("Exit");
    btnExit.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) { ConnectFourGUI.this.dispose(); }
      }
    );
    exitPanel.add(btnExit);

    // add components to content box
    contents.add(statusPanel);
    contents.add(columnChoiceBox);
    contents.add(viewPanel);
    contents.add(exitPanel);

    // add content box to frame
    getContentPane().add(contents);

    // lock controls until game starts
    toggleControls(false);
  }

  private void updateView() {
    for (int i = 0; i < grid.getRows(); i++) {
      for (int j = 0; j < grid.getColumns(); j++) {
        gridDisplay[i][j].repaint();
      }
    }
  }

  /**
   * Enables or disables the user's controls.
   * @param enabled Whether the controls will be enabled after this method
   * returns.
   */
  private void toggleControls(boolean enabled) {
    // toggle text field
    txtColumnChoice.setEnabled(enabled);
    // toggle buttons
    for (int i = 0; i < columnChoiceButtons.getComponentCount(); i++) {
      columnChoiceButtons.getComponent(i).setEnabled(enabled);
    }
  }

  private void startGame() {
    dlgWhiteChoice.setVisible(true);
    dlgBlackChoice.setVisible(true);
    players.put(Colour.White, dlgWhiteChoice.getPlayerType());
    players.put(Colour.Black, dlgBlackChoice.getPlayerType());
    for (Colour colour : players.keySet()) {
      switch (players.get(colour)) {
        // human: AI is null
        case Human: playerAI.put(colour, null); break;

        // remote player: not implemented so cancel game
        case Network:
          JOptionPane.showMessageDialog(this, "Sorry, but network play is not functional yet.", "Missing functionality", JOptionPane.ERROR_MESSAGE);
          active = false;
          return;

        // AI
        case ComputerBrainless: playerAI.put(colour, new NoAI(grid, colour)); break;
        case ComputerEasy: playerAI.put(colour, new EasyAI(grid, colour)); break;
        case ComputerMedium: playerAI.put(colour, new MediumAI(grid, colour)); break;
        case ComputerHard: playerAI.put(colour, new MaxAI(grid, colour)); break;
      }
    }

    // clear grid and begin
    grid.zap();
    updateView();
    active = true;

    // randomly determine starter
    double randomNumber;
    do {
      randomNumber = Math.random();
    } while (randomNumber == 0.5);  // equalise odds by preventing 0.5

    // a number less than 0.5 means white starts; greater means black
    if (randomNumber < 0.5) currentPlayer = Colour.White;
    else currentPlayer = Colour.Black;

    statusMessage(colourString(currentPlayer)+"'s turn");

    // check for human/computer status of starting colour
    switch (players.get(currentPlayer)) {
      // human player: enable controls
      case Human: toggleControls(true); break;

      // AI: take a turn
      default: computerTurn();
    }
  }

  private void flipTurn() {
    // change colour
    currentPlayer = (currentPlayer == Colour.White)? Colour.Black : Colour.White;

    // determine whether next player is human or AI
    if (players.get(currentPlayer) == Player.Human) toggleControls(true);
    else computerTurn();
  }

  /**
   * Adds a piece, of the current player's colour, to the grid, then either
   * ends the game, or passes control to the other player.
   * @return Whether the piece was added successfully.
   */
  private boolean addPiece(int column) {
    // remember the row to which a piece will be added
    int row = grid.getNextRow(column);

    // attempt to add a piece
    if (grid.addPiece(currentPlayer, column)) {
      //~ transmitMove(currentPlayer, column);
      updateView();

      // check for Connect Four
      if (grid.connectsFour(row, column)) {
        win(currentPlayer);
      } else {
        // test for grid being full
        for (int i = 0; i < grid.getColumns(); i++) {
          // any empty column will do
          if (grid.getNextRow(i) != -1) {
            flipTurn();
            return true;
          }
        }

        // no empty columns; grid is full so game is drawn
        win(null);
      }

      // piece has been added
      return true;
    } else return false;
  }

  private boolean checkColumnChoice() {
    int column;

    // attempt to read column entered by user
    try {
      column = Integer.parseInt(txtColumnChoice.getText()) - 1;
    } catch (NumberFormatException nfe) {
      JOptionPane.showMessageDialog(this, "You must enter a number.", "Non-numeric column entered", JOptionPane.ERROR_MESSAGE);
      return false;
    }

    // check whether column exists
    if (column < 0 || column >= grid.getColumns()) {
      JOptionPane.showMessageDialog(this, "Column "+(column+1)+" does not exist.", "No such column", JOptionPane.ERROR_MESSAGE);
      return false;
    }

    // check whether column is full
    if (grid.getNextRow(column) == -1) {
      JOptionPane.showMessageDialog(this, "Column "+(column+1)+" is full.", "Cannot add to full column", JOptionPane.ERROR_MESSAGE);
      return false;
    }

    // no errors found
    return true;
  }

  //~ private void transmitMove(Colour player, int column) {

  //~ }

  //~ private void networkTurn() {

  //~ }

  /* AI */

  /**
   * Makes the computer take a turn, disabling the user's controls until it is
   * finished.
   *
   *<p>Artificial intelligence - try each of the following, in order, until a
   * move is chosen:
   *<ul>
   *  <li>Check whether a column would allow the AI to win.</li>
   *  <li>Check whether a column would block an opposing win.</li>
   *  <li>Check whether a column would give the AI an n-way or consequential
   * threat.</li>
   *  <li>Check whether a column would block an opposing n-way/consequential
   * threat.</li>
   *  <li>Detect columns that, if used by the AI, would allow an opposing win,
   * or an opposing n-way/consequential threat, and avoid these columns if
   * alternatives exist.</li>
   *  <li>Pick a random column.</li>
   *</ul>
   */
  private void computerTurn() {
    // disable user controls until computer is finished
    toggleControls(false);

    int column;
    do {
      column = playerAI.get(currentPlayer).chooseColumn();
/*    ConnectFourGrid alternateGrid;  // a cloned grid for testing moves

    // choose a column
    ColumnChoice: {
      // check for win
      int[] wins = getThreats(grid, currentPlayer);
      if (wins.length > 0) {
        column = wins[(int) (Math.random() * wins.length)];
        break ColumnChoice;
      }

      // check for blocking win
      int[] blocks = getThreats(grid, currentPlayer.opposite());
      if (blocks.length > 0) {
        column = blocks[(int) (Math.random() * blocks.length)];
        break ColumnChoice;
      }

      // check for constructing n-way or consequential win
      column = findNWay(grid, currentPlayer);
      if (column != -1) break ColumnChoice;

      // check for blocking n-way or consequential win
      column = findNWay(grid, currentPlayer.opposite());
      if (column != -1) break ColumnChoice;

      // check for 'bad' columns that would allow a user win
      boolean[] badColumns = new boolean[grid.getColumns()];
      boolean goodPlayExists = false;
      for (int i = 0; i < grid.getColumns(); i++) {
        alternateGrid = (ConnectFourGrid) grid.clone();

        // add a piece and check for threats
        alternateGrid.addPiece(currentPlayer, i);

        // all threatened columns are bad
        int[] threats = getThreats(alternateGrid, currentPlayer.opposite());
        if (threats.length > 0) {
          debug(i+" would allow win");
          badColumns[i] = true;
          continue;
        }

        // all columns allowing n-way or consequential are bad
        int badColumn = findNWay(alternateGrid, currentPlayer.opposite());
        if (badColumn != -1) {
          debug("allowed by "+i);
          badColumns[i] = true;
          continue;
        }
      }

      // check whether good move exists
      for (int i = 0; i < grid.getColumns(); i++) {
        if (grid.getNextRow(i) != -1 && !badColumns[i]) {
          goodPlayExists = true;
          break;
        }
      }

      // avoid bad columns if good play exists
      do {
        column = pickColumn();
      } while (goodPlayExists && badColumns[column] == true);
    }*/

    // add a piece to the chosen column
    } while (!addPiece(column));

    // status message if the game has not finished
    if (active) statusMessage("AI played in column "+(column+1));
  }

  /**/

  /** Makes <code>colour</code> win the game. */
  private void win(Colour colour) {
    // disable controls
    toggleControls(false);
    active = false;
    history.put(colour, history.get(colour) + 1);
    dlgHistory.updateHistory();

    // inform the user of the outcome
    statusMessage("Game over.");
    JOptionPane.showMessageDialog(this, colourString(colour)+" wins!");

    if (colour == null) {
      JOptionPane.showMessageDialog(this, "A draw! How boring.");
    } else if (players.get(colour) == Player.Human) {
      JOptionPane.showMessageDialog(this, "Congratulations!");
    } else if (players.get(colour) == null) {
      JOptionPane.showMessageDialog(this, "Hang on - who's that?");
    }
  }

  /* Display */

  /**
   * @return A String representing <code>colour</code>, eg "Black", or
   * "No-one" for null.
   */
  private static String colourString(Colour colour) {
    if (colour == null) return "No-one";
    else return colour.toString();
  }

  private void statusMessage(String message) {
    lblStatusMessage.setText(message);
  }

  /**
   * Constructs and displays 1 * ConnectFourGUI.
   * @param args The command-line arguments are not used.
   */
  public static void main(String[] args) throws IOException {
    System.setErr(new java.io.PrintStream(new java.io.FileOutputStream("error.txt"), true, StandardCharsets.UTF_8.name()));
    for (String s : args) {
      if (s.equalsIgnoreCase("debug")) {
        AbstractAI.debug = true;
        break;
      }
    }
    ConnectFourGUI tpo = new ConnectFourGUI();
    tpo.pack();
    tpo.setVisible(true);
  }

  /** The potential types of player that may be involved. */
  private enum Player { Human, Network, ComputerBrainless, ComputerEasy, ComputerMedium, ComputerHard; }

  /* Member classes */

  /** Graphically represents a square in a Connect Four grid. */
  private class GridLabel extends JLabel {

    private int x, y;

    /**
     * Constructs a new GridLabel representing the square at the specified
     * coordinates in the Connect Four grid.
     */
    public GridLabel(int x, int y) {
      this.x = x;
      this.y = y;
    }

    public void paint(Graphics g) {
      // draw border around cell
      g.setColor(Color.BLACK);
      g.drawRect(0, 0, CELL_SIZE-1, CELL_SIZE-1);

      // determine colour of this square, or leave blank
      Colour colour = grid.colourAt(x, y);
      if (colour == null) return;

      // draw the piece if present
      g.setColor(colour.getColor());
      g.fillOval(1, 1, CELL_SIZE-3, CELL_SIZE-3);

      // draw border around piece
      g.setColor(Color.BLACK);
      g.drawOval(1, 1, CELL_SIZE-3, CELL_SIZE-3);
    }

  }

  /** A button for choosing a column to play in. */
  private class ColumnChoiceButton extends JButton {

    private int columnIndex;

    public ColumnChoiceButton(int columnIndex) {
      super("Go");
      this.columnIndex = columnIndex;

      addActionListener(new ColumnChoiceListener());
    }

    private class ColumnChoiceListener implements ActionListener {
      public void actionPerformed(ActionEvent e) {
        if (!active) return;  // shouldn't happen but be on safe side
        addPiece(columnIndex);
      }
    }

  }

  /** A dialog for displaying the session history. */
  private class GameHistoryDialog extends JDialog {

    private Box contents;

    private ColourHistoryPanel whitePanel;
    private ColourHistoryPanel blackPanel;
    private ColourHistoryPanel drawPanel;

    private JPanel exitPanel;
      private JButton btnExit;

    public GameHistoryDialog() {
      super(ConnectFourGUI.this, "Session History");

      contents = Box.createVerticalBox();

      // white
      whitePanel = new ColourHistoryPanel(Colour.White);

      // black
      blackPanel = new ColourHistoryPanel(Colour.Black);

      // draw
      drawPanel = new ColourHistoryPanel(null);

      // exit
      exitPanel = new JPanel();

      btnExit = new JButton("Done");
      btnExit.addActionListener(new ActionListener() {
          public void actionPerformed(ActionEvent e) { GameHistoryDialog.this.setVisible(false); }
        }
      );
      btnExit.addKeyListener(new EnterListener());

      exitPanel.add(btnExit);

      // add panels to box
      contents.add(whitePanel);
      contents.add(blackPanel);
      contents.add(drawPanel);
      contents.add(exitPanel);

      // add box to container
      getContentPane().add(contents);

      // set dialog size to preferred
      pack();
    }

    private void updateHistory() {
      whitePanel.updateHistory();
      blackPanel.updateHistory();
      drawPanel.updateHistory();
    }

    private class ColourHistoryPanel extends JPanel {

      private Colour colour;

      private JLabel caption;
      private JLabel wins;

      public ColourHistoryPanel(Colour colour) {
        this.colour = colour;

        // create components
        caption = new JLabel(colourString(colour)+":");
        wins = new JLabel();

        // add components to panel
        add(caption);
        add(wins);

        updateHistory();
      }

      public void updateHistory() {
        //System.out.println(colourString(colour)+" panel updated to "+history.get(colour));
        wins.setText(String.valueOf(history.get(colour)));
        repaint();
      }
    }
  }

  /** A dialog for choosing whether each player is human or AI. */
  private class PlayerChoiceDialog extends JDialog {

    private Colour colour;

    private Box contents;

    private JLabel lblColour;
    private JRadioButton[] playerTypes;
    private ButtonGroup grpPlayerTypes;

    private JButton btnOK;

    public PlayerChoiceDialog(Colour colour) {
      super(ConnectFourGUI.this, "Choose the players", true);

      // create contents box
      contents = Box.createVerticalBox();

      // create title label
      lblColour = new JLabel(colourString(colour)+" will be ");

      class RadioListener extends EnterListener {
        public void keyPressed(KeyEvent e) {
          super.keyPressed(e);
          if (e.getKeyCode() == KeyEvent.VK_ENTER) btnOK.doClick();
        }
      }

      // create radio buttons
      playerTypes = new JRadioButton[Player.values().length];

      playerTypes[Player.Human.ordinal()] = new JRadioButton("Human", true);
      playerTypes[Player.Human.ordinal()].addKeyListener(new RadioListener());

      playerTypes[Player.ComputerBrainless.ordinal()] = new JRadioButton("AI - Clod", false);
      playerTypes[Player.ComputerBrainless.ordinal()].addKeyListener(new RadioListener());

      playerTypes[Player.ComputerEasy.ordinal()] = new JRadioButton("AI - Barbarian", false);
      playerTypes[Player.ComputerEasy.ordinal()].addKeyListener(new RadioListener());

      playerTypes[Player.ComputerMedium.ordinal()] = new JRadioButton("AI - Rogue", false);
      playerTypes[Player.ComputerMedium.ordinal()].addKeyListener(new RadioListener());

      playerTypes[Player.ComputerHard.ordinal()] = new JRadioButton("AI - Wizard", false);
      playerTypes[Player.ComputerHard.ordinal()].addKeyListener(new RadioListener());

      playerTypes[Player.Network.ordinal()] = new JRadioButton("Remote player", false);
      playerTypes[Player.Network.ordinal()].addKeyListener(new RadioListener());

      // put radio buttons in exclusion group
      grpPlayerTypes = new ButtonGroup();
      for (int i = 0; i < playerTypes.length; i++) {
        grpPlayerTypes.add(playerTypes[i]);
      }

      // create confirmation button
      btnOK = new JButton("OK");
      btnOK.addActionListener(new ActionListener() {
          public void actionPerformed(ActionEvent e) { PlayerChoiceDialog.this.setVisible(false); }
        }
      );
      btnOK.addKeyListener(new EnterListener());

      // add components to contents box
      contents.add(lblColour);
      for (int i = 0; i < playerTypes.length; i++) {
        contents.add(playerTypes[i]);
      }
      contents.add(btnOK);

      // add contents box to container
      getContentPane().add(contents);

      pack();
    }

    public Player getPlayerType() {
      for (int i = 0; i < playerTypes.length; i++) {
        if (playerTypes[i].isSelected()) return Player.values()[i];
      }
      throw new IllegalStateException("No player type selected");
    }

  }

  /**
   * A KeyListener that responds to enter by pressing buttons or transferring
   * the focus.
   */
  private class EnterListener extends KeyAdapter {
    public void keyPressed(KeyEvent e) {
      if (e.getKeyCode() == KeyEvent.VK_ENTER) {
        if (e.getSource() instanceof AbstractButton) {
          ((AbstractButton) e.getSource()).doClick();
        } else {
          ((JComponent) e.getSource()).transferFocus();
        }
      }
    }
  }
}
