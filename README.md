# websocket_samp
웹소켓 통신을 위한 Sock, Stomp, Redis Pubsub을 활용한 샘플 프로젝트입니다.

```bash
.
└── src
    ├── main
    │   ├── java
    │   │   └── com
    │   │       └── example
    │   │           └── ccfe2
    │   │               ├── Ccfe2Application.java
    │   │               ├── config
    │   │               │   ├── RedisConfig.java
    │   │               │   ├── WebSocketConfig.java
    │   │               │   └── WebSocketEventListener.java
    │   │               ├── controller
    │   │               │   └── ChatController.java
    │   │               ├── model
    │   │               │   └── ChatMessage.java
    │   │               └── pubsub
    │   │                   ├── RedisPublisher.java
    │   │                   └── RedisSubscriber.java
    │   └── resources
    │       ├── application.yml
    │       └── static
    │           ├── css
    │           │   └── chatt.css
    │           ├── index.html
    │           └── js
    │               ├── app.js
    │               ├── jquery.min.js
    │               ├── sockjs.min.js
    │               └── stomp.min.js
    └── test
```
