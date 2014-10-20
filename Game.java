import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;

public class Game extends JFrame {
    private Image[][] images = null; // all card pictures
    Image cardBackSide; // to display before game started
    ArrayList<Card> myDeck = new ArrayList<Card>();
    ArrayList<Card> cpuDeck = new ArrayList<Card>();
    ArrayList<Card> stack = new ArrayList<Card>();

    Random random = new Random(); // random number generator

    // hold the uncovered cards' images
    ImageIcon myCardPic = new ImageIcon();
    ImageIcon cpuCardPic = new ImageIcon();
    // hold visual info texts
    JLabel myCardCount = new JLabel("");
    JLabel cpuCardCount = new JLabel("");
    JLabel stackCount = new JLabel();
    JLabel statusString = new JLabel("Welcome");

    String allSuits = "CSHD"; // contains 4 chars for the names of the suits

    // constructor, load images and create the GUI
    public Game() {
        super("War of Cards"); // create a JFrame and set the title
        try { // ImageIO.read() demands you to handle its possible errors
            cardBackSide = ImageIO.read(new File("classic-cards/b1fv.png"));
        } catch (IOException e) {
            System.out.println("Error while loading image file ");
        }

        // create the UI elements
        // the ui will contain three rows with the following elements:
        // row 1   player's card pic on the left, cpu's card pic on the right
        // row 2   player's deck size on the left, size of the stack in the middle, cpu's deck size on the right
        // row 2   status message on the left, "next card" button on the right
        JPanel row1 = new JPanel(new BorderLayout());
        row1.add(new JLabel(myCardPic), BorderLayout.LINE_START);
        row1.add(new JLabel(cpuCardPic), BorderLayout.LINE_END);

        JPanel row2 = new JPanel(new BorderLayout());
        row2.add(myCardCount, BorderLayout.LINE_START);
        row2.add(stackCount, BorderLayout.CENTER);
        row2.add(cpuCardCount, BorderLayout.LINE_END);

        JPanel row3 = new JPanel(new BorderLayout());
        row3.add(statusString, BorderLayout.LINE_START);
        JButton nextButton = new JButton("Next card");
        row3.add(nextButton, BorderLayout.LINE_END);

        // now combine the rows into one container
        JPanel container = new JPanel(new BorderLayout());
        container.add(row1, BorderLayout.BEFORE_FIRST_LINE);
        container.add(row2);
        container.add(row3, BorderLayout.AFTER_LAST_LINE);

        // add the container to the main window
        this.add(container);
        this.setSize(220,200);

        // make the "Next card" button call its method if clicked
        nextButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                playNextCard();
            }
        });

        loadCardPictures();
        initNewGame();
    }

    // card pictures must be loaded only once
    void loadCardPictures() {
        /**
         * images are in order 1 = ace of cross, 2 = ace of spaces, 3 = ace of hearts, 4 = ace of diamonds,
         * 5 = king of cross, ... 51 = two of hearts, 52 = two of diamonds, so we need to loop through them in the order
         * - for each rank from ace(14) down to 2
         *   load picture for cross, spades, hearts, diamonds
         */
        images = new Image[4][15];
        int curCard = 1;
        for (int rank = 14; rank >= 2; rank--) {
            for (int suit = 0; suit < 4; suit++) {
                try {
                    File imageFile = new File("classic-cards/" + curCard + ".png");
                    images[suit][rank] = ImageIO.read(imageFile);
                } catch (Exception e) {
                    System.out.println("Error while loading image file ");
                }
                curCard++;
            }
        }
    }

    // create and return a new, sorted deck of cards
    ArrayList<Card> newDeck() {
        ArrayList<Card> deck = new ArrayList<Card>();
        for (int suitNum = 0; suitNum < 4; suitNum++) {
            for (int rank = 2; rank <= 14; rank++) {
                char suitName = allSuits.charAt(suitNum); // the char for suite #0 is the char for "cross" - see the allSuits string
                deck.add(new Card(suitName, rank));
            }
        }
        return deck;
    }

    // simulate drawing from a shuffled deck by drawing from random positions of a sorted deck
    void dealDeckToAandB(ArrayList<Card> deck, ArrayList<Card> a, ArrayList<Card> b) {
        int cardsPerPlayer = deck.size() / 2;
        Card card;
        for (int i = 0; i < cardsPerPlayer; i++) {
            card = deck.remove(random.nextInt(deck.size())); // draw random card from the deck
            a.add(card); // give it to player
            card = deck.remove(random.nextInt(deck.size())); // draw another random card
            b.add(card); // give it to the cpu
        }
    }

    // set parameters for a new game
    void initNewGame() {
        myDeck.clear();
        cpuDeck.clear();
        dealDeckToAandB(newDeck(), myDeck, cpuDeck);
        updateInfo(null, null); // pass null, so only card backsides get displayed
    }

    // uncover the next two cards, compare their ranks and act accordingly
    void playNextCard() {
        Card myCard = myDeck.remove(0); // draw the first card
        Card cpuCard = cpuDeck.remove(0);
        // put them on the stack. when there's war, the next cards go onto the stack, too, when there's a winner
        // the stack gets cleared
        stack.add(myCard);
        stack.add(cpuCard);
        // compare results, winner gets all cards on stack
        int result = myCard.compareTo(cpuCard);
        if (result < 0) { // player rank is lower
            displayMessage("My card");
            cpuDeck.addAll(stack);
            stack.clear(); // don't forget to remove copies of our virtual stacks
        } else if (result > 0) { // player rank is higher
            displayMessage("Your card");
            myDeck.addAll(stack);
            stack.clear();
        } else { // draw -> a war starts
            // players with at least one card left add them to the stack
            if (!myDeck.isEmpty()) {
                stack.add(myDeck.remove(0));
            }
            if (!cpuDeck.isEmpty()) {
                stack.add(myDeck.remove(0));
            }
            displayMessage("Let There Be War");
        }
        updateInfo(myCard, cpuCard); // the updateInfo method also checks if there's a winner
    }

    // display a game status text, convenience method that saves typing the same stuff over and over
    void displayMessage(String msg) {
        statusString.setText(msg);
    }

    // update visuals, check for a winner
    void updateInfo(Card myCard, Card cpuCard) {
        // decide which pictures to show
        if (myCard == null || cpuCard == null) {
            myCardPic.setImage(cardBackSide);
            cpuCardPic.setImage(cardBackSide);
        } else {
            // find the picture for a card with <suit> and <rank> in the images array
            int suitCode = allSuits.indexOf(myCard.getSuit());
            int rankNum = myCard.getRank();
            myCardPic.setImage(images[suitCode][rankNum]);
            // now find the cpu's card the same way
            suitCode = allSuits.indexOf(cpuCard.getSuit());
            rankNum = cpuCard.getRank();
            cpuCardPic.setImage(images[suitCode][rankNum]);
        }
        // display card counts
        myCardCount.setText("you: "+myDeck.size());
        cpuCardCount.setText("cpu: "+cpuDeck.size());
        if (stack.isEmpty()) {
            stackCount.setText("              -"); // spaces needed because "CENTER" doesn't do what it sounds like
        } else {
            stackCount.setText("              "+stack.size());
        }

        repaint(); // draw the newly uncovered cards
        if (myDeck.isEmpty() || cpuDeck.isEmpty()) { // if anyone ran out of cards
            winningMessage();
        }
    }

    // tell who is the winner and ask if a replay is wanted
    void winningMessage() {
        String title = "";
        if (myDeck.isEmpty() && cpuDeck.isEmpty()) {
            title = "A draw. No winner.";
        } else if (myDeck.isEmpty()) {
            title = "Awww, you lose.";
        } else {
            title = "Hurray, you win!";
        }

        // ask for a replay
        String[] possibleAnswers = {"Yes", "No"};
        final int YES = 0;
        int answer = JOptionPane.showOptionDialog(this, title, "Do you want to play again?", 0, 0, null, possibleAnswers, possibleAnswers[1]);
        if (answer == YES) {
            initNewGame();
        } else {
            System.exit(0);
        }
    }

    public static void main(String[] args) {
        Game game = new Game();
        game.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        game.setVisible(true);
    }
}