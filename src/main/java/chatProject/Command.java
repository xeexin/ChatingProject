/*
package chatProject;

import java.util.ArrayList;
import java.util.List;

public class Command { // 명령어를 수행하는 클래스


    private int id;
    private List<ChatThread> chatClients;

    public Command(){
        chatClients = new ArrayList<>();
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    //방 생성
    public void createRoom(ChatThread chatThread) {

        System.out.println("방이 생성되었습니다. 방 ID : " + id);
        chatClients.add(chatThread);

    }

    //방 입장
    public void joinRoom() {

    }

    //방 목록
    public void listRoom() {
        System.out.println("방 목록 : ");
        for (ChatThread chatClient : chatClients) {
            System.out.println(chatClient.getId());
        }
    }

    //방나가기
    public void exitRoom(ChatThread chatThread) {
        chatClients.remove(chatThread);
    }
}
*/
