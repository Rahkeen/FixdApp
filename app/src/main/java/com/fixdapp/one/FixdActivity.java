package com.fixdapp.one;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Set;
import java.util.Timer;
import java.util.UUID;
import java.util.logging.Logger;

import android.app.Activity;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.client.Firebase;
import com.fixdapp.one.io.DatabaseUtility;
import com.fixdapp.one.io.FixdDatabase;
import com.fixdapp.one.requests.DTCRequest;
import com.fixdapp.one.requests.DisplayProtocolRequest;
import com.fixdapp.one.requests.EngineLightRequest;
import com.fixdapp.one.requests.HeadersOffRequest;
import com.fixdapp.one.requests.LineFeedOffRequest;
import com.fixdapp.one.requests.ResetRequest;
import com.fixdapp.one.requests.SetProtocolRequest;

import pt.lighthouselabs.obd.commands.control.TroubleCodesObdCommand;


/**
 *
 * Main controller for Fixd App. Currently handles Bluetooth Data connection,
 * as well as switching to different info screens when necessary. Will be the hub
 * of any display information
 *
 * @author Rikin Marfatia (rikin@fixdapp.com)
 *
 */
public class FixdActivity extends Activity {

    private static final int REQUEST_ENABLE_BT = 1;
    private static final int REQUEST_PAIR_DEVICE = 2;
    private static final UUID MY_UUID =
            UUID.fromString("00001101-0000-1000-8000-00805F9B34FB"); // well known SPP UUID
    private static final String OBDII = "OBDII";
    private static final String DB_PATH = "data/data/com.fixd.app/databases/";
    private static final String DB_NAME = "dtc.db";
    private static final String THREAT_YELLOW = "yellow";
    private static final String THREAT_RED = "red";
    private static final String THREAT_GREEN = "green";
    private static final String NO_CODE = "This code is not in our database," +
            "Please send an email to team@fixdapp.com with the code and we will " +
            "try and add it.";
    private static boolean isConnected;
    public static FixdDatabase fixdDb = null;
    public static SQLiteDatabase dtcDb = null;

    private BluetoothAdapter btAdapter = null;
    private BluetoothSocket socket = null;
    private OutputStream outStream = null;
    private InputStream inStream = null;
    private ImageView statusCircle;
    private TextView responseView, headerView, engineStatus;
    private Button checkEngine;
    private DTCRequest dtcReq;
    private EngineLightRequest milReq;
    private Timer taskTimer; // Messing around with this for recurring tasks TODO (rikin) : See if this is best way
    private String userName, pulledDTC, pulledInfo, threatLevel;
    private Firebase firebaseRef;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fixd);

        isConnected = false;
        taskTimer = new Timer();

        userName = getIntent().getStringExtra("USERNAME");
        pulledDTC = "No DTC";
        pulledInfo = "Check if you are connected";
        threatLevel = "gray";

        Firebase.setAndroidContext(this);
        firebaseRef = new Firebase("https://fixdapp.firebaseIO.com/");
        firebaseRef.child("user").setValue(userName);

        // TODO(rikin): move this to styling / create custom UI with this text standard
        // This is just a font test
        Typeface font = Typeface.createFromAsset(getAssets(), "fonts/Exo-Regular.otf");

        findViews();

        headerView.setTypeface(font);
        responseView.setTypeface(font);
        engineStatus.setTypeface(font);
        //checkEngine.setOnClickListener(new ButtonClicked());
        statusCircle.setOnClickListener(new ButtonClicked());
        btAdapter = BluetoothAdapter.getDefaultAdapter();

