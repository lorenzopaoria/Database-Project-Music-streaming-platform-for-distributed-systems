import java.io.*;
import java.net.*;

public class DatabaseClient {
    public static void main(String[] args) {
        try (Socket socket = new Socket("localhost", 12345); // Porta del server
            BufferedReader input = new BufferedReader(new InputStreamReader(System.in));
            PrintWriter output = new PrintWriter(socket.getOutputStream(), true);
            BufferedReader serverInput = new BufferedReader(new InputStreamReader(socket.getInputStream()))
        ) {
            System.out.println("Inserisci la query SQL:");
            String query = input.readLine();
            output.println(query); // Invia la query al server

            // Legge la risposta del server
            String response;
            while ((response = serverInput.readLine()) != null) {
                System.out.println("Risultato: " + response);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
