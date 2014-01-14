/*******************************************************************************
 * Copyright 2014 Norbert Pabian.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * imitations under the License.
 ******************************************************************************/
package com.nps.micro.view;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;

import com.nps.micro.R;

/**
 * @author Norbert Pabian
 * www.npsoft.clanteam.com
 */
public class Dialogs {

    private Dialogs() {
    }

    public static Dialog getUsbDeviceNotFoundDialog(final Activity activity) {
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setTitle(R.string.usb_not_found)
               .setMessage(R.string.usb_not_found_info)
               .setPositiveButton(R.string.close, new DialogInterface.OnClickListener() {
                   public void onClick(DialogInterface dialog, int id) {
                       activity.finish();
                   }
               });
        return builder.create();
    }

    public static Dialog getCannotCreateUsbGateDialog(final Activity activity, String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setTitle(R.string.usb_not_opened).setMessage(R.string.usb_not_opened_info)
                .setMessage(message)
                .setPositiveButton(R.string.close, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        activity.finish();
                    }
                });
        return builder.create();
    }

    public static Dialog getCannotOpenUsbConnectionDialog(final Activity activity, String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setTitle(R.string.usb_not_connected)
                .setMessage(R.string.usb_not_connected_info)
                .setMessage(message)
                .setPositiveButton(R.string.try_again, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // activity.openCommunication();
                    }
                }).setNegativeButton(R.string.close, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        activity.finish();
                    }
                });
        return builder.create();
    }

    public static Dialog getDeviceDisconnectedDialog(final Activity activity) {
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setTitle(R.string.usb_disconnected)
                .setMessage(R.string.usb_disconnected_info)
                .setPositiveButton(R.string.close, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        activity.finish();
                    }
                });
        return builder.create();
    }

    public static Dialog getCannotSwitchToStreamDialog(final Activity activity, String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setTitle(R.string.usb_error_switch_to_stream)
               .setMessage(R.string.usb_error_switch_to_stream_info)
               .setMessage(message)
               .setPositiveButton(R.string.close, new DialogInterface.OnClickListener() {
                   public void onClick(DialogInterface dialog, int id) {
                       activity.finish();
                   }
               });
        return builder.create();
    }
}
