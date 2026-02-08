package Cau4;

import java.io.*;
import java.net.*;

public class EchoServer {
    public static void main(String[] args) {
        int port = 12345; // Cổng giao tiếp

        System.out.println("Server dang khoi dong tai port " + port + "...");

        // Try-with-resources để tự động đóng socket khi xong
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            
            //  Chặn và chờ client kết nối
            Socket clientSocket = serverSocket.accept();
            System.out.println("Client da ket noi!");

            //  Tạo luồng đọc input và ghi output
            BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true); 

            // Đọc tin nhắn từ client
            String message ;
            while ((message = in.readLine()) != null) {
            if (message.equals("0")) {
                    System.out.println("Client yeu cau ngat ket noi.");
                    break; 
            }
            System.out.println("Server nhan duoc: " + message);

            //  Gửi phản hồi lại cho client 
            out.println("Server da nhan: " + message);
        }

            System.out.println("Da phan hoi xong. Server tu dong.");

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}