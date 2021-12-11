packagerobotspace.simplelabs.amr_voice;

importandroid.app.Service; importandroid.bluetooth.BluetoothAdapter; importandroid.bluetooth.BluetoothDevice; importandroid.bluetooth.BluetoothServerSocket; importandroid.bluetooth.BluetoothSocket; importandroid.content.Context; importandroid.content.Intent; importandroid.os.Bundle; importandroid.os.Handler; importandroid.os.IBinder; importandroid.os.Message; importandroid.util.Log; importjava.io.IOException; importjava.io.InputStream; importjava.io.OutputStream; importjava.util.UUID;

publicclassBtServiceextends Service {

/* renamed from: D */ privatestaticfinalboolean f1D = true;
/* access modifiers changed from: private */
publicstaticfinal UUID MY_UUID_SECURE = UUID.fromString("00001101- 0000-1000-8000-00805f9b34fb");
privatestaticfinal String NAME_SECURE = "BluetoothBTSecure"; publicstaticfinalint STATE_CONNECTED = 3;
publicstaticfinalint STATE_CONNECTING = 2; publicstaticfinalint STATE_LISTEN = 1; publicstaticfinalint STATE_NONE = 0; privatestaticfinal String TAG = "BtService";
/* access modifiers changed from: private */ publicfinalBluetoothAdaptermAdapter = BluetoothAdapter.getDefaultAdapter();


 
/* access modifiers changed from: private */ publicConnectThreadmConnectThread; privateConnectedThreadmConnectedThread;
/* access modifiers changed from: private */ publicfinal Handler mHandler; privateAcceptThreadmSecureAcceptThread;
/* access modifiers changed from: private */ publicintmState = 0;

privateclassAcceptThreadextends Thread { private String mSocketType = "Secure";
privatefinalBluetoothServerSocketmmServerSocket;

publicAcceptThread(boolean secure) { BluetoothServerSockettmp = null;
if (secure) { try {
tmp = BtService.this.mAdapter.listenUsingRfcommWithServiceRecord(BtService.NAM E_SECURE, BtService.MY_UUID_SECURE);
} catch (IOException e) {
Log.e(BtService.TAG, "Socket Type: " + this.mSocketType + "listen() failed", e);
}
}
this.mmServerSocket = tmp;
}

publicvoidrun() {
Log.d(BtService.TAG, "Socket Type: " + this.mSocketType + "BEGIN mAcceptThread" + this);
setName("AcceptThread" + this.mSocketType); while (BtService.this.mState != 3) {
try {
BluetoothSocket socket = this.mmServerSocket.accept(); if (socket != null) {
synchronized (BtService.this) { switch (BtService.this.mState) { case 0:
case 3: try {

 
socket.close(); break;
} catch (IOException e) {
Log.e(BtService.TAG, "Could not close unwanted socket", e); break;
}
case 1:
case 2:
BtService.this.connected(socket, socket.getRemoteDevice(), this.mSocketType); break;
}
}
}
} catch (IOException e2) {
Log.e(BtService.TAG, "Socket Type: " + this.mSocketType + "accept() failed", e2);
}
}
Log.i(BtService.TAG, "END mAcceptThread, socket Type: " + this.mSocketType);
return;
}

publicvoidcancel() {
Log.d(BtService.TAG, "Socket Type" + this.mSocketType + "cancel " + this); try {
this.mmServerSocket.close();
} catch (IOException e) {
Log.e(BtService.TAG, "Socket Type" + this.mSocketType + "close() of server failed", e);
}
}
}

privateclassConnectThreadextends Thread { private String mSocketType = "Secure"; privatefinalBluetoothDevicemmDevice; privatefinalBluetoothSocketmmSocket;

publicConnectThread(BluetoothDevice device, boolean secure) {

 
this.mmDevice = device; BluetoothSockettmp = null; if (secure) {
try { tmp =
device.createRfcommSocketToServiceRecord(BtService.MY_UUID_SECURE);
} catch (IOException e) {
Log.e(BtService.TAG, "Socket Type: " + this.mSocketType + "create() failed", e);
}
}
this.mmSocket = tmp;
}

publicvoidrun() {
Log.i(BtService.TAG, "BEGIN mConnectThreadSocketType:" + this.mSocketType);
setName("ConnectThread" + this.mSocketType); BtService.this.mAdapter.cancelDiscovery();
try { this.mmSocket.connect();
synchronized (BtService.this) { BtService.this.mConnectThread = null;
}
BtService.this.connected(this.mmSocket, this.mmDevice, this.mSocketType);
} catch (IOException e) {
try { this.mmSocket.close();
} catch (IOException e2) {
Log.e(BtService.TAG, "unable to close() " + this.mSocketType + " socket during connection failure", e2);
}
BtService.this.connectionFailed();
}
}

publicvoidcancel() { try { this.mmSocket.close();
} catch (IOException e) {


 
Log.e(BtService.TAG, "close() of connect " + this.mSocketType + " socket failed", e);
}
}
}

privateclassConnectedThreadextends Thread { privatefinalInputStreammmInStream; privatefinalOutputStreammmOutStream; privatefinalBluetoothSocketmmSocket;

publicConnectedThread(BluetoothSocket socket, String socketType) { Log.d(BtService.TAG, "create ConnectedThread: " + socketType); this.mmSocket = socket;
InputStreamtmpIn = null; OutputStreamtmpOut = null; try {
tmpIn = socket.getInputStream(); tmpOut = socket.getOutputStream();
} catch (IOException e) { Log.e(BtService.TAG, "temp sockets not created", e);
}
this.mmInStream = tmpIn; this.mmOutStream = tmpOut;
}

publicvoidrun() {
Log.i(BtService.TAG, "BEGIN mConnectedThread"); byte[] buffer = newbyte[1024];
while (true) { try {
BtService.this.mHandler.obtainMessage(2, this.mmInStream.read(buffer), -1, buffer).sendToTarget();
} catch (IOException e) { Log.e(BtService.TAG, "disconnected", e); BtService.this.connectionLost(); BtService.this.start();
return;
}
 
}

publicvoid write(byte[] buffer) { try { this.mmOutStream.write(buffer);
BtService.this.mHandler.obtainMessage(3, -1, -1, buffer).sendToTarget();
} catch (IOException e) { Log.e(BtService.TAG, "Exception during write", e);
}
}

publicvoidcancel() { try { this.mmSocket.close();
} catch (IOException e) {
Log.e(BtService.TAG, "close() of connect socket failed", e);
}
}
}

publicBtService(Context context, Handler handler) { this.mHandler = handler;
}

privatesynchronizedvoidsetState(int state) { Log.d(TAG, "setState() " + this.mState + " -> " + state); this.mState = state;
this.mHandler.obtainMessage(1, state, -1).sendToTarget();
}

publicsynchronizedintgetState() { returnthis.mState;
}

publicsynchronizedvoidstart() { Log.d(TAG, "start");
if (this.mConnectThread != null) { this.mConnectThread.cancel(); this.mConnectThread = null;
 
if (this.mConnectedThread != null) { this.mConnectedThread.cancel(); this.mConnectedThread = null;
}
setState(1);
if (this.mSecureAcceptThread == null) { this.mSecureAcceptThread = newAcceptThread(f1D); this.mSecureAcceptThread.start();
}
}

publicsynchronizedvoidconnect(BluetoothDevice device, boolean secure) { Log.d(TAG, "connect to: " + device);
if (this.mState == 2 &&this.mConnectThread != null) { this.mConnectThread.cancel();
this.mConnectThread = null;
}
if (this.mConnectedThread != null) { this.mConnectedThread.cancel(); this.mConnectedThread = null;
}
this.mConnectThread = newConnectThread(device, secure); this.mConnectThread.start();
setState(2);
}

publicsynchronizedvoidconnected(BluetoothSocket socket, BluetoothDevice device, String socketType) {
Log.d(TAG, "connected, Socket Type:" + socketType); if (this.mConnectThread != null) { this.mConnectThread.cancel();
this.mConnectThread = null;
}
if (this.mConnectedThread != null) { this.mConnectedThread.cancel(); this.mConnectedThread = null;
}
if (this.mSecureAcceptThread != null) { this.mSecureAcceptThread.cancel(); this.mSecureAcceptThread = null;

 
}
this.mConnectedThread = newConnectedThread(socket, socketType); this.mConnectedThread.start();
Message msg = this.mHandler.obtainMessage(4); Bundle bundle = newBundle();
bundle.putString(VoiceControlActivity.DEVICE_NAME, device.getName()); msg.setData(bundle);
this.mHandler.sendMessage(msg); setState(3);
}

publicsynchronizedvoidstop() { Log.d(TAG, "stop");
if (this.mConnectThread != null) { this.mConnectThread.cancel(); this.mConnectThread = null;
}
if (this.mConnectedThread != null) { this.mConnectedThread.cancel(); this.mConnectedThread = null;
}
if (this.mSecureAcceptThread != null) { this.mSecureAcceptThread.cancel(); this.mSecureAcceptThread = null;
}
setState(0);
}

publicvoid write(byte[] out) { synchronized (this) {
if (this.mState == 3) {
ConnectedThread r = this.mConnectedThread; r.write(out);
}
}
}

/* access modifiers changed from: private */ publicvoidconnectionFailed() {
Message msg = this.mHandler.obtainMessage(5);

 
Bundle bundle = newBundle(); bundle.putString(VoiceControlActivity.TOAST, "Unable to connect device"); msg.setData(bundle);
this.mHandler.sendMessage(msg); start();
}

/* access modifiers changed from: private */ publicvoidconnectionLost() {
Message msg = this.mHandler.obtainMessage(5); Bundle bundle = newBundle();
bundle.putString(VoiceControlActivity.TOAST, "Device connection was lost"); msg.setData(bundle);
this.mHandler.sendMessage(msg); start();
}

publicIBinderonBind(Intent arg0) { returnnull;
}
}

