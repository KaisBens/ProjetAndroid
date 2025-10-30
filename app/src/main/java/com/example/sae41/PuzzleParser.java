package com.example.sae41;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import android.util.Xml;
import java.io.InputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class PuzzleParser {
    public static Puzzle parsePuzzle(InputStream inputStream, String fileName) {
        int size = 0;
        String name = "";
        List<Point[]> pairs = new ArrayList<Point[]>();
        boolean valid = true;
        try {
            XmlPullParser parser = Xml.newPullParser();
            parser.setInput(inputStream, null);
            int eventType = parser.getEventType();
            Point firstPoint = null;
            while (eventType != XmlPullParser.END_DOCUMENT) {
                String tag = parser.getName();
                if (eventType == XmlPullParser.START_TAG) {
                    if ("puzzle".equals(tag)) {
                        String sizeStr = parser.getAttributeValue(null, "size");
                        if (sizeStr != null) {
                            size = Integer.parseInt(sizeStr);
                        } else {
                            valid = false;
                        }
                        name = parser.getAttributeValue(null, "nom");
                        if (name == null) {
                            // Utiliser le nom du fichier (sans extension) si l'attribut est absent
                            int dotIndex = fileName.lastIndexOf('.');
                            name = (dotIndex > 0) ? fileName.substring(0, dotIndex) : fileName;
                        }
                        if (size < 5 || size > 14) {
                            valid = false;
                        }
                    } else if ("point".equals(tag)) {
                        String colStr = parser.getAttributeValue(null, "colonne");
                        String ligneStr = parser.getAttributeValue(null, "ligne");
                        if (colStr == null || ligneStr == null) {
                            valid = false;
                        } else {
                            int col = Integer.parseInt(colStr);
                            int ligne = Integer.parseInt(ligneStr);
                            if (col < 0 || col >= size || ligne < 0 || ligne >= size) {
                                valid = false;
                            }
                            Point pt = new Point(col, ligne);
                            if (firstPoint == null) {
                                firstPoint = pt;
                            } else {
                                pairs.add(new Point[]{firstPoint, pt});
                                firstPoint = null;
                            }
                        }
                    }
                }
                eventType = parser.next();
            }
        } catch (XmlPullParserException | IOException e) {
            valid = false;
        }
        return new Puzzle(size, name, pairs, valid, fileName);
    }
}
