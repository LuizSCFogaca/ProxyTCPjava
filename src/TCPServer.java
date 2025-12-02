import java.io.*;
import java.net.*;

public class TCPServer {
    public static void main(String[] args) {
        if (args.length != 1) {
            System.out.println("Parametros: <local_port>");
            System.exit(1);
        }

        int port = Integer.parseInt(args[0]);

        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("> Aguardando conexao na porta " + port);

            // O servidor fica em loop para aceitar conexões (sequencialmente)
            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("Cliente conectado: " + clientSocket.getInetAddress());

                try (
                    BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                    PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true)
                ) {
                    String linha;
                    // Lê até que o cliente desconecte ou envie "exit"
                    while ((linha = in.readLine()) != null) {
                        System.out.println("Recebi: " + linha);
                        out.println(linha); // Reenvia (Echo)
                        System.out.println("Reenviei: " + linha);

                        if (linha.equals("exit")) {
                            break;
                        }
                    }
                } catch (IOException e) {
                    System.err.println("Erro na conexao com cliente: " + e.getMessage());
                } finally {
                    clientSocket.close();
                    System.out.println("Conexao fechada.");
                }
            }
        } catch (IOException e) {
            System.err.println("Erro no servidor: " + e.getMessage());
        }
    }
}
