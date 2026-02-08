package Cau1;

import java.io.*;
import java.net.*;

public class EchoClient {
    public static void main(String[] args) {
        String serverAddress = "localhost";
        int port = 12345;

        try (Socket socket = new Socket(serverAddress, port)) {
            
            // Tạo luồng đọc và ghi
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            // Gửi tin nhắn lên server
            String myMessage = "Hello World";
            out.println(myMessage);
            System.out.println("Client da gui: " + myMessage);

            // Đọc phản hồi từ server
            String response = in.readLine();
            System.out.println("Server tra loi: " + response);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}