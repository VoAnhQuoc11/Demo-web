package Cau9;

import java.io.*;
import java.net.*;

public class EchoServer {
    public static void main(String[] args) {
        int port = 12345; 
        String[] numberText = {"Khong", "Mot", "Hai", "Ba", "Bon", "Nam", "Sau", "Bay", "Tam", "Chin", "Muoi"};

        System.out.println("Server Delay dang chay tai port " + port + "...");

        // Try-with-resources để tự động đóng socket khi xong
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            
            //  Chặn và chờ client kết nối
            Socket clientSocket = serverSocket.accept();
            System.out.println("Client da ket noi!");

            //  Tạo luồng Đọc input và ghi output
            BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true); // true để auto-flush

            // Đọc tin nhắn từ client
           String inputLine;

            // Vòng lặp đọc dữ liệu =
            while ((inputLine = in.readLine()) != null) {
                
                
                if (inputLine.equalsIgnoreCase("Quit")) {
                    System.out.println("Client yeu cau thoat.");
                    break;
                }

                String response;
                try {
                    
                    int number = Integer.parseInt(inputLine);

                    if (number >= 0 && number <= 10) {
                        System.out.println("Nhan duoc so " + number + ". Dang doi " + number + " giay...");
                        
                      
                       try {
                            Thread.sleep(number * 1000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    
                        System.out.println("Da doi xong. Dang gui phan hoi...");
                        response = numberText[number]; // Lấy chữ tương ứng =
                    } else {
                        response = "So vuot qua pham vi (0-10)";
                    }

                } catch (NumberFormatException e) {
                    // Xử lý nếukhông phải số
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