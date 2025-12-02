import java.io.*;
import java.net.*;

public class TCPClient {
    public static void main(String[] args) {
        if (args.length != 2) {
            System.out.println("Parametros: <remote_host> <remote_port>");
            System.exit(1);
        }

        String hostname = args[0];
        int port = Integer.parseInt(args[1]);

        System.out.println("> Conectando no servidor '" + hostname + ":" + port + "'");

        try (Socket socket = new Socket(hostname, port);
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             BufferedReader stdIn = new BufferedReader(new InputStreamReader(System.in))) {

            String userInput;
            // Lê do teclado
            while ((userInput = stdIn.readLine()) != null) {
                // Envia para o servidor
                out.println(userInput);

                // Recebe do servidor
                String serverResponse = in.readLine();
                System.out.println("Recebi: " + serverResponse);

                if (userInput.equals("exit")) {
                    break;
                }
            }
        } catch (UnknownHostException e) {
            System.err.println("Host desconhecido: " + hostname);
            System.exit(1);
        } catch (IOException e) {
            System.err.println("Erro de I/O na conexao com " + hostname);
            System.exit(1);
        }
    }
}