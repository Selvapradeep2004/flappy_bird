import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.Random;
import javax.swing.*;

public class FlappyBird extends JPanel implements ActionListener, KeyListener {
    int boardWidth = 360, boardHeight = 640;
    
    Image backgroundImg, birdImg, topPipeImg, bottomPipeImg;
    int birdX = boardWidth / 8, birdY = boardHeight / 2, birdWidth = 34, birdHeight = 24;
    
    class Bird {
        int x = birdX, y = birdY, width = birdWidth, height = birdHeight;
        Image img;
        Bird(Image img) { this.img = img; }
    }
    
    int pipeX = boardWidth, pipeY = 0, pipeWidth = 64, pipeHeight = 512;
    class Pipe {
        int x, y, width = pipeWidth, height = pipeHeight;
        Image img; boolean passed = false;
        Pipe(Image img, int x, int y) { this.img = img; this.x = x; this.y = y; }
    }

    Bird bird;
    int velocityY = 0, velocityX = -4, gravity = 1;
    ArrayList<Pipe> pipes = new ArrayList<>();
    Random random = new Random();

    Timer gameLoop, placePipesTimer;
    boolean gameOver = false, gameStarted = false;
    double score = 0;

    JButton startButton, restartButton;

    public FlappyBird() {
        setPreferredSize(new Dimension(boardWidth, boardHeight));
        setLayout(null);
        setFocusable(true);
        addKeyListener(this);

        // Load images
        backgroundImg = new ImageIcon(getClass().getResource("/flappybirdbg.png")).getImage();
        birdImg = new ImageIcon(getClass().getResource("/flappybird.png")).getImage();
        topPipeImg = new ImageIcon(getClass().getResource("/toppipe.png")).getImage();
        bottomPipeImg = new ImageIcon(getClass().getResource("/bottompipe.png")).getImage();

        bird = new Bird(birdImg);

        // Game loop
        gameLoop = new Timer(1000 / 60, this);
        placePipesTimer = new Timer(1500, e -> placePipes());

        // Start button
        startButton = createButton("Start", boardWidth / 2 - 60, boardHeight / 2 - 25);
        startButton.addActionListener(e -> startGame());
        add(startButton);

        // Restart button
        restartButton = createButton("Restart", boardWidth / 2 - 60, boardHeight / 2 + 30);
        restartButton.setVisible(false);
        restartButton.addActionListener(e -> startGame());
        add(restartButton);
    }

    private JButton createButton(String text, int x, int y) {
        JButton button = new JButton(text);
        button.setBounds(x, y, 120, 50);
        button.setBackground(new Color(50, 205, 50)); // Green color
        button.setForeground(Color.WHITE); // White text
        button.setFont(new Font("Arial", Font.BOLD, 18));
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createLineBorder(Color.BLACK, 2)); // Black border
        button.setOpaque(true);
        return button;
    }

    public void startGame() {
        gameStarted = true;
        gameOver = false;
        bird.y = boardHeight / 2;
        velocityY = 0;
        score = 0;
        pipes.clear();
        gameLoop.start();
        placePipesTimer.start();
        startButton.setVisible(false);
        restartButton.setVisible(false);
        requestFocus();
    }

    public void placePipes() {
        int randomPipeY = pipeY - pipeHeight / 4 - random.nextInt(pipeHeight / 2);
        int openingSpace = boardHeight / 4;
        pipes.add(new Pipe(topPipeImg, boardWidth, randomPipeY));
        pipes.add(new Pipe(bottomPipeImg, boardWidth, randomPipeY + pipeHeight + openingSpace));
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        draw(g);
    }

    public void draw(Graphics g) {
        g.drawImage(backgroundImg, 0, 0, boardWidth, boardHeight, null);
        g.drawImage(bird.img, bird.x, bird.y, bird.width, bird.height, null);

        for (Pipe pipe : pipes) {
            g.drawImage(pipe.img, pipe.x, pipe.y, pipe.width, pipe.height, null);
        }

        g.setColor(Color.white);
        g.setFont(new Font("Arial", Font.PLAIN, 32));

        if (!gameStarted) {
            g.drawString("Press Start", boardWidth / 2 - 70, boardHeight / 2 - 50);
        } else if (gameOver) {
            g.drawString("Game Over: " + (int) score, 10, 35);
            restartButton.setVisible(true);
        } else {
            g.drawString(String.valueOf((int) score), 10, 35);
        }
    }

    public void move() {
        if (!gameStarted || gameOver) return;

        velocityY += gravity;
        bird.y += velocityY;
        bird.y = Math.max(bird.y, 0);

        if (bird.y > boardHeight) {
            gameOver = true;
        }

        for (Pipe pipe : pipes) {
            pipe.x += velocityX;
            if (collision(bird, pipe)) {
                gameOver = true;
                break;
            }
        }

        for (Pipe pipe : pipes) {
            if (!pipe.passed && bird.x > pipe.x + pipe.width) {
                pipe.passed = true;
                score += 0.5;
            }
        }

        if (gameOver) {
            gameLoop.stop();
            placePipesTimer.stop();
            restartButton.setVisible(true);
        }
    }

    public boolean collision(Bird a, Pipe b) {
        return a.x < b.x + b.width &&
               a.x + a.width > b.x &&
               a.y < b.y + b.height &&
               a.y + a.height > b.y;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        move();
        repaint();
    }

    @Override
    public void keyPressed(KeyEvent e) {
        if (gameStarted && !gameOver && e.getKeyCode() == KeyEvent.VK_SPACE) {
            velocityY = -9;
        }
    }

    @Override
    public void keyTyped(KeyEvent e) {}

    @Override
    public void keyReleased(KeyEvent e) {}
}
