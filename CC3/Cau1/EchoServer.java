package Cau1;

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

            //  Tạo luồng Đọc input và ghi output
            BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true); // true để auto-flush

            // Đọc tin nhắn từ client
            String message = in.readLine();
            System.out.println("Server nhan duoc: " + message);

            //  Gửi phản hồi lại cho client 
            out.println("Server da nhan: " + message);

            System.out.println("Da phan hoi xong. Server tu dong.");

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}