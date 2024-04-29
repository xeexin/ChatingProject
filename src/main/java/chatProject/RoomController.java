package chatProject;

import chatProject.client.ChatClient;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RoomController {
    private static Integer ROOMID = 1; //방번호 ++ --
    ChatClient chatClient; //socket, nickname, out

    Map<Integer, ChatClient> roomList = new HashMap<>(); // 방(ROOMID, chatClient)들 저장소


    //방 생성 후 리스트에 추가
    public void create() {
        Rooms room = new Rooms( ROOMID, chatClient); //방 하나 생성 -> 수정 필요
        // join(room); // -> 스레드 실행 톡방
        roomList.put(ROOMID++, chatClient);
    }

    public void join(int ROOMID, ChatClient chatClient) {
        // 사용자 key 값 - > chatClinet -> 배열로 관리 : 유저 정보 배열로 관리
        //chatClient.out == /join + roomID { list -> add }
        //list의 value끼리 broadcast 메시지 전송

        List<ChatClient> roomMember = (List<ChatClient>) roomList.get(ROOMID);
        if (roomMember == null) {
            System.out.println("방이 존재하지 않습니다.");
        }


    }


    //방 나간 후 리스트 삭제
    public void exit() {
        // ( 방번호가 < 1 ) -> 예외 처리
        ChatClient remove = roomList.remove(ROOMID--);

    }

    //방 목록 불러오기
    public void AllList() {
        for (Map.Entry<Integer, ChatClient> integerChatClientEntry : roomList.entrySet()) {
            System.out.println(integerChatClientEntry.getKey() + " : " + integerChatClientEntry.getValue());
        }
    }

}
