package com.metar.browser.utils;

import android.app.Activity;
import android.content.Intent;

public class Utility {
    public static String normalizeResponse(String response) {
        response = response.replaceAll("\n", "");
        response = response.trim();
        response = response.substring(response.indexOf("<table>") + 7, response.indexOf("</table>"));
        response = response.replaceAll("<tr>", "");
        response = response.replaceAll("</tr>", "");
        response = response.replaceAll("<hr>", "");
        response = response.replaceAll("</hr>", "");
        response = response.replaceAll("<td>&nbsp;</td>", "");
        response = response.replaceAll("\n", "");
        return response;
    }

    public static void launchActivity(Activity activity, Class classToLaunch, boolean finishParent) {
        activity.startActivity(new Intent(activity, classToLaunch));
        if (finishParent) {
            activity.finish();
        }
    }
}
