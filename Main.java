import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.List;

public class Main {
    private static List<DataOutputStream> clientStreams = new ArrayList<>();
    
    public static void main(String[] args) {
        int port = Integer.parseInt(System.getenv().getOrDefault("PORT", "8080")); // Render provides the PORT environment variable
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("Server started on port: " + port);
            
            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("Client connected: " + clientSocket.getInetAddress());
                
                DataOutputStream out = new DataOutputStream(clientSocket.getOutputStream());
                DataInputStream in = new DataInputStream(clientSocket.getInputStream());
                
                synchronized (clientStreams) {
                    clientStreams.add(out);
                }
                
                // Handle each client in a new thread
                new Thread(() -> handleClient(in, out)).start();
            }
        } catch (IOException e) {
            System.err.println("Error in server: " + e.getMessage());
        }
    }
    
    private static void handleClient(DataInputStream in, DataOutputStream out) {
        try {
            while (true) {
                String message = in.readUTF();
                System.out.println("Received: " + message);
                broadcastMessage(message);
            }
        } catch (IOException e) {
            System.out.println("Client disconnected.");
            synchronized (clientStreams) {
                clientStreams.remove(out);
            }
        }
    }
    
    private static void broadcastMessage(String message) {
        synchronized (clientStreams) {
            for (DataOutputStream clientStream : clientStreams) {
                try {
                    clientStream.writeUTF(message);
                    clientStream.flush();
                } catch (IOException e) {
                    System.err.println("Error broadcasting message: " + e.getMessage());
                }
            }
        }
    }
}
