 /**
 * Copyright 2020 ArcSoft Corporation Limited. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is
 * distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See
 * the License for the specific language governing permissions and limitations under the License.
 */

package com.arcsoft.arcfacesingle.util.business;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;

import com.arcsoft.arcfacesingle.broadcast.UsbReceiver;
import com.github.mjdev.libaums.UsbMassStorageDevice;
import com.github.mjdev.libaums.fs.FileSystem;
import com.github.mjdev.libaums.fs.UsbFile;
import com.github.mjdev.libaums.partition.Partition;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class UsbHelper {

    private static String TAG = UsbHelper.class.getSimpleName();
    private static volatile UsbHelper instance;

    private Context context;
    private UsbMassStorageDevice[] storageDevices;
    private UsbReceiver mUsbReceiver;
    private UsbReceiver.UsbListener usbListener;
    private UsbFile currentFolder = null;
    private FileSystem fileSystem;

    public UsbHelper() {
    }

    public static UsbHelper getInstance() {
        if (instance == null) {
            synchronized (UsbHelper.class) {
                if (instance == null) {
                    instance = new UsbHelper();
                }
            }
        }
        return instance;
    }

    public void init(Context context, UsbReceiver.UsbListener usbListener) {
        this.context = context;
        this.usbListener = usbListener;
        //注册广播
        registerReceiver();
    }

    /**
     * 注册 USB 监听广播
     */
    private void registerReceiver() {
        mUsbReceiver = new UsbReceiver();
        mUsbReceiver.setUsbListener(usbListener);
        //监听otg插入 拔出
        IntentFilter usbDeviceStateFilter = new IntentFilter();
        usbDeviceStateFilter.addAction(UsbManager.ACTION_USB_DEVICE_ATTACHED);
        usbDeviceStateFilter.addAction(UsbManager.ACTION_USB_DEVICE_DETACHED);
        context.registerReceiver(mUsbReceiver, usbDeviceStateFilter);
        //注册监听自定义广播
        IntentFilter filter = new IntentFilter(UsbReceiver.ACTION_USB_PERMISSION);
        context.registerReceiver(mUsbReceiver, filter);
    }

    public void readUsbDiskDevList(UsbHelperCallback callback) {
        UsbManager usbManager = (UsbManager) context.getSystemService(Context.USB_SERVICE);
        storageDevices = UsbMassStorageDevice.getMassStorageDevices(context);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0,
                new Intent(UsbReceiver.ACTION_USB_PERMISSION), 0);
        if (usbManager != null && storageDevices.length > 0) {
            for (UsbMassStorageDevice device : storageDevices) {
                if (usbManager.hasPermission(device.getUsbDevice())) {
                    boolean hasPermission = setUpDevice(device);
                    if (callback != null) {
                        callback.requestPermission(hasPermission);
                    }
                } else {
                    usbManager.requestPermission(device.getUsbDevice(), pendingIntent);
                }
            }
        } else {
            if (callback != null) {
                callback.onError("");
            }
        }
    }

    public boolean setUpDevice(UsbMassStorageDevice device) {
        try {
            Thread.sleep(1500);
            device.init();
            Partition partition = device.getPartitions().get(0);
            fileSystem = partition.getFileSystem();
            currentFolder = fileSystem.getRootDirectory();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public UsbMassStorageDevice getUsbMass(UsbDevice usbDevice) {
        for (UsbMassStorageDevice device : storageDevices) {
            if (usbDevice.equals(device.getUsbDevice())) {
                return device;
            }
        }
        return null;
    }

    public UsbFile getRootFolder() {
        return currentFolder;
    }

    public FileSystem getFileSystem() {
        return fileSystem;
    }

    /**
     * 获取device 根目录文件
     *
     * @return 设备根目录下文件列表
     */
    public List<UsbFile> readFilesFromDevice() {
        List<UsbFile> usbFiles = new ArrayList<>();
        try {
            Collections.addAll(usbFiles, currentFolder.listFiles());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return usbFiles;
    }

    public void unInit() {
        if (storageDevices != null) {
            for (UsbMassStorageDevice device : storageDevices) {
                if (device != null) {
                    device.close();
                }
            }
            storageDevices = null;
        }
        fileSystem = null;
        currentFolder = null;
        mUsbReceiver = null;
    }

    public void unRegisterReceiver() {
        context.unregisterReceiver(mUsbReceiver);
    }

    public interface UsbHelperCallback {

        /**
         * 请求权限
         * @param hasPermission true 有权限；false 无权限
         */
        void requestPermission(boolean hasPermission);

        /**
         * 处理失败
         * @param message 失败信息
         */
        void onError(String message);
    }
}
