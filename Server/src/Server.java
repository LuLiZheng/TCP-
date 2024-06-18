import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Base64;
import java.util.zip.CRC32;

public class Server {
    private static final int PORT = 12345;
    private static final String VERSION = "2.0";

    public static void main(String[] args) {
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("服务器已启动，端口号：" + PORT);
            while (true) {
                // 接受客户端连接并启动新的线程处理
                new ClientHandler(serverSocket.accept()).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static class ClientHandler extends Thread {
        private Socket socket;
        private BufferedReader in;
        private PrintWriter out;
        private DataOutputStream dataOut;

        public ClientHandler(Socket socket) {
            this.socket = socket;
        }

        @Override
        public void run() {
            try {
                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                out = new PrintWriter(socket.getOutputStream(), true);
                dataOut = new DataOutputStream(socket.getOutputStream());

                // 向客户端发送版本号
                out.println(VERSION);

                // 接收客户端的版本号响应
                String clientResponse = in.readLine();
                if ("False".equals(clientResponse)) {
                    // 客户端版本不匹配，发送Client.txt文件
                    sendFile("Client.txt");
                } else if ("True".equals(clientResponse)) {
                    // 客户端版本匹配，准备接收数据
                    out.println("OK");
                    handleClientData();
                }

            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        // 发送文件给客户端
        private void sendFile(String fileName) throws IOException {
            File file = new File(fileName);
            byte[] fileContent = Files.readAllBytes(file.toPath());
            out.println(Base64.getEncoder().encodeToString(fileContent));
        }

        // 处理客户端数据
        private void handleClientData() throws IOException {
            String input = in.readLine();
            if ("1".equals(input)) {
                handleFieldData();
            } else if ("2".equals(input)) {
                handleFileData();
            }
        }

        // 处理字段数据
        private void handleFieldData() throws IOException {
            String encodedData = in.readLine();
            String decodedData = new String(Base64.getDecoder().decode(encodedData));
            System.out.println("收到的字段数据：" + decodedData);

            // 计算CRC32校验值
            CRC32 crc32 = new CRC32();
            crc32.update(decodedData.getBytes());
            long checksum = crc32.getValue();

            // 存入数据库
            DatabaseHelper.insertData(decodedData);
        }

        // 处理文件数据
        private void handleFileData() throws IOException {
            String encodedFileName = in.readLine();
            String fileName = new String(Base64.getDecoder().decode(encodedFileName));
            System.out.println("正在接收文件：" + fileName);

            out.println("OKAY");

            String encodedFileContent = in.readLine();
            byte[] fileContent = Base64.getDecoder().decode(encodedFileContent);

            // 计算文件内容的CRC32校验值
            CRC32 crc32 = new CRC32();
            crc32.update(fileContent);
            long checksum = crc32.getValue();

            // 验证校验值是否匹配
            String receivedChecksum = in.readLine();
            if (Long.toString(checksum).equals(receivedChecksum)) {
                // 校验通过，保存文件
                Files.write(Paths.get(fileName), fileContent);
                System.out.println("文件已保存：" + fileName);
                out.println("ACK");
            } else {
                // 校验失败
                System.out.println("文件校验失败");
                out.println("NACK");
            }
        }
    }
}
