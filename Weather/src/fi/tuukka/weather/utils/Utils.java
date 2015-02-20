/*******************************************************************************
 * Copyright (C) 2015 Tuukka Ruhanen
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 *******************************************************************************/
package fi.tuukka.weather.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import fi.tuukka.weather.R;
import fi.tuukka.weather.R.id;
import fi.tuukka.weather.R.layout;
import android.app.Activity;
import android.app.Dialog;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;
import android.widget.LinearLayout;

public class Utils {

    public static enum Day {
        SU, MA, TI, KE, TO, PE, LA
    }

    public static enum CondCode {
        SELKEÄÄ("selkeää", 1),
        PUOLIPILVISTÄ("puolipilvistä", 2),
        PILVISTÄ("pilvistä", 3),
        HEIKKOJASADEKUUROJA("heikkoja sadekuuroja", 21),
        SADEKUUROJA("sadekuuroja", 22),
        RANKKOJASADEKUUROJA("rankkoja sadekuuroja", 23),
        HEIKKOASADETTA("heikkoa sadetta", 31),
        SADETTA("sadetta", 32),
        RUNSASTASADETTA("runsasta sadetta", 33),
        HEIKKOJALUMIKUUROJA("heikkoja lumikuuroja", 41),
        LUMIKUUROJA("lumikuuroja", 42),
        SAKEITALUMIKUUROJA("sakeita lumikuuroja", 43),
        HEIKKOALUMISADETTA("heikkoa lumisadetta", 51),
        LUMISADETTA("lumisadetta", 52),
        RUNSASTALUMISADETTA("runsasta lumisadetta", 53),
        UKKOSKUUROJA("ukkoskuuroja", 61),
        VOIMAKKAITAUKKOSKUUROJA("voimakkaita ukkoskuuroja", 62),
        UKKOSTA("ukkosta", 63),
        VOIMAKASTAUKKOSTA("voimakasta ukkosta", 64),
        HEIKKOJARÄNTÄKUUROJA("heikkoja räntäkuuroja", 71),
        RÄNTÄKUUROJA("räntäkuuroja", 72),
        VOIMAKKAITARÄNTÄKUUROJA("voimakkaita räntäkuuroja", 73),
        HEIKKOARÄNTÄSADETTA("heikkoa räntäsadetta", 81),
        RÄNTÄSADETTA("räntäsadetta", 82),
        RUNSASTARÄNTÄSADETTA("runsasta räntäsadetta", 83);

        final String name;
        final int code;

        CondCode(String name, int code) {
            this.name = name;
            this.code = code;
        }
    }

    public static SimpleDateFormat getDateFormatter() {
        return new SimpleDateFormat("d.M. HH:mm");
    }

    public static SimpleDateFormat getTimeFormatter() {
        return new SimpleDateFormat("HH:mm");
    }

