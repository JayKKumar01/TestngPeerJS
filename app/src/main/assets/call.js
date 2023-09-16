let peer1;
let conn1;
let peer2;
let conn2;
let conn;


function init(userId) {
    peer1 = new Peer(userId+"peer1", {
        port: 443,
        path: '/',
    });

    peer1.on('open', () => {
        Android.send("my id: " + userId+"peer1");
    });

    peer1.on('connection', (connection) => {

        connection.on('data', (data) => {
            Android.play(data); // Pass the 'data' parameter to Android.play
        });
        Android.send("Connected: " + connection.peer);
    });

/////////////////////////////////////////////////////////////////////

    peer2 = new Peer(userId+"peer2", {
            port: 443,
            path: '/',
    });

    peer2.on('open', () => {
            Android.send("my id: " + userId+"peer2");
    });

        peer2.on('connection', (connection) => {
            conn = connection;
            Android.send("Connected: " + connection.peer);
        });

}

function connect(otherId) {
    conn1 = peer1.connect(otherId+"peer1");
    conn = conn1;
    Android.send("Connected: " + conn1.peer);





    ///////////////////////////////////////////////////////////////////


    conn2 = peer2.connect(otherId+"peer2");
        conn2.on('data', (data) => {
            Android.play(data); // Pass the 'data' parameter to Android.play
        });
        Android.send("Connected: " + conn2.peer);
}


function sendFile(bytes) {
    conn.send(bytes);
}
