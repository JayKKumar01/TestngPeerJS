let peer = null;
let conn = null;

function init(userId) {
    peer = new Peer(userId, {
        port: 443,
        path: '/',
    });

    peer.on('open', () => {
        Android.send("my id: " + peer.id);
    });

    peer.on('connection', handleConnection);

    listen(peer); // Start listening for incoming calls
}

function handleConnection(connection) {
    conn = connection;
    conn.on('data', handleData);
    Android.send("Connected: " + connection.peer);
}

function connect(otherId) {
    conn = peer.connect(otherId);
    conn.on('data', handleData);
    Android.send("Connected: " + conn.peer);

    call(peer, otherId); // Initiate a call to the other peer
}

function handleData(data) {
    Android.play(data); // Process the 'data' parameter using Android.play
}

function sendFile(bytes) {
    if (conn && conn.open) {
        conn.send(bytes);
    }
}

function call(peer, otherId) {
    navigator.mediaDevices.getUserMedia({ audio: true, video: false })
        .then((stream) => {
            const call = peer.call(otherId, stream);
            call.on('stream', (remoteStream) => {
                Android.send("call connected");
            });
        })
        .catch((error) => {
            Android.send('Failed to get local stream: ');
        });
}

function listen(peer) {
    peer.on('call', (call) => {
        navigator.mediaDevices.getUserMedia({
            audio: true,
            video: false
        }).then((stream) => {
            call.answer(stream);
            call.on('stream', (remoteStream) => {
                Android.send("call connected");
            });
        }).catch((error) => {
            Android.send('Failed to get local stream: ');
        });
    });
}
