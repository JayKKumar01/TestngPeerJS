let peer = null;
let conn = null;
let myId = null;

function init(userId) {
    peer = new Peer(userId, {
        port: 443,
        path: '/',
    });

    peer.on('open', () => {
        myId = peer.id;
        Android.send("my id: " + myId);
    });

    peer.on('connection', handleConnection);

    peer.on('close',function(){
        Android.onClose(peer.id);
    });
    peer.on('disconnected',function(){
        Android.send("user disconnected");
    })
}

function handleConnection(connection) {
    Android.onConnected();
    conn = connection;
    conn.on('data', handleData);
    Android.send("Connected: " + connection.peer);
}

function connect(otherId) {
    conn = peer.connect(otherId, { reliable: true });

    conn.on('open', () => {
        Android.onConnected();
        Android.send("Connected: " + conn.peer);
    });

    conn.on('data', handleData);
}


function handleData(data) {

//Android.showText(data);
    Android.play(data.id,data.bytes,data.read,data.millis); // Process the 'data' parameter using Android.play
}

function sendFile(bytes, read, millis) {

    var data = {
        id: myId,
        bytes: bytes,
        read: read,
        millis: millis
        };
//&& !peer.disconnected
    if (conn && conn.open) {
        conn.send(data);
    }

    
}

