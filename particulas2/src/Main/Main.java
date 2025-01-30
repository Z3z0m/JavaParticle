package Main;

import javax.swing.*;
import java.awt.*;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Main extends JPanel {

    private static final int LARGURA = 1920;
    private static final int ALTURA = 1080;
    private static final int NUM_PONTOS = 1000;
    private static final int NUM_THREADS = Runtime.getRuntime().availableProcessors();
    private static final int MAXFPS = 1000;

    private final Random random = new Random();
    private final Point[] pontos;
    private final int[] velocidadesX;
    private final int[] velocidadesY;
    private final Color[] cores;
    private int fps;
    private long ultimoTempo;
    private long tempoAcumulado;
    private int contadorQuadros;

    private final ExecutorService executorService;
    private final boolean usarMultithread;

    public Main(boolean usarMultithread) {
        this.usarMultithread = usarMultithread;
        pontos = new Point[NUM_PONTOS];
        velocidadesX = new int[NUM_PONTOS];
        velocidadesY = new int[NUM_PONTOS];
        cores = new Color[NUM_PONTOS];
        gerarPontosAleatorios();
        executorService = Executors.newFixedThreadPool(NUM_THREADS);
        ultimoTempo = System.nanoTime();
        tempoAcumulado = 0;
        contadorQuadros = 0;
    }

    private void gerarPontosAleatorios() {
        for (int i = 0; i < NUM_PONTOS; i++) {
            int x = random.nextInt(LARGURA);
            int y = random.nextInt(ALTURA);
            pontos[i] = new Point(x, y);
            velocidadesX[i] = random.nextInt(5) + 1;
            velocidadesY[i] = random.nextInt(5) + 1;
            cores[i] = new Color(random.nextInt(256), random.nextInt(256), random.nextInt(256));
        }
    }

    private void moverParticulas(int inicio, int fim) {
        for (int i = inicio; i < fim; i++) {
            pontos[i].x += velocidadesX[i];
            pontos[i].y += velocidadesY[i];

            if (pontos[i].x < 0 || pontos[i].x + 10 >= LARGURA) {
                velocidadesX[i] = -velocidadesX[i];
            }
            if (pontos[i].y < 0 || pontos[i].y + 10 >= ALTURA) {
                velocidadesY[i] = -velocidadesY[i];
            }
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        for (int i = 0; i < NUM_PONTOS; i++) {
            g.setColor(cores[i]);
            g.fillRect(pontos[i].x, pontos[i].y, 10, 10);
        }

        Font fonteOriginal = g.getFont();
        Font fonteGrande = fonteOriginal.deriveFont(fonteOriginal.getSize() * 5f);
        g.setFont(fonteGrande);

        g.setColor(Color.RED);
        g.drawString("FPS: " + fps, 10, 60);
        g.drawString("Modo: " + (usarMultithread ? "Multithread" : "Singlethread"), 10, 120);
    }

    public static void main(String[] args) {
        boolean usarMultithread = true;

        JFrame frame = new JFrame("Simulação de Partículas com Multithreading");
        Main painel = new Main(usarMultithread);
        frame.add(painel);
        frame.setSize(LARGURA, ALTURA);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);

        long tempoPorQuadroNanos = 1_000_000_000 / MAXFPS;
        long ultimoTempo = System.nanoTime();

        while (true) {
            long tempoAtual = System.nanoTime();
            long deltaTempo = tempoAtual - ultimoTempo;

            if (deltaTempo >= tempoPorQuadroNanos) {
                if (usarMultithread) {
                    int particulasPorThread = NUM_PONTOS / NUM_THREADS;
                    Thread[] threads = new Thread[NUM_THREADS];

                    for (int i = 0; i < NUM_THREADS; i++) {
                        int inicioLote = i * particulasPorThread;
                        int fimLote = (i == NUM_THREADS - 1) ? NUM_PONTOS : inicioLote + particulasPorThread;
                        threads[i] = new Thread(() -> painel.moverParticulas(inicioLote, fimLote));
                        threads[i].start();
                    }

                    for (Thread thread : threads) {
                        try {
                            thread.join();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                } else {
                    painel.moverParticulas(0, NUM_PONTOS);
                }

                painel.repaint();

                painel.tempoAcumulado += deltaTempo;
                painel.contadorQuadros++;

                if (painel.tempoAcumulado >= 1_000_000_000) {
                    painel.fps = painel.contadorQuadros;
                    painel.tempoAcumulado = 0;
                    painel.contadorQuadros = 0;
                }

                ultimoTempo = tempoAtual;
            }

            try {
                Thread.sleep(1);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}