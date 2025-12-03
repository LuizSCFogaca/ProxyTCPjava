import java.io.*;
import java.net.*;

public class TCPProxy {

    public static void main(String[] args) {
        if (args.length != 3) {
            System.out.println("Uso: java TCPProxy <porta_local_proxy> <host_remoto> <porta_remota>");
            return;
        }

        int localPort = Integer.parseInt(args[0]);     // Porta onde o Proxy escuta (ex: 8080)
        String remoteHost = args[1];                   // IP do Servidor Real
        int remotePort = Integer.parseInt(args[2]);    // Porta do Servidor Real

        try (ServerSocket serverSocket = new ServerSocket(localPort)) {
            System.out.println("Proxy TCP rodando na porta " + localPort);
            System.out.println("Encaminhando para " + remoteHost + ":" + remotePort);

            while (true) {
                // 1. Aceita conexão do Cliente (ex: seu TCPClient ou browser)
                Socket clientSocket = serverSocket.accept();
                System.out.println("Nova conexão de: " + clientSocket.getInetAddress());

                // 2. Cria conexão com o Servidor Real (ex: seu TCPServer)
                Socket serverSocketBackend = new Socket(remoteHost, remotePort);

                // 3. Inicia threads para repassar dados nas duas direções
                // Cliente -> Servidor
                new Thread(new ProxyTask(clientSocket, serverSocketBackend, "CLIENTE->SERVIDOR")).start();
                // Servidor -> Cliente
                new Thread(new ProxyTask(serverSocketBackend, clientSocket, "SERVIDOR->CLIENTE")).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Classe interna para gerenciar o tráfego
    private static class ProxyTask implements Runnable {
        private Socket source;
        private Socket destination;
        private String direction;

        public ProxyTask(Socket source, Socket destination, String direction) {
            this.source = source;
            this.destination = destination;
            this.direction = direction;
        }

        @Override
        public void run() {
            byte[] buffer = new byte[4096]; // Buffer de leitura
            int bytesRead;

            try {
                InputStream in = source.getInputStream();
                OutputStream out = destination.getOutputStream();

                // LOOP PRINCIPAL DE TRANSFERÊNCIA
                while ((bytesRead = in.read(buffer)) != -1) {
                    
                    //long startTime = System.nanoTime(); // Timestamp inicio

                    // --- PONTO DE MONITORAMENTO (Requisito 2 do PDF) ---
                    // Aqui você calcula throughput (bytesRead / tempo)
                    
                    // --- PONTO DE OTIMIZAÇÃO (Requisito 3 do PDF) ---
                    // Exemplo: TCP Pacing (atraso proposital)
                    // Thread.sleep(10); 

                    // Repassa os dados
                    out.write(buffer, 0, bytesRead);
                    out.flush();
                    
                    // Log simples (Requisito 4 do PDF)
                    System.out.println("[" + direction + "] Bytes: " + bytesRead);
                }
            } catch (IOException e) {
                // Conexão encerrada ou erro
            } finally {
                try { source.close(); destination.close(); } catch (Exception e) {}
            }
        }
    }
}