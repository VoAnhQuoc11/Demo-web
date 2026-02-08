package Cau8;

import java.io.*;
import java.net.*;

public class EchoServer {
    private static final String PASSWORD = "4444";
    public static void main(String[] args) {
        int port = 12345; 

        System.out.println("Server dang khoi dong tai port " + port + "...");

        // Try-with-resources để tự động đóng socket khi xong
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            
            Socket clientSocket = serverSocket.accept();
            System.out.println("Client da ket noi. Dang cho xac thuc...");

            //  Tạo luồng đọc input và ghi output
            BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true); // true để auto-flush

            String clientPass = in.readLine();

            // Kiểm tra mật khẩu
            if (clientPass != null && clientPass.equals(PASSWORD)) {
                System.out.println("Mat khau dung. Chap nhan ket noi.");
                out.println("OK"); // Gửi tín hiệu thành công cho Client
            } else {
                System.out.println("Mat khau sai. Tu choi ket noi.");
                out.println("WRONG"); // Gửi tín hiệu thất bại
                clientSocket.close(); // Đóng kết nối
                return; 
            }

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