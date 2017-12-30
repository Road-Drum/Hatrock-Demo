package org.ayylmao.hatrock;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;


import java.util.UUID;

public class ActivityFragment extends Fragment {

    private static final String RPI_MAC = "B8:27:EB:1E:A1:F5";

    private static final String BLE_SERVICE_UUID = "13333333-3333-3333-3333-333333333337";

    private static final String BLE_CHARACTERISTICS_UUID = "13333333-3333-3333-3333-333333330001";
    private BluetoothManager blueMan;
    private BluetoothAdapter blueAdapt;
    private BluetoothDevice blueDev;
    private BluetoothGatt blueGatt;
    private BluetoothGattCharacteristic gattChar;
    private EditText editText;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        blueMan = (BluetoothManager)getActivity().getSystemService(Context.BLUETOOTH_SERVICE);
        blueAdapt = blueMan.getAdapter();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.activity_fragment, container, false);
        editText = (EditText)view.findViewById(R.id.editText);
        view.findViewById(R.id.buttonConnect).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                blueDev = blueAdapt.getRemoteDevice(RPI_MAC);
                blueGatt = blueDev.connectGatt(getActivity(), true, new BluetoothGattCallback() {
                    @Override
                    public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
                        super.onConnectionStateChange(gatt, status, newState);
                        if (newState == BluetoothProfile.STATE_CONNECTED) {
                            gatt.discoverServices();
                            Toast.makeText(getActivity(), "Device connected",
                                    Toast.LENGTH_LONG).show();
                        }
                    }
                    @Override
                    public void onServicesDiscovered(BluetoothGatt gatt, int status) {
                        super.onServicesDiscovered(gatt, status);
                        gattChar = gatt.getService(UUID.fromString(BLE_SERVICE_UUID))
                                .getCharacteristic(UUID.fromString(BLE_CHARACTERISTICS_UUID));
                        blueGatt = gatt;
                    }
                    @Override
                    public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
                        super.onCharacteristicRead(gatt, characteristic, status);
                        final byte[] values = characteristic.getValue();
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                // only 1 byte of data is expected
                                editText.setText(""+values[0]);
                            }
                        });
                    }
                });
            }
        });
        view.findViewById(R.id.buttonRead).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                blueGatt.readCharacteristic(gattChar);
            }
        });
        view.findViewById(R.id.buttonWrite).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                byte value = (byte)Integer.parseInt(editText.getText().toString());
                gattChar.setValue(new byte[]{value});
                blueGatt.writeCharacteristic(gattChar);
            }
        });
        return view;
    }
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        blueGatt.disconnect();
    }
}
