package chatProject.server;



import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

public class ChatServer {
    public static void main(String[] args) {


        // 1. 서버 소캣 생성
        try ( ServerSocket serverSocket = new ServerSocket(12345);) {
            System.out.println("서버가 준비 되었습니다.");

            // 필드 : 여러명의 클라이언트 정보를 기억할 공간 (자료구조) [클라이언트 정보]
            Map<String, PrintWriter> chatClients = new HashMap<>();

            // 사용자에 해당하는 방 정보를 넣을 자료구조
            Map<String, Integer> userRooms = new HashMap<>();

//            // 채팅방 1 생성
//            RoomController roomController = new RoomController();

            //2. accept()를 통해 소켓을 얻어옴 (여러명의 클라이언트와 접속하도록 구현)
            while (true) {
                Socket socket = serverSocket.accept();

                // Thread 이용
                new ChatThread(socket, chatClients, userRooms).start();


            }
        } catch (Exception e) {
            System.out.println(e);
            e.printStackTrace();
        }

    }
}
