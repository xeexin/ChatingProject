package chatProject.server;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.*;

public class ChatThread extends Thread{

    // 생성자를 통해 클라이언트 소켓을 얻어옴,
    private Socket socket;
    private String nickname;
    private Map<String, PrintWriter> chatClients;
    private Map<String, Integer> userRooms; // 클라이언트의 방 (닉네임, 방번호)
    private static Map<Integer, List<ChatThread>> roomList = new HashMap<>();
    private static int ROOMID = 1;

    static{
        roomList.put(ROOMID, new ArrayList<>());
    }

    BufferedReader in = null;
    public ChatThread(Socket socket, Map<String, PrintWriter> chatClients, Map<String, Integer> userRooms) {
        this.socket = socket;
        this.chatClients = chatClients;
        this.userRooms = userRooms;

        // 클라이언트가 생성될 때 클라이언트로 부터 아이디를 얻어오게
        try {
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            nickname = in.readLine();

            broadcast(nickname + "님이 입장하셨습니다.");
            System.out.println("사용자의 아이디는 " + nickname + " / IP address : " + socket.getInetAddress());

            // 동시에 일어날 수도
            synchronized (chatClients) {
                chatClients.put(this.nickname, out);
                userRooms.put(this.nickname, ROOMID);
                roomList.get(ROOMID).add(this);
            }
        } catch (Exception e) {
            System.out.println(e);
            e.printStackTrace();

        }
    }

    @Override
    public void run() {
        // 연결된 클라이언트가 메시지를 전송하면, 그 메시지를 받아서 다른 사용자들에게 보내줌.
        String msg = null;
        ChatThread chatThread = this;
        try {
            while ((msg = in.readLine()) != null) {
                if ("/quit".equalsIgnoreCase(msg)) {
                    System.out.println("채팅을 종료합니다.");
                    break;
                }
                if (msg.indexOf("/to") == 0) {
                    sendMsg(msg);
                } else {
                    broadcast(nickname + " : " + msg);
                }
                if ("/create".equalsIgnoreCase(msg)) {
                    // 새로운 채팅방을 생성하는 메서드
                    createChat((ChatThread) currentThread()); //이게 맞나..?
                }

                if ("/list".equalsIgnoreCase(msg)) {
                    // 리스트를 보여주는 메서드
                    showList();
                }
                if ("/join".equalsIgnoreCase(msg)) {
                    // join 뒤에 숫자 받아 오기
                    // 메시지에서 방 번호를 추출합니다.
                    int roomId = 0;
                    String[] parts = msg.split(" ");
                    if (parts.length != 2) {
                        System.out.println("잘못된 명령어 형식입니다. 사용법: /join 방번호");
                    }
                    String roomIdStr = parts[1].trim(); // 방 번호 문자열 추출
                    try {
                        roomId = Integer.parseInt(roomIdStr);
                    } catch (NumberFormatException e) {
                        System.out.println("잘못된 방 번호입니다.");
                    }
                    // 방번호의 채팅방으로 입장하는 메서드
                    joinChat(roomId);
                }
                if ("/exit".equalsIgnoreCase(msg)) {
                    //[방번호] 채팅방에서 나가는 메서드
                    String[] parts = msg.split(" ");
                    if (parts.length != 2) {
                        System.out.println("잘못된 명령어 형식입니다. 사용법: /exit 방번호");
                        return;
                    }
                    int roomId;
                    try {
                        roomId = Integer.parseInt(parts[1]);
                    } catch (NumberFormatException e) {
                        System.out.println("잘못된 방 번호입니다.");
                        return;
                    }

                    exitChat(roomId);
                }
            }
        } catch (IOException e) {
            System.out.println(e);
        }
        finally {
            synchronized (chatClients) {
                chatClients.remove(nickname);
            }
            broadcast(nickname + " 님이 채팅에서 나갔습니다.");

            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }

            if (socket != null) {
                try {
                    socket.close();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

    private void exitChat(int roomId) {

    }

    private void joinChat(int roomId) {
          // 사용자를 새로운 채팅방에 참여시킴
        // 해당 roomId가 있는지 확인
        //없으면, "방이 존재하지 않습니다."
        //있으면, roomId번 방의 채팅방에 입장.
        synchronized (roomList) {
            List<ChatThread> roomMembers = roomList.get(roomId);
            if (roomMembers != null) {
                // 해당 roomId의 채팅방에 사용자를 참여시킴
                roomMembers.add(this);
                userRooms.put(nickname, roomId);
                System.out.println(nickname + "님이 " + roomId + "번 방에 참여하였습니다.");
            } else {
                System.out.println("방이 존재하지 않습니다.");
            }
        }
    }

    private void createChat(ChatThread chatThread) {
        ROOMID++; // 방 +1
        List<ChatThread> roomMembers = new ArrayList<>(); //유저 리스트 생성
        roomMembers.add(chatThread); //입장 후 유저 리스트에 추가
        roomList.put(ROOMID, roomMembers); //생성된 방을 방 리스트에 추가
        userRooms.put(this.nickname, ROOMID); //속해있는 방 업데이트
        System.out.println(ROOMID + "번 방 생성 완료");
    }

    private void showList() {
        synchronized (roomList) {
            // 방 목록이 비어 있는지 확인
            if (roomList.isEmpty()) {
                System.out.println("방이 없습니다. 방을 생성하려면 /create를 입력하세요.");
            } else {
                System.out.print("현재 방 목록:");
                //Set<Integer> uniqueRoomNumbers = new HashSet<>(userRooms.values());
                for (Integer roomNumber : roomList.keySet()) {
                    System.out.print(roomNumber + " ");
                }
            }
        }
    }


    //메시지를 특정 사용자에게만 보내는 메서드
    public void sendMsg(String msg) {
        int firstSpaceIndex = msg.indexOf(" ");
        if (firstSpaceIndex == -1) {
            return;  // 공백이 없다면... 실행 종료
        }
        int secontSpaceIndex = msg.indexOf(" ", firstSpaceIndex + 1);
        if (secontSpaceIndex == -1) {
            return;
        }
        String to = msg.substring(firstSpaceIndex + 1, secontSpaceIndex);
        String message = msg.substring(secontSpaceIndex + 1);

        //to(수신자)에게 메시지 전송.
        PrintWriter pw = chatClients.get(to);
        if (pw != null) {
            pw.println(nickname + "님으로 부터 온 비밀 메시지 " + message);
        } else {
            System.out.println("오류 ;: 수신자 " + to + " 님을 찾을 수 없습니다.");
        }

    }


    // 전체 사용자에게 알려주는 메소드
    public void broadcast(String msg) {

        synchronized (chatClients) {
            Iterator<PrintWriter> it = chatClients.values().iterator();
            while (it.hasNext()) {
                PrintWriter out = it.next();
                try {
                    out.println(msg);
                } catch (Exception e) {
                    it.remove();
                    e.printStackTrace();
                }
            }
        }
    }
}

