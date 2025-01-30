package Main;

public class ThreadWorker extends Thread {
    private final java.awt.Point ponto;
    private final int largura, altura;
    private final int id;
    private final Main painel;
    private int dirX, dirY;

    public ThreadWorker(java.awt.Point ponto, int largura, int altura, int id, Main painel) {
        this.ponto = ponto;
        this.largura = largura;
        this.altura = altura;
        this.id = id;
        this.painel = painel;

        this.dirX = (int) (Math.random() * 3) - 1;
        this.dirY = (int) (Math.random() * 3) - 1;
    }

    @Override
    public void run() {
        // A thread só começa a se mover quando mtBool for true
        while (!painel.mtBool) {
            try {
                Thread.sleep(100);  // Aguarda até que mtBool seja true
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        // Movimento de cada ponto
        for (int i = 0; i < 50; i++) {
            try {
                Thread.sleep(30);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            ponto.x += dirX;
            ponto.y += dirY;

            if (ponto.x < 0 || ponto.x > largura) {
                dirX = -dirX;
            }
            if (ponto.y < 0 || ponto.y > altura) {
                dirY = -dirY;
            }
        }
    }
}
