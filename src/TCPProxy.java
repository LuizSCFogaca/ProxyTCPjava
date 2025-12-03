import java.io.*;
import java.net.*;
import java.text.SimpleDateFormat;
import java.util.Date;

public class TCPProxy {

    // Mude para true para testar Otimização
    private static final boolean ATIVAR_PACING = false; 
    private static final int TAXA_LIMITE_KBPS = 50; // Limite de 50KB/s se o pacing estiver ativo
    
    // Mude o tamanho do buffer para testar impacto no desempenho (Buffer Tuning)
    private static final int BUFFER_SIZE = 16384; // 16KB

    // Arquivo de Log
    private static final String LOG_FILE = "proxy_log.csv";

    public static void main(String[] args) {
        if (args.length != 3) {
            System.out.println("Uso: java TCPProxy <porta_local> <host_remoto> <porta_remota>");
            return;
        }

        int localPort = Integer.parseInt(args[0]);
        String remoteHost = args[1];
        int remotePort = Integer.parseInt(args[2]);

        // Inicializa o arquivo de Log com o cabeçalho
        inicializarLog();

        try (ServerSocket serverSocket = new ServerSocket(localPort)) {
            System.out.println("=== TCP Proxy Iniciado ===");
            System.out.println("Escutando em: " + localPort);
            System.out.println("Encaminhando para: " + remoteHost + ":" + remotePort);
            System.out.println("Log sendo gravado em: " + LOG_FILE);
            System.out.println("Otimização (Pacing): " + (ATIVAR_PACING ? "ATIVADO" : "DESATIVADO"));
            System.out.println("==========================");

            int connectionId = 0;

            while (true) {
                Socket clientSocket = serverSocket.accept();
                connectionId++;
                System.out.println("Nova conexão (#" + connectionId + ") de: " + clientSocket.getInetAddress());

                Socket serverSocketBackend = new Socket(remoteHost, remotePort);

                // Configuração de Otimização: Ajuste de Buffer (Etapa 3)
                otimizarSocket(clientSocket);
                otimizarSocket(serverSocketBackend);

                // Thread Cliente -> Servidor
                new Thread(new ProxyTask(clientSocket, serverSocketBackend, "CLIENTE->SERVER", connectionId)).start();
                // Thread Servidor -> Cliente
                new Thread(new ProxyTask(serverSocketBackend, clientSocket, "SERVER->CLIENTE", connectionId)).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Função auxiliar para configurar buffers
    private static void otimizarSocket(Socket s) throws SocketException {
        s.setReceiveBufferSize(BUFFER_SIZE);
        s.setSendBufferSize(BUFFER_SIZE);
        // s.setTcpNoDelay(true); //latência menor
    }

    // Inicializa o CSV
    private static void inicializarLog() {
        try (FileWriter fw = new FileWriter(LOG_FILE, false);
             PrintWriter pw = new PrintWriter(fw)) {
            pw.println("Timestamp,ConnID,Direcao,Bytes,Throughput_Mbps,Latencia_ms,Pacing_Ativo");
        } catch (IOException e) {
            System.err.println("Erro ao criar log: " + e.getMessage());
        }
    }

    // Método sincronizado para escrever no log (Thread-safe)
    private static synchronized void logMetricas(int connId, String direcao, int bytes, double throughput, double latencia) {
        try (FileWriter fw = new FileWriter(LOG_FILE, true); 
             PrintWriter pw = new PrintWriter(fw)) {
            
            String timeStamp = new SimpleDateFormat("HH:mm:ss.SSS").format(new Date());
            pw.printf("%s,%d,%s,%d,%.4f,%.2f,%b%n", 
                timeStamp, connId, direcao, bytes, throughput, latencia, ATIVAR_PACING);
            
        } catch (IOException e) {
            System.err.println("Erro ao escrever no log: " + e.getMessage());
        }
    }

    // Classe interna que processa o tráfego
    private static class ProxyTask implements Runnable {
        private Socket source;
        private Socket destination;
        private String direction;
        private int connId;

        public ProxyTask(Socket source, Socket destination, String direction, int connId) {
            this.source = source;
            this.destination = destination;
            this.direction = direction;
            this.connId = connId;
        }

        @Override
        public void run() {
            byte[] buffer = new byte[BUFFER_SIZE];
            int bytesRead;

            try {
                InputStream in = source.getInputStream();
                OutputStream out = destination.getOutputStream();

                while ((bytesRead = in.read(buffer)) != -1) {
                    long startTime = System.nanoTime();

                    // --- POLÍTICA DE OTIMIZAÇÃO: TCP PACING ---
                    if (ATIVAR_PACING) {
                        aplicarPacing(bytesRead);
                    }

                    // Encaminha os dados
                    out.write(buffer, 0, bytesRead);
                    out.flush();

                    long endTime = System.nanoTime();

                    // --- CÁLCULO DE MÉTRICAS  ---
                    double durationSeconds = (endTime - startTime) / 1000000000; // segundos
                    double durationMs = (endTime - startTime) / 1000000;          // milissegundos
                    
                    // Throughput = Bits / Segundos -> Mbps
                    double bits = bytesRead * 8;
                    double throughputMbps = (bits / durationSeconds) / 1000000;

                    // Exibe no console
                    System.out.printf("[%s #%d] %d B | %.2f Mbps | %.2f ms%n", 
                        direction, connId, bytesRead, throughputMbps, durationMs);

                    // --- REGISTRO DE LOG ---
                    logMetricas(connId, direction, bytesRead, throughputMbps, durationMs);
                }
            } catch (IOException e) {
                // Conexão fechada normalmente ou erro de rede
            } finally {
                try { source.close(); destination.close(); } catch (Exception e) {}
            }
        }

        // Simula controle de congestionamento atrasando o envio
        private void aplicarPacing(int bytes) {
            try {
                // Calcula quanto tempo levaria para enviar esses bytes na taxa limite
                double expectedTimeMs = ((double) bytes / (TAXA_LIMITE_KBPS * 1024)) * 1000;
                if (expectedTimeMs > 0) {
                    Thread.sleep((long) expectedTimeMs);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }
}