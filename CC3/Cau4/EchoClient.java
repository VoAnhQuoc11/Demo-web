package Cau4;

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
            System.out.println("Da ket noi den Server.");
            System.out.println("Nhap tin nhan va an Enter de gui.");
            System.out.println("Nhap so 0 de thoat chuong trinh.");

            // Vòng lặp gửi tin nhắn
            while (true) {
                System.out.print("Client: ");
                String myMessage = scanner.nextLine();

                // Gửi tin nhắn lên server
                out.println(myMessage);

                //  Kiểm tra nếu người dùng nhập 0 thì thoát
                if (myMessage.equals("0")) {
                    System.out.println("Da ngat ket noi.");
                    break;
                }

                // Đọc phản hồi từ server 
                String response = in.readLine();
                System.out.println("Server tra loi: " + response);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}