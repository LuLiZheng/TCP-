import java.io.*;
import java.net.Socket;
import java.nio.file.Files;
import java.util.Base64;
import java.util.Scanner;
import java.nio.file.Paths;


public class Client {
    private static final String SERVER_ADDRESS = "localhost";
    private static final int SERVER_PORT = 12345;
    private static final String VERSION = "2.0";

    public static void main(String[] args) {
        while (true) {

            try {
                Thread.sleep(1000); // 1000毫秒 = 1秒
            } catch (InterruptedException e) {
                // 处理中断异常
            }

            try (Socket socket = new Socket(SERVER_ADDRESS, SERVER_PORT);
                 BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                 PrintWriter out = new PrintWriter(socket.getOutputStream(), true)) {

                // 接收服务器版本号
                String serverVersion = in.readLine();
                System.out.println("服务器版本：" + serverVersion);

                if (!VERSION.equals(serverVersion)) {
                    out.println("False");
                    String fileContent = in.readLine();
                    byte[] decodedFileContent = Base64.getDecoder().decode(fileContent);
                    Files.write(Paths.get("Newclient.c"), decodedFileContent);
                    System.out.println("文件已接收并保存为 Newclient.c");
                } else {
                    out.println("True");
                    String response = in.readLine();
                    if ("OK".equals(response)) {
                        Scanner scanner = new Scanner(System.in);
                        System.out.println("选择模式：1 为字段数据，2 为文件");
                        String choice = scanner.nextLine();
                        out.println(choice);

                        if ("1".equals(choice)) {
                            System.out.println("输入字段数据：");
                            String data = scanner.nextLine();
                            String encodedData = Base64.getEncoder().encodeToString(data.getBytes());
                            System.out.println("发送数据？（Y/N）");
                            String confirm = scanner.nextLine();
                            if ("Y".equalsIgnoreCase(confirm)) {
                                out.println(encodedData);
                            }
                        } else if ("2".equals(choice)) {
                            System.out.println("输入文件路径：");
                            String filePath = scanner.nextLine();
                            File file = new File(filePath);
                            String fileName = file.getName();
                            String encodedFileName = Base64.getEncoder().encodeToString(fileName.getBytes());
                            byte[] fileContent = Files.readAllBytes(file.toPath());
                            String encodedFileContent = Base64.getEncoder().encodeToString(fileContent);
                            System.out.println("发送文件？（Y/N）");
                            String confirm = scanner.nextLine();
                            if ("Y".equalsIgnoreCase(confirm)) {
                                out.println(encodedFileName);
                                String serverResponse = in.readLine();
                                if ("OKAY".equals(serverResponse)) {
                                    out.println(encodedFileContent);
                                }
                            }
                        }
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}