//        SQLiteDatabase.deleteDatabase(new File(DB_PATH + DB_NAME));

        connectionChecks();

    }

    public void findViews() {
//        checkEngine = (Button) findViewById(R.id.check_engine_button);
//        checkEngine.setEnabled(false);
        statusCircle = (ImageView) findViewById(R.id.status_circle);
        responseView = (TextView) findViewById(R.id.response_view);
        headerView = (TextView) findViewById(R.id.connected_header);
        headerView.setText(userName);
        engineStatus = (TextView) findViewById(R.id.engine_status);
    }

    public void connectionChecks() {
        checkBTState();
        checkPairedDevices();
        createDbIfNecessary();
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onStop() {
        super.onStop();
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == REQUEST_PAIR_DEVICE || requestCode == REQUEST_ENABLE_BT) {
            checkPairedDevices();
        }

    }

    //--------------------------- UI HANDLING ----------------------------------------------------------

    /**
     * Currently handles the "Check Engine" button, however this will be removed
     * eventually
     */
    private class ButtonClicked implements OnClickListener {

        @Override
        public void onClick(View v) {
            switch(v.getId()) {
//                case R.id.check_engine_button :
//                    // when Check Engine button is clicked, send the request code
//                    new SendDTCRequest().execute();
//                    break;
                case R.id.status_circle :
                    // Intent to TroubleInfo
                    Intent i = new Intent(FixdActivity.this, TroubleInfo.class);
                    i.putExtra("DTC", pulledDTC);
                    i.putExtra("INFO", pulledInfo);
                    i.putExtra("COLOR", threatLevel);
                    startActivity(i);
                default :
                    break;
            }
        }
    }

    //----------------------------------------- CONNECTION -------------------------------------------

    /**
     * Creates the data connection between the phone and the OBDII device, to allow for requests
     * for data.
     */
    private class ConnectTask extends AsyncTask<String, Void, String> {

        String deviceName = "";
        ProgressDialog connProgress;
        private ConnectTask(String deviceName) {
            this.deviceName = deviceName;
        }

        @Override
        protected void onPreExecute() {
            connProgress = ProgressDialog.show(FixdActivity.this, "Connection Status",
                    "Connecting...");
        }

        @Override
        protected String doInBackground(String... params) {
            String message = "";
            BluetoothDevice server = null;

            // Cancel discovery if it is running, as it is an intensive task
            btAdapter.cancelDiscovery();

            // Check if the device is paired, to ensure we can make a connection
            Set<BluetoothDevice> pairedDevices = btAdapter.getBondedDevices();
            for(BluetoothDevice device : pairedDevices) {
                if(device.getName().equals(deviceName)) {
                    server = device;
                }
            }

            // if it is paired attempt data connection, make sure not already
            // connected

            if(server != null && socket == null) {
                try {
                    socket = server.createRfcommSocketToServiceRecord(MY_UUID);
                } catch(IOException e) {
                    e.printStackTrace();
                }

                if(socket != null) {
                    try {
                        socket.connect();
                        inStream = socket.getInputStream();
                        outStream = socket.getOutputStream();
                        message = "Connection Established.";
                        isConnected = true;
                    } catch(Exception connect) {
                        connect.printStackTrace();
                        message = "Could not connect to the device. Make sure it is plugged in"
                                + " and your car is on.";
                        try {
                            socket.close();
                        } catch(IOException close) {
                            close.printStackTrace();
                            message = "Error closing the connection socket.";
                        }
                    }
                }
            }
            else if(server == null) {
                message = "Couldn't find device. Make sure you are paired with it!";
            }

            return message;
        }

        @Override
        protected void onPostExecute(String result) {
            connProgress.dismiss();
            toastMaker(result);
            if(isConnected) {
                //checkEngine.setEnabled(true);
                new SetupTasks().execute();
            } else {
                responseView.setText("Not Connected.");
                statusCircle.setImageResource(R.drawable.red_round);
            }
        }
    }


    private class SetupTasks extends AsyncTask<String, Void, String> {

        ResetRequest resReq = new ResetRequest(inStream, outStream);
        SetProtocolRequest spReq = new SetProtocolRequest(inStream, outStream);
        HeadersOffRequest headOffReq = new HeadersOffRequest(inStream, outStream);
        LineFeedOffRequest lineFeedOffReq = new LineFeedOffRequest(inStream, outStream);
        DisplayProtocolRequest dpReq = new DisplayProtocolRequest(inStream, outStream);

        ProgressDialog setupProgress;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            setupProgress = ProgressDialog.show(FixdActivity.this,
                    "Setup", "Setting up some things...");
        }

        @Override
        protected String doInBackground(String... params) {
            String result = "";

            resReq.sendMessage();
            resReq.readResult();

            spReq.sendMessage();
            spReq.readResult();

            headOffReq.sendMessage();
            headOffReq.readResult();

            lineFeedOffReq.sendMessage();
            lineFeedOffReq.readResult();

            dpReq.sendMessage();
            dpReq.readResult();

            result = dpReq.rawResult();

            return result;
        }

        @Override
        protected void onPostExecute(String result) {
            setupProgress.dismiss();
            new SendDTCRequest().execute();
        }
    }


    /**
     * Requests for the latest stored DTC
     */
    private class SendDTCRequest extends AsyncTask<String, Void, String> {

        TroubleCodesObdCommand troubleCodes = new TroubleCodesObdCommand();
        String rawResult = "";
        @Override
        protected String doInBackground(String... params) {
            String result = "";

            try {
                troubleCodes.run(inStream, outStream);
                rawResult = troubleCodes.getResult();
                result = troubleCodes.getFormattedResult();

            } catch (Exception e) {
                e.printStackTrace();
                result = "None";
            }

            return result;
        }

        @Override
        protected void onPostExecute(String result) {

            if(!rawResult.contains("SEARCHING")) {

                String[] multipleCodes = null;
                String firstResult = null;
                int numCodes = 0;
                if (result.contains("\n")) {
                    multipleCodes = result.split("\n");
                    numCodes = 1;
                    firstResult = multipleCodes[0];
                } else {
                    firstResult = result;
                }

                if (result.equalsIgnoreCase("None")) { // U1ATA is the parsed when receives NODATA

                    threatLevel = "green";
                    pulledDTC = "There are no problems";
                    pulledInfo = "";

                    responseView.setText("Good to Go");

                    firebaseRef.child("DTC").setValue(pulledDTC);

                } else {

                    // TODO (rikin) : check if rawQuery has any downsides
                    Cursor c = dtcDb.rawQuery("select * from DTC where _id = ?", new String[]{firstResult});

                    if (c != null && c.moveToFirst()) {
                        pulledDTC = c.getString(c.getColumnIndex(FixdDatabase.DtcCategories.TECHNICAL_DESCRIPTION));
                        pulledInfo = c.getString(c.getColumnIndex(FixdDatabase.DtcCategories.CONSEQUENCES));
                        threatLevel = c.getString(c.getColumnIndex(FixdDatabase.DtcCategories.THREAT_LEVEL));
                        responseView.setText("Vehicle Problems: " + numCodes);

                        if (THREAT_YELLOW.equalsIgnoreCase(threatLevel)) {
                            statusCircle.setImageResource(R.drawable.yellow_round);
                        } else if (THREAT_RED.equalsIgnoreCase(threatLevel)) {
                            statusCircle.setImageResource(R.drawable.red_round);
                        }

                    } else {
                        responseView.setText("Code Not Found");
                        pulledDTC = firstResult;
                        pulledInfo = NO_CODE;
                    }
                }
            } else {
                new SendDTCRequest().execute();
            }
        }

    }

