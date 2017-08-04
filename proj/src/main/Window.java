package main;


import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.awt.image.BufferedImage;
import java.net.URL;
import java.util.Date;
import java.util.Random;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

import main.enums.Biome;
import perlinNoise.NoiseGenerator;
import perlinNoise.NoiseInterpreter;

public class Window {
    private JFrame window;
    private String windowName;
    private Random rand;
    private int window_width;
    private int window_height;

    private float[][] ElevationNoise;
    private float[][] MoistureNoise;
    private float[][] TemperatureNoise;

    private BufferedImage ElevationMap;
    private BufferedImage MoistureMap;
    private BufferedImage TemperatureMap;

    private JPanel ElevationPanel;
    private JPanel MoisturePanel;
    private JPanel TemperaturePanel;

    private static JLabel BiomeLabel;
    private NoiseGenerator generator;

    public Window(String windowName, int width, int height){
        this.windowName = windowName;
        this.window_width = width;
        this.window_height = height;
        this.rand = new Random(new Date().getTime());
        generator = new NoiseGenerator(rand.nextLong(), width / 3, height - (height / 4));
    }

    public void init(){
        window = new JFrame(windowName);
        window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        window.setSize(1200, 500);
        BiomeLabel = new JLabel("BIOME: ");
        BiomeLabel.setPreferredSize(new Dimension(150, 30));

        Dimension screenSize = java.awt.Toolkit.getDefaultToolkit().getScreenSize();
        window.setBounds(
                screenSize.width / 2 - (window_width / 2),
                screenSize.height / 2 - (window_height / 2),
                window_width, window_height);

        ElevationNoise = generator.generatePerlinNoise(generator.getSettings().getOctaves());
        ElevationMap = perlinNoise.Image
                        .RenderImage(NoiseInterpreter
                                .GetGradientMap(ElevationNoise, Color.white, Color.black));

        generator.setSeed(rand.nextLong());
        MoistureNoise = generator.generatePerlinNoise(generator.getSettings().getOctaves());
        MoistureMap = perlinNoise.Image
                        .RenderImage(NoiseInterpreter
                                .GetGradientMap(MoistureNoise, Color.blue, Color.black));

        generator.setSeed(rand.nextLong());
        TemperatureNoise = generator.generatePerlinNoise(generator.getSettings().getOctaves());
        TemperatureMap = perlinNoise.Image
                            .RenderImage(NoiseInterpreter
                                    .GetGradientMap(TemperatureNoise, Color.red, Color.cyan));

        ElevationPanel = new JPanel(new BorderLayout()) {
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D)g;
                g2d.clearRect(0, 0, getWidth(), getHeight());
                g2d.drawImage(ElevationMap, 0, 0, this);
            }
        };
        ElevationPanel.addMouseMotionListener(new MouseMotionListener() {
            @Override
            public void mouseDragged(MouseEvent e) {

            }

            @Override
            public void mouseMoved(MouseEvent e) {
                changeBiomeLabel(e);
            }
        });
        ElevationPanel.setPreferredSize(generator.getDimensions());

        MoisturePanel = new JPanel(new BorderLayout()) {
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D)g;
                g2d.clearRect(0, 0, getWidth(), getHeight());
                g2d.drawImage(MoistureMap, 0, 0, this);
            }
        };
        MoisturePanel.addMouseMotionListener(new MouseMotionListener() {
            @Override
            public void mouseDragged(MouseEvent e) {

            }

            @Override
            public void mouseMoved(MouseEvent e) {
                changeBiomeLabel(e);
            }
        });
        MoisturePanel.setPreferredSize(generator.getDimensions());

        TemperaturePanel = new JPanel(new BorderLayout()) {
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D)g;
                g2d.clearRect(0, 0, getWidth(), getHeight());
                g2d.drawImage(TemperatureMap, 0, 0, this);
            }
        };
        TemperaturePanel.addMouseMotionListener(new MouseMotionListener() {
            @Override
            public void mouseDragged(MouseEvent e) {

            }

            @Override
            public void mouseMoved(MouseEvent e) {
                changeBiomeLabel(e);
            }
        });
        TemperaturePanel.setPreferredSize(generator.getDimensions());

        window.setLayout(new FlowLayout());
        window.getContentPane().add(ElevationPanel);
        window.getContentPane().add(MoisturePanel);
        window.getContentPane().add(TemperaturePanel);
        window.getContentPane().add(BiomeLabel);
        window.pack();
    }

    public void show(){
        window.setVisible(true);
    }

    private String determineBiome(Biome b){
        switch(b){
            case OCEAN:
                return "Ocean";
            case BEACH:
                return "Beach";
            case DESERT:
                return "Desert";
            case TEMPERATE_DESERT:
                return "Temperate Desert";
            case SCORCHED_DESERT:
                return "Scorched Desert";
            case GRASSLAND:
                return "Grassland";
            case SHRUBLAND:
                return "Shrubland";
            case CRAG:
                return "Crag";
            case LAKES:
                return "Lakes";
            case MARSH:
                return "Marsh";
            case PLAINS:
                return "Plains";
            case FOREST:
                return "Forest";
            case RAIN_FOREST:
                return "Rain Forest";
            case TAIGA:
                return "Taiga";
            case TUNDRA:
                return "Tundra";
            case SNOW:
                return "Snow";
        }

        return "err";
    }

    private void changeBiomeLabel(MouseEvent e){
        int x = e.getX();
        int y = e.getY();
        BiomeLabel.setText("Biome: "
                + determineBiome(MapInterpreter
                .GetBiome(ElevationNoise[x][y], MoistureNoise[x][y], TemperatureNoise[x][y])));
        window.validate();
    }
}
