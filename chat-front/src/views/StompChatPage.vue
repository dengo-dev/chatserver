<template>
  <v-container>
    <v-row justify="center">
      <v-col cols="12" md="8">
        <v-card>
          <v-card-title class="text-center text-h5">
            채팅
          </v-card-title>
          <v-card-text>
            <div class="chat-box">
              <div
              v-for="(msg, index) in messages"
              :key="index"
              >
                {{msg}}
              </div>
            </div>
            <v-text-field
              v-model="newMessage"
              label="메시지 입력"
              @keyup.enter="sendMessage"

            />
              <v-btn color="primary" block @click="sendMessage">전송</v-btn>
          </v-card-text>

        </v-card>
      </v-col>

    </v-row>

  </v-container>
</template>


<script>
import SockJS from 'sockjs-client';
import Stomp from 'webstomp-client';
// import axios from 'axios';

export default {
  data(){
    return{
      messages: [],
      newMessage: "",
      stompClient: null,

    }
  },
  created(){
    this.connectWebSocket();
  },
  beforeUnmount() {
    this.disconnectWebSocket();
  },
  methods:{
    connectWebSocket(){
      // sockjs는 websocket을 내장한 향상된 js라이브러리 http엔드포인트 사용.
      const sockJs = new SockJS(`${process.env.VUE_APP_API_BASE_URL}/connect`)
      this.stompClient = Stomp.over(sockJs);

      this.stompClient.connect({},
          ()=>{
            this.stompClient.subscribe(`/topic/1`,(message)=>{
              console.log(message);
              this.messages.push(message.body);
              this.scrollToBottom();
            })
          }

      )
    },
    sendMessage(){
      if(this.newMessage.trim()==="")return;
      this.stompClient.send(`/publish/1`,this.newMessage);
      this.newMessage="";
    },
    scrollToBottom(){
      this.$nextTick(()=>{
        const chatBox = this.$el.querySelector(".chat-box");
        chatBox.scrollTop=chatBox.scrollHeight;
      })
    },
    disconnectWebSocket(){
      // if (this.ws){
      //   this.ws.close();
      //   console.log("disconnected!!")
      //   this.ws=null;
      // }
    }
  },
}

</script>
<style>
.chat-box{
  height: 300px;
  overflow-y: auto;
  border: 1px solid #ddd;
  margin-bottom: 10px;

}
</style>