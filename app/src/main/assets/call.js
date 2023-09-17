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
}

function handleData(data) {
    Android.play(data); // Process the 'data' parameter using Android.play
}

function sendFile(bytes) {
    if (conn && conn.open) {
        conn.send(bytes);
    }
}

