// FourRoad.java
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.Random;
import javax.swing.*;
import javax.sound.sampled.*;  // Add this for wav
import java.io.File;
import javax.sound.sampled.*;  // Clip, for AudioSystem 
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;

public class FourRoad extends JFrame {
    private boolean gameStarted = false;
    private RoadPanel roadPanel;

    public FourRoad() {
        setTitle("Traffic light simulator - Manual Lights, Queue System");
        setSize(800, 800);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        setContentPane(new StartPanel());

        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (!gameStarted && e.getKeyCode() == KeyEvent.VK_SPACE) {
                    startGame();
                } else if (gameStarted && roadPanel.gameOver && e.getKeyCode() == KeyEvent.VK_SPACE) {
                    restartGame();
                }
            }
        });
    }

    private void startGame() {
        gameStarted = true;
        roadPanel = new RoadPanel();
        setContentPane(roadPanel);
        revalidate();
    }

    private void restartGame() {
        roadPanel.anim.stop();
        roadPanel.spawner.stop();
        startGame();
    }

    class StartPanel extends JPanel {
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            g.setColor(Color.BLACK);
            g.fillRect(0, 0, getWidth(), getHeight());
            g.setColor(Color.WHITE);
            g.setFont(new Font("Arial", Font.BOLD, 36));
            g.drawString("4 Road Traffic Simulator", 180, 300);
            g.setFont(new Font("Arial", Font.PLAIN, 24));
            g.drawString("Press SPACE to Start", 270, 400);
        }
    }

    class RoadPanel extends JPanel implements ActionListener {
        private final java.util.List<Car> cars = new ArrayList<>();
        private final Random rand = new Random();
        private final int[] lights = {0, 0, 0, 0};
        private final javax.swing.Timer anim;
        private final javax.swing.Timer spawner;
        boolean gameOver = false;

        // PNG of bush and House
        private BufferedImage bushImg, houseImg;

        public RoadPanel() {
            setBackground(new Color(144, 238, 144)); // light green grass
            anim = new javax.swing.Timer(30, this);
            anim.start();
            spawner = new javax.swing.Timer(1070, e -> spawnCar());
            spawner.start();
            setupKeyBindings();

            // Load Bush and House PNG images
            try {
                bushImg = ImageIO.read(getClass().getResource("/image/bush.png"));
                houseImg = ImageIO.read(getClass().getResource("/image/house.png"));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        private void setupKeyBindings() {
            int cond = JComponent.WHEN_IN_FOCUSED_WINDOW;
            getInputMap(cond).put(KeyStroke.getKeyStroke('1'), "L_TOP");
            getActionMap().put("L_TOP", new AbstractAction() {
                @Override public void actionPerformed(ActionEvent e) { cycle(0); }
            });
            getInputMap(cond).put(KeyStroke.getKeyStroke('2'), "L_RIGHT");
            getActionMap().put("L_RIGHT", new AbstractAction() {
                @Override public void actionPerformed(ActionEvent e) { cycle(1); }
            });
            getInputMap(cond).put(KeyStroke.getKeyStroke('3'), "L_BOTTOM");
            getActionMap().put("L_BOTTOM", new AbstractAction() {
                @Override public void actionPerformed(ActionEvent e) { cycle(2); }
            });
            getInputMap(cond).put(KeyStroke.getKeyStroke('4'), "L_LEFT");
            getActionMap().put("L_LEFT", new AbstractAction() {
                @Override public void actionPerformed(ActionEvent e) { cycle(3); }
            });
        }

        private void cycle(int i) {
            lights[i] = (lights[i] + 1) % 3;
            repaint();
        }

        private void spawnCar() {
            int side = rand.nextInt(4);
            int lane = 0;
            cars.add(new Car(side, lane, getWidth(), getHeight()));

            // Random horn chance when car spawns
            if(rand.nextInt(3)==0){ // 20% chance
                playHorn();
            }
        }

        // Random horn chance when car spawns
        private void playHorn() {
            try {
                // List of horns (inside src/image/)
                String[] horns = {
                        "/image/horn1.wav",
                        "/image/horn2.wav",
                        "/image/horn3.wav",
                        "/image/horn4.wav"
                };

                // Randomly pick one
                Random rand = new Random();
                String chosenHorn = horns[rand.nextInt(horns.length)];

                // Load file from resources (classpath)
                java.net.URL soundURL = getClass().getResource(chosenHorn);
                if (soundURL == null) {
                    System.out.println("Horn file not found: " + chosenHorn);
                    return;
                }

                AudioInputStream audio = AudioSystem.getAudioInputStream(soundURL);
                Clip clip = AudioSystem.getClip();
                clip.open(audio);
                clip.start();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        // Add the crash sound functionality
        private void playCrashSound() {
            try {
                // List of crash sounds (you can add more crash sounds here)
                String[] crashSounds = {
                        "/image/crash1.wav",
                        "/image/crash2.wav",
                        "/image/crash3.wav",
                        "/image/crash4.wav"
                };

                // Randomly pick one
                Random rand = new Random();
                String chosenCrashSound = crashSounds[rand.nextInt(crashSounds.length)];

                // Load file from resources (classpath)
                java.net.URL soundURL = getClass().getResource(chosenCrashSound);
                if (soundURL == null) {
                    System.out.println("Crash sound file not found: " + chosenCrashSound);
                    return;
                }

                AudioInputStream audio = AudioSystem.getAudioInputStream(soundURL);
                Clip clip = AudioSystem.getClip();
                clip.open(audio);
                clip.start();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            int w = getWidth(), h = getHeight();
            int interLeft = w / 3, interRight = 2 * w / 3;
            int interTop = h / 3, interBottom = 2 * h / 3;

            // ===== Border greenery: ovals -> bush.png (fallback to oval) =====
            int bushSize = 30; // thoda bada bush so it covers the previous circle nicely
            for (int i = 0; i < w; i += 40) {
                if (bushImg != null) {
                    g.drawImage(bushImg, i, 0, bushSize, bushSize, null);
                    g.drawImage(bushImg, i, h - bushSize, bushSize, bushSize, null);
                } else {
                    g.setColor(new Color(0, 100, 0));
                    g.fillOval(i, 0, 30, 30);
                    g.fillOval(i, h - 30, 30, 30);
                }
            }
            for (int j = 0; j < h; j += 40) {
                if (bushImg != null) {
                    g.drawImage(bushImg, 0, j, bushSize, bushSize, null);
                    g.drawImage(bushImg, w - bushSize, j, bushSize, bushSize, null);
                } else {
                    g.setColor(new Color(0, 100, 0));
                    g.fillOval(0, j, 30, 30);
                    g.fillOval(w - 30, j, 30, 30);
                }
            }
            // ===== Roads and markings (unchanged) =====
            g.setColor(Color.GRAY);
            g.fillRect(w / 3, 0, w / 3, h);
            g.fillRect(0, h / 3, w, h / 3);

            g.setColor(Color.WHITE);
            g.fillRect(interLeft - 4, 0, 4, h);
            g.fillRect(interRight, 0, 4, h);
            g.fillRect(0, interTop - 4, w, 4);
            g.fillRect(0, interBottom, w, 4);

            g.setColor(Color.WHITE);
            for (int y = 0; y < h; y += 40) g.fillRect(w / 2 - 2, y, 4, 20);
            for (int x = 0; x < w; x += 40) g.fillRect(x, h / 2 - 2, 20, 4);

            int stripe = 8, gap = 8;
            g.setColor(Color.WHITE);
            for (int x = interLeft; x <= interRight - stripe; x += stripe + gap)
                g.fillRect(x, interTop - 35, stripe, 30);
            for (int x = interLeft; x <= interRight - stripe; x += stripe + gap)
                g.fillRect(x, interBottom + 5, stripe, 35);
            for (int y = interTop; y <= interBottom - stripe; y += stripe + gap)
                g.fillRect(interLeft - 35, y, 30, stripe);
            for (int y = interTop; y <= interBottom - stripe; y += stripe + gap)
                g.fillRect(interRight + 7, y, 29, stripe);

            drawSignal(g, interRight + 15, interTop - 135, lights[0]);
            drawSignal(g, interRight + 15, interBottom + 15, lights[1]);
            drawSignal(g, interLeft - 80, interBottom + 15, lights[2]);
            drawSignal(g, interLeft - 80, interTop - 135, lights[3]);

            // ===== Houses (bigger, image) in each corner block =====
            int houseSize = 60; // pehle 45 tha, ab thoda bada
            drawHouses(g, 50, 50, houseSize);
            drawHouses(g, w - (houseSize * 2 + 15) - 50, 50, houseSize);
            drawHouses(g, 50, h - (houseSize * 2 + 15) - 50, houseSize);
            drawHouses(g, w - (houseSize * 2 + 15) - 50, h - (houseSize * 2 + 15) - 50, houseSize);

            for (Car c : cars) c.draw(g);

            if (gameOver) {
                g.setColor(Color.RED);
                g.setFont(new Font("Arial", Font.BOLD, 60));
                g.drawString("GAME OVER", w / 2 - 180, h / 2);
                g.setFont(new Font("Arial", Font.PLAIN, 24));
                g.drawString("Press SPACE to Restart", w / 2 - 150, h / 2 + 50);
            }
        }

        private void drawHouses(Graphics g, int startX, int startY, int size) {
            int pad = 15;
            for (int i = 0; i < 4; i++) {
                int x = startX + (i % 2) * (size + pad);
                int y = startY + (i / 2) * (size + pad);

                // House (prefer image; fallback to old rectangles)
                if (houseImg != null) {
                    g.drawImage(houseImg, x, y, size, size, null);
                } else {
                    g.setColor(new Color(200, 180, 150));
                    g.fillRect(x, y, size, size);
                    g.setColor(new Color(139, 69, 19));
                    g.fillRect(x + 5, y + 5, size - 10, size - 10);
                }

                // Add bushes near each house (looks like garden)
                if (bushImg != null) {
                    int b = Math.max(18, size / 3);
                    g.drawImage(bushImg, x - 8, y + size + 4, b, b, null);
                    g.drawImage(bushImg, x + size - b + 8, y + size + 4, b, b, null);
                }
            }
        }

        private void drawSignal(Graphics g, int x, int y, int state) {
            g.setColor(Color.BLACK);
            g.fillRoundRect(x, y, 50, 120, 12, 12);
            g.setColor(state == 0 ? Color.RED : Color.DARK_GRAY);
            g.fillOval(x + 15, y + 10, 20, 20);
            g.setColor(state == 1 ? Color.YELLOW : Color.DARK_GRAY);
            g.fillOval(x + 15, y + 45, 20, 20);
            g.setColor(state == 2 ? Color.GREEN : Color.DARK_GRAY);
            g.fillOval(x + 15, y + 80, 20, 20);
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            if (!gameOver) {
                for (Car c : cars) {
                    c.update(lights, getWidth(), getHeight(), cars);
                }

                for (int i = 0; i < cars.size(); i++) {
                    Car c1 = cars.get(i);
                    Rectangle r1 = new Rectangle(c1.x + 10, c1.y + 10, c1.wCar - 20, c1.hCar - 20);

                    for (int j = i + 1; j < cars.size(); j++) {
                        Car c2 = cars.get(j);
                        Rectangle r2 = new Rectangle(c2.x + 10, c2.y + 10, c2.wCar - 20, c2.hCar - 20);

                        if (r1.intersects(r2)) {
                            // Play crash sound when a collision happens
                            playCrashSound();

                            gameOver = true;
                            anim.stop();
                            spawner.stop();
                            break;
                        }
                    }
                    if (gameOver) break;
                }

                cars.removeIf(c -> c.offscreen(getWidth(), getHeight()));
                repaint();
            }
        }

        class Car {
            int x, y, dx, dy, side, lane;
            boolean turned = false;
            boolean isTurning = false;
            boolean moving = false;
            int wCar, hCar;
            final int speed = 5;

            static Image[] carImgs;
            Image myCarImg;

            static {
                try {
                    carImgs = new Image[]{
                            javax.imageio.ImageIO.read(Car.class.getResource("/image/car1.png")),
                            javax.imageio.ImageIO.read(Car.class.getResource("/image/car2.png")),
                            javax.imageio.ImageIO.read(Car.class.getResource("/image/car3.png")),
                            javax.imageio.ImageIO.read(Car.class.getResource("/image/car4.png"))
                    };

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            Car(int side, int lane, int W, int H) {
                this.side = side;
                this.lane = lane;
                this.myCarImg = carImgs[rand.nextInt(carImgs.length)];

                int laneOffset = 0;

                switch (side) {
                    case 0:
                        wCar = 70; hCar = 50;
                        x = W/2 + laneOffset; y = -hCar-5; dx=0; dy=speed;
                        break;
                    case 1:
                        wCar = 70; hCar = 50;
                        x = W+5; y = H/2 + laneOffset; dx=-speed; dy=0;
                        break;
                    case 2:
                        wCar = 70; hCar = 50;
                        x = W/2 + laneOffset; y = H+5; dx=0; dy=-speed;
                        break;
                    case 3:
                        wCar = 70; hCar = 50;
                        x = -wCar-5; y = H/2 + laneOffset; dx=speed; dy=0;
                        break;
                }
            }

            void update(int[] lights, int W, int H, java.util.List<Car> allCars) {
                int interLeft=W/3, interRight=2*W/3, interTop=H/3, interBottom=2*H/3;
                boolean mustStop=false;
                int moveSpeed=speed;

                if(!turned) {
                    if(side==0){int stopY=interTop-40; if(y+hCar>=stopY && y+hCar<=stopY+5){if(lights[0]==0) mustStop=true; else if(lights[0]==1) moveSpeed=1;}}
                    else if(side==2){int stopY=interBottom+40; if(y<=stopY && y>=stopY-5){if(lights[2]==0) mustStop=true; else if(lights[2]==1) moveSpeed=1;}}
                    else if(side==3){int stopX=interLeft-40; if(x+wCar>=stopX && x+wCar<=stopX+5){if(lights[3]==0) mustStop=true; else if(lights[3]==1) moveSpeed=1;}}
                    else if(side==1){int stopX=interRight+40; if(x<=stopX && x>=stopX-5){if(lights[1]==0) mustStop=true; else if(lights[1]==1) moveSpeed=1;}}

                    int safeGap=Math.max(wCar,hCar)+speed-40;
                    for(Car other: allCars){
                        if(other==this) continue;
                        if(other.side!=this.side) continue;
                        if(dy>0 && other.y>y && other.y-(y+hCar)<safeGap && !other.moving) mustStop=true;
                        if(dy<0 && other.y<y && (y-hCar)-other.y<safeGap && !other.moving) mustStop=true;
                        if(dx>0 && other.x>x && other.x-(x+wCar)<safeGap && !other.moving) mustStop=true;
                        if(dx<0 && other.x<x && (x-wCar)-other.x<safeGap && !other.moving) mustStop=true;
                    }
                }

                int centerX=W/2, centerY=H/2;
                if(!turned && Math.abs(x-centerX)<70 && Math.abs(y-centerY)<70){
                    int choice=rand.nextInt(3);
                    if(choice==1){
                        if(dx==0 && dy>0){dx=-moveSpeed; dy=0; wCar=70; hCar=40;}
                        else if(dx==0 && dy<0){dx=moveSpeed; dy=0; wCar=70; hCar=40;}
                        else if(dx>0 && dy==0){dx=0; dy=moveSpeed; wCar=40; hCar=70;}
                        else if(dx<0 && dy==0){dx=0; dy=-moveSpeed; wCar=40; hCar=70;}
                        turned=true;
                        isTurning=true;
                    } else if(choice==2){
                        if(dx==0 && dy>0){dx=moveSpeed; dy=0; wCar=70; hCar=40;}
                        else if(dx==0 && dy<0){dx=-moveSpeed; dy=0; wCar=70; hCar=40;}
                        else if(dx>0 && dy==0){dx=0; dy=-moveSpeed; wCar=40; hCar=70;}
                        else if(dx<0 && dy==0){dx=0; dy=moveSpeed; wCar=40; hCar=70;}
                        turned=true;
                        isTurning=true;
                    }
                }

                if(!mustStop){
                    moving = true;
                    x += (dx==0?0:(dx>0?moveSpeed:-moveSpeed));
                    y += (dy==0?0:(dy>0?moveSpeed:-moveSpeed));
                } else {
                    moving = false;
                }
            }

            boolean offscreen(int W, int H){
                return x<-wCar-40 || x>W+40 || y<-hCar-40 || y>H+40;
            }

            void draw(Graphics g){
                if (myCarImg != null) {
                    Graphics2D g2d = (Graphics2D) g.create();
                    double angle = 0;

                    if (dx == 0 && dy > 0) angle = Math.toRadians(180);
                    if (dx == 0 && dy < 0) angle = 0;
                    if (dx > 0 && dy == 0) angle = Math.toRadians(90);
                    if (dx < 0 && dy == 0) angle = Math.toRadians(270);

                    g2d.rotate(angle, x + wCar/2.0, y + hCar/2.0);
                    g2d.drawImage(myCarImg, x, y, wCar, hCar, null);
                    g2d.dispose();
                }
            }
        }
    }

    public static void main(String[] args){
        SwingUtilities.invokeLater(() -> new FourRoad().setVisible(true));
    }
}

// thanks