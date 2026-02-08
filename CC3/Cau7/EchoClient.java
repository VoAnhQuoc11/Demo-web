package Cau7;

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
             System.out.print("Nhap tin nhan: ");
                String originalMessage = scanner.nextLine();

                //  Dịch chuyển ký tự +1 trước khi gửi
                // 
                String encryptedMessage = caesarCipher(originalMessage, 1);

                // Gửitin đã mã hóa lên Server
                out.println(encryptedMessage);

                // Kiểm tra thoát
                if (originalMessage.equals("0")) {
                    System.out.println("Da ngat ket noi.");
                    break;
                }

                // Nhận phản hồi bị mã hóa từ Server
                String encryptedResponse = in.readLine();

                //  Dịch ngược -1
                String decryptedResponse = caesarCipher(encryptedResponse, -1);
                
                System.out.println("Server tra loi (Da giai ma): " + decryptedResponse);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public static String caesarCipher(String text, int key) {
        char[] chars = text.toCharArray();
        for (int i = 0; i < chars.length; i++) {
            chars[i] = (char) (chars[i] + key);
        }
        return new String(chars);
    }
}