    public static String downloadHtml(String url) throws IllegalStateException {
        HttpClient client = new DefaultHttpClient();
        HttpGet request = new HttpGet(url.toLowerCase());
        HttpResponse response = null;
        InputStream in = null;
        try {
            response = client.execute(request);
            in = response.getEntity().getContent();
            BufferedReader reader = new BufferedReader(new InputStreamReader(in));
            StringBuilder str = new StringBuilder();
            String line = null;
            line = reader.readLine();
            while (line != null) {
                str.append(line);

                try {
                    line = reader.readLine();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            in.close();

            return str.toString();
        } catch (ClientProtocolException e1) {
            e1.printStackTrace();
        } catch (IOException e1) {
            e1.printStackTrace();
        }

        return null;
    }

    // public static void requestRightForecastUrl(String note, final Tuorein tuorein) {
    // Context context = tuorein.getParent();
    // final Dialog dialog = new Dialog(context);
    // dialog.setContentView(R.layout.urldialog);
    // dialog.setTitle("Ennustetta ei voitu hakea");
    // TextView tv = (TextView) dialog.findViewById(R.id.textView2);
    // tv.setText(note);
    // final EditText input = (EditText) dialog.findViewById(R.id.editText1);
    // ((Button) dialog.findViewById(R.id.button1)).setOnClickListener(new OnClickListener() {
    // @Override
    // public void onClick(View v) {
    // tuorein.setNewForecastUrl(input.getText().toString());
    // dialog.dismiss();
    // }
    // });
    //
    // dialog.show();
    // }

    public static int getTempColor(int tempValue) {
        int r = 255;
        int g = 255;
        int b = 255;
        if (tempValue <= -30) {
            r = 0;
            g = 0;
            b = 255;
        }
        else if (tempValue > -30 && tempValue <= -15) {
            double c1 = 17.;
            double c2 = 510.;
            r = 0;
            g = (int) (c1 * tempValue + c2);
            b = 255;
        }
        else if (tempValue > -15 && tempValue <= -0) {
            double c1 = 15.;
            double c2 = 225.;
            r = (int) (c1 * tempValue + c2);
            g = 255;
            b = 255;
        }
        else if (tempValue >= 0 && tempValue < 15) {
            double c1 = -14.;
            double c2 = 210.0;
            r = 255;
            g = 255;
            b = (int) (c1 * tempValue + c2);
        }
        else if (tempValue >= 15 && tempValue < 30) {
            double c1 = -17.;
            double c2 = 510.;
            r = 255;
            g = (int) (c1 * tempValue + c2);
            b = 0;
        }
        if (tempValue >= 30) {
            r = 255;
            g = 0;
            b = 0;
        }
        return Color.rgb(r, g, b);
    }

    public static String getDescription(int code) {
        CondCode[] codes = CondCode.values();
        for (int i = 0; i < codes.length; i++)
            if (codes[i].code == code)
                return codes[i].name;
        return codes[0].name;
    }

    public static int getDay(String day) {
        Day[] days = Day.values();
        for (int i = 0; i < days.length; i++) {
            if (day.toUpperCase().equals(days[i].name()))
                return i + 1;
        }
        return 1;
    }

    public static String getDay(int day) {
        return Day.values()[day - 1].name();
    }

    public static boolean isRainCode(int code) {
        return code != CondCode.SELKEÄÄ.code && code != CondCode.PILVISTÄ.code && code != CondCode.PUOLIPILVISTÄ.code;
    }

    public static int dpToPx(int dp, Activity activity) {
        DisplayMetrics displayMetrics = activity.getResources().getDisplayMetrics();
        int px = Math.round(dp * (displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT));
        return px;
    }

    public static int getScaledFont(Activity activity) {
        DisplayMetrics displayMetrics = activity.getResources().getDisplayMetrics();
        return displayMetrics.widthPixels / 27;
    }

    public static int[] getEnnusteWidths(Activity activity) {
        DisplayMetrics displayMetrics = activity.getResources().getDisplayMetrics();
        int width = displayMetrics.widthPixels;
        float[] fraqs = new float[] { .10f, .18f };
        int[] widths = new int[fraqs.length];
        for (int i = 0; i < widths.length; i++) {
            widths[i] = Math.round(width * fraqs[i]);
        }
        return widths;
    }

    public static void recycle(BitmapDrawable[] drawables) {
        if (drawables == null)
            return;
        for (int i = 0; i < drawables.length; i++) {
            if (drawables[i] != null) {
                drawables[i].getBitmap().recycle();
                drawables[i] = null;
            }
        }
    }

    public static void recycle(Bitmap[] bmps) {
        if (bmps == null)
            return;
        for (int i = 0; i < bmps.length; i++) {
            if (bmps[i] != null) {
                bmps[i].recycle();
                bmps[i] = null;
            }
        }
    }

    public static void recycle(Bitmap[] bmps, int i) {
        if (bmps == null)
            return;
        if (bmps[i] != null) {
            bmps[i].recycle();
            bmps[i] = null;
        }
    }

    public static BitmapDrawable loadBitmapDrawableFromUrl(Activity activity, String imageAddress) {
        Bitmap bmp = loadBitmapFromUrl(imageAddress);
        return new BitmapDrawable(activity.getResources(), bmp);
    }

    public static Bitmap loadBitmapFromUrl(String imageAddress) {
        try {
            URL url = new URL(imageAddress);
            Object content = url.getContent();
            InputStream stream = (InputStream) content;
            // System.out.println("downloaded " + imageAddress);
            return BitmapFactory.decodeStream(stream);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (OutOfMemoryError e) {
            e.printStackTrace();
        }
        return null;
    }

    public static void showImage(Activity activity, View view, Bitmap bmp) {
        final Dialog imageDialog = new Dialog(activity);
        imageDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        imageDialog.setContentView(R.layout.showimage);
        imageDialog.setCancelable(true);

        ImageView imageView = (ImageView) imageDialog
                .findViewById(R.id.imageView);
        // Getting width & height of the given image.
        DisplayMetrics displayMetrics = activity.getResources().getDisplayMetrics();
        int wn = displayMetrics.widthPixels;
        int hn = displayMetrics.heightPixels;
        int wo = bmp.getWidth();
        int ho = bmp.getHeight();
        Matrix mtx = new Matrix();
        // Setting rotate to 90
        mtx.preRotate(90);
        // Setting resize
        mtx.postScale(((float) 1.3 * wn) / ho, ((float) 1.3 * hn) / wo);
        // Rotating Bitmap
        Bitmap rotatedBMP = Bitmap.createBitmap(bmp, 0, 0, wo, ho, mtx, true);
        BitmapDrawable bmd = new BitmapDrawable(rotatedBMP);

        imageView.setImageDrawable(bmd);

        imageView.setOnClickListener(new View.OnClickListener() {
            public void onClick(View button) {
                imageDialog.dismiss();
            }
        });

        imageDialog.show();
    }

    public static void setBackGroundDrawable(Activity activity, LinearLayout layout, Bitmap bmp) {
        // int sdk = android.os.Build.VERSION.SDK_INT;
        // if (sdk < android.os.Build.VERSION_CODES.JELLY_BEAN) {
        layout.setBackgroundDrawable(new BitmapDrawable(activity.getResources(), bmp));
        // } else {
        // layout.setBackground(new BitmapDrawable(activity.getResources(), bmp));
        // }
    }
}
