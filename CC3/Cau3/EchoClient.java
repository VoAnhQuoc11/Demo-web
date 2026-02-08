package Cau3;

import java.io.*;
import java.net.*;
import java.util.Scanner;

public class EchoClient {
    public static void main(String[] args) {
        String serverAddress = "localhost";
        int port = 12345;

        try (Socket socket = new Socket(serverAddress, port);Scanner scanner = new Scanner(System.in)) {
            
            // Tạo luồng đọc và ghi
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            // Nhập và gửi tin nhắn lên server
            System.out.print("Nhap tin nhan gui den Server: ");
            String myMessage = scanner.nextLine();
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