//----------------------------------------- UTILITY METHODS ----------------------------------------------------	

    /**
     * Checks to see if a device supports bluetooth, and if it is,
     * turn it on if it is disabled
     */
    private void checkBTState() {

        if(btAdapter==null) {
            toastMaker("Device doesn't support bluetooth.");
        } else {
            if (!btAdapter.isEnabled()) {
                //Prompt user to turn on Bluetooth
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
            }
        }
    }

    /**
     * Checks to see if you are currently paired with the OBDII
     */
    private void checkPairedDevices() {
        Set<BluetoothDevice> pairedDevices = btAdapter.getBondedDevices();
        if(pairedDevices.size() > 0) {
            boolean paired = false;
            for(BluetoothDevice dev : pairedDevices) {
                if(dev.getName().equals(OBDII)) {
                    paired = true;
                    new ConnectTask(OBDII).execute();
                    break;
                }
            }

            if(!paired) {
                Intent scanIntent = new Intent(this, DeviceListService.class);
                startActivityForResult(scanIntent, REQUEST_PAIR_DEVICE);
            }
        }
    }

    /**
     * Creates the DTC Database on the phone if not already created
     */
    private void createDbIfNecessary() {
        if(!DatabaseUtility.isThereDB()) {
            // toastMaker("Database not found!");
            fixdDb = new FixdDatabase(this);
            dtcDb = fixdDb.getDatabase();
        } else {
            // toastMaker("Database found");
            dtcDb = SQLiteDatabase.openDatabase(DB_PATH + DB_NAME, null, SQLiteDatabase.OPEN_READWRITE);
        }
    }

    /**
     * Simple utility for making Toasts
     * @param message the message used in the Toast
     */
    private void toastMaker(String message) {
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }

}