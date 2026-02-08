package Cau7;

import java.io.*;
import java.net.*;

public class EchoServer {
    public static void main(String[] args) {
        int port = 12345; 
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
        String encryptedMessage;

            // Vòng lặp nhận tin nhắn
            while ((encryptedMessage = in.readLine()) != null) {
                
                // Dịch ngược ký tự -1 
               
                String originalMessage = caesarCipher(encryptedMessage, -1);

                //Kiểm tra điều kiện thoát
                if (originalMessage.equals("0")) {
                    System.out.println("Client yeu cau ngat ket noi.");
                    break;
                }

                System.out.println("Tin nhan goc tu Client: " + originalMessage + " (Da nhan ma hoa: " + encryptedMessage + ")");

                // Chuẩn bị câu trả lời
                String reply = "Server da hieu: " + originalMessage;

                // Mã hóa câu trả lời trước khi gửi +1
                String encryptedReply = caesarCipher(reply, 1);
                out.println(encryptedReply);
            }

            System.out.println("Da phan hoi xong. Server tu dong.");

        } catch (IOException e) {
            e.printStackTrace();
        }
    } 
    // Hàm chuyển đổi
public static String caesarCipher(String text, int key) {
        char[] chars = text.toCharArray();
        for (int i = 0; i < chars.length; i++) {
            chars[i] = (char) (chars[i] + key);
        }
        return new String(chars);
    }
}
