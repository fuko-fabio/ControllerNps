package com.nps.micro;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;

/**
 * @author Norbert Pabian
 * www.npsoftware.pl
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
        builder.setTitle(R.string.usb_not_opened)
        	   .setMessage(R.string.usb_not_opened_info)
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
                	   //activity.openCommunication();
                   }
               })
               .setNegativeButton(R.string.close, new DialogInterface.OnClickListener() {
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
