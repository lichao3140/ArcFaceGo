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

package com.arcsoft.arcfacesingle.broadcast;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;

public class UsbReceiver extends BroadcastReceiver {

    private static final String TAG = UsbReceiver.class.getSimpleName();
    private static final String USB_DEVICE_ATTACHED = "android.hardware.usb.action.USB_DEVICE_ATTACHED";
    private static final String USB_DEVICE_DETACHED = "android.hardware.usb.action.USB_DEVICE_DETACHED";
    public static final String ACTION_USB_PERMISSION = "com.arcsoft.arcfacesingle.USB_PERMISSION";

    private UsbListener usbListener;

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        switch (action) {
            case ACTION_USB_PERMISSION:
                //接受到自定义广播
                UsbDevice usbDevice = intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
                if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
                    //允许权限申请
                    if (usbDevice != null) {
                        //回调
                        if (usbListener != null) {
                            usbListener.getReadUsbPermission(usbDevice);
                        }
                    }
                } else {
                    if (usbListener != null) {
                        usbListener.readFailedUsb(usbDevice);
                    }
                }
                break;
            case UsbManager.ACTION_USB_DEVICE_ATTACHED:
                //接收到存储设备插入广播
                UsbDevice deviceAdd = intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
                if (deviceAdd != null) {
                    if (usbListener != null) {
                        usbListener.insertUsb(deviceAdd);
                    }
                }
                break;
            case UsbManager.ACTION_USB_DEVICE_DETACHED:
                //接收到存储设备拔出广播
                UsbDevice deviceRemove = intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
                if (deviceRemove != null) {
                    if (usbListener != null) {
                        usbListener.removeUsb(deviceRemove);
                    }
                }
                break;
            default:
                break;
        }
    }

    public void setUsbListener(UsbListener usbListener) {
        this.usbListener = usbListener;
    }

    /**
     * USB 操作监听
     */
    public interface UsbListener {

        /**
         * u盘插入
         * @param deviceAdd UsbDevice信息
         */
        void insertUsb(UsbDevice deviceAdd);

        /**
         * u盘移除
         * @param deviceRemove UsbDevice信息
         */
        void removeUsb(UsbDevice deviceRemove);

        /**
         * 获取Upan权限
         * @param usbDevice UsbDevice信息
         */
        void getReadUsbPermission(UsbDevice usbDevice);

        /**
         * U盘读取失败
         * @param usbDevice UsbDevice信息
         */
        void readFailedUsb(UsbDevice usbDevice);
    }
}
