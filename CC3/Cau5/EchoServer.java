package Cau5;

import java.io.*;
import java.net.*;

public class EchoServer {
    public static void main(String[] args) {
        int port = 12345; // Cổng giao tiếp
        String[] numberText = {"Khong", "Mot", "Hai", "Ba", "Bon", "Nam", "Sau", "Bay", "Tam", "Chin", "Muoi"};

        System.out.println("Server dang khoi dong tai port " + port + "...");

        // Try-with-resources để tự động đóng socket khi xong
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            
            //  Chặn và chờ client kết nối
            Socket clientSocket = serverSocket.accept();
            System.out.println("Client da ket noi!");

            //  Tạo luồng đọc input và ghi output
            BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true); // true để auto-flush

            // Đọc tin nhắn từ client
           String inputLine;

            // Vòng lặp đọc dữ liệu 
            while ((inputLine = in.readLine()) != null) {
                
                // Kiểm tra điều kiện thoát
                if (inputLine.equalsIgnoreCase("Quit")) {
                    System.out.println("Client yeu cau thoat.");
                    break;
                }

                String response;
                try {
                    //  Chuyển chuỗithành số nguyên
                    int number = Integer.parseInt(inputLine);

                    //  Kiểm tra điều kiện 
                    if (number >= 0 && number <= 10) {
                        response = numberText[number]; // Lấy chữ tương ứng
                    } else {
                        response = "So vuot qua pham vi (0-10)";
                    }

                } catch (NumberFormatException e) {
                    // Xử lý nếu không phải số
                    response = "Vui long nhap so hoac chu 'Quit'";
                }

                // Gửi kết quả về Client
                out.println(response);
            }

            System.out.println("Da phan hoi xong. Server tu dong.");

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}