package com.metar.browser.utils;

import android.app.Activity;
import android.content.Intent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import com.metar.browser.BuildConfig;

public class Utility {
    public static final String QUERY_STRING_EXTRA = "com.metar.query.string.extra";
    public static final String REQUEST_TYPE_GET = "GET";
    public static final int CONNECTION_READ_TIMEOUT = 30000;
    public static final int CONNECTION_TIMOUT = 20000;


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

    public static String getDecodedMessageUrl(String query) {
        return BuildConfig.METAR_DECODED_BASE_URL.concat(query).concat(".TXT");
    }

    public static String getRawMessageUrl(String query) {
        return BuildConfig.METAR_LIST_URL.concat(query).concat(".TXT");
    }

    public static void hideKeyboard(Activity activity) {
        InputMethodManager imm = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
        //Find the currently focused view, so we can grab the correct window token from it.
        View view = activity.getCurrentFocus();
        //If no view currently has focus, create a new one, just so we can grab a window token from it
        if (view == null) {
            view = new View(activity);
        }
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }
}
