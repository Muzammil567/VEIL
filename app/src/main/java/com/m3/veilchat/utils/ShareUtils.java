package com.m3.veilchat.utils;

import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import com.m3.veilchat.R;

public class ShareUtils {

    public static void shareApp(Context context) {
        String appUrl = context.getString(R.string.share_app_url);
        String shareText = context.getString(R.string.share_app_text, appUrl);

        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TEXT, shareText);
        shareIntent.putExtra(Intent.EXTRA_SUBJECT, context.getString(R.string.share_app_subject));

        context.startActivity(Intent.createChooser(shareIntent, context.getString(R.string.share_app_chooser_title)));
    }

    public static void shareToWhatsApp(Context context) {
        String appUrl = context.getString(R.string.share_app_url);
        String shareText = "Hey! Join me on VEIL - The most private anonymous chat app! " +
                "No phone numbers, no emails, just pure privacy. " +
                "Download now: " + appUrl +
                "\n\nOnce you install it, we can chat anonymously with different personas!";

        Intent whatsappIntent = new Intent(Intent.ACTION_SEND);
        whatsappIntent.setType("text/plain");
        whatsappIntent.setPackage("com.whatsapp");
        whatsappIntent.putExtra(Intent.EXTRA_TEXT, shareText);

        try {
            context.startActivity(whatsappIntent);
        } catch (android.content.ActivityNotFoundException ex) {
            Toast.makeText(context, R.string.whatsapp_not_installed, Toast.LENGTH_SHORT).show();
            shareApp(context);
        }
    }

    public static void shareToTelegram(Context context) {
        String appUrl = context.getString(R.string.share_app_url);
        String shareText = "Join me on VEIL - The most private anonymous chat app! " +
                "Download: " + appUrl;

        Intent telegramIntent = new Intent(Intent.ACTION_SEND);
        telegramIntent.setType("text/plain");
        telegramIntent.setPackage("org.telegram.messenger");
        telegramIntent.putExtra(Intent.EXTRA_TEXT, shareText);

        try {
            context.startActivity(telegramIntent);
        } catch (android.content.ActivityNotFoundException ex) {
            Toast.makeText(context, R.string.telegram_not_installed, Toast.LENGTH_SHORT).show();
            shareApp(context);
        }
    }

    public static void shareUsername(Context context, String username) {
        String appUrl = context.getString(R.string.share_app_url);
        String shareText = context.getString(R.string.share_username_text, username, appUrl);

        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TEXT, shareText);
        shareIntent.putExtra(Intent.EXTRA_SUBJECT, context.getString(R.string.share_username_subject));

        context.startActivity(Intent.createChooser(shareIntent, context.getString(R.string.share_username_chooser_title)));
    }

    public static void shareRoomInvite(Context context, String roomId, String roomName) {
        String appUrl = context.getString(R.string.share_app_url);
        String shareText = context.getString(R.string.share_room_invite_text, roomName, roomId, appUrl);

        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TEXT, shareText);
        shareIntent.putExtra(Intent.EXTRA_SUBJECT, context.getString(R.string.share_room_invite_subject));

        context.startActivity(Intent.createChooser(shareIntent, context.getString(R.string.share_room_invite_chooser_title)));
    }
}
