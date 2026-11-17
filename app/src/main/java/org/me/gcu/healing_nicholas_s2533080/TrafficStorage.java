package org.me.gcu.healing_nicholas_s2533080;

import android.content.Context;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;

public class TrafficStorage {

    // Reading the saved XML from the app's internal storage
    public static String loadXml(Context context, String fileName)
            throws IOException {

        // Find the saved file in the app's private storage
        File f = new File(context.getFilesDir(), fileName);

        String xml = "";

        // Open the file and read its contents
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(new FileInputStream(f)))) {

            String line;

            while ((line = reader.readLine()) != null) {
                xml = xml + line;
            }
        }

        // Return the saved XML as a String
        return xml;
    }

    // Save downloaded XML into the app's internal storage
    public static void saveXml(Context context,
                               String fileName, String xml)
            throws IOException {

        // Find the location where the XML should be saved
        File f = new File(context.getFilesDir(), fileName);

        // Open the file and write the XML into it
        try (FileOutputStream fos = new FileOutputStream(f)) {
            fos.write(xml.getBytes());
        }
    }
}
