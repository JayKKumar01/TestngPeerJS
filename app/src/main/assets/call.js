
let peer
let con

function init(userId) {
    peer = new Peer(userId, {
        port: 443,
        path: '/',
    })

    peer.on('open', () => {
        Android.send("my id: "+userId)
    })
    peer.on('connection', (conn) => {
    Android.send("Connected: "+conn.peer)
    Android.answer();
    con = conn
      conn.on('data', (data) => {
//      sendFile(data)
//        Android.receiveFile(data)
        Android.play(data)
      });
    });
}



function connect(otherId){
con = peer.connect(otherId);
con.on('data', (data) => {
Android.play(data)
//Android.receiveFile(data)
//        Android.send(data)
      });
Android.send("Connected: "+con.peer)
}

function send(msg){
con.send(msg)
}
function sendFile(bytes){
con.send(bytes)
}
