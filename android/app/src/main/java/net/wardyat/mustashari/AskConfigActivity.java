package net.wardyat.mustashari;

import android.app.Activity;
import android.appwidget.AppWidgetManager;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

/** يظهر عند إضافة الودجت وعند الضغط المطوّل ثم «إعادة الضبط»: اختيار مستشار ثابت أو التبديل التلقائي. */
public class AskConfigActivity extends Activity {
    @Override protected void onCreate(Bundle b) {
        super.onCreate(b);
        setResult(RESULT_CANCELED);
        final int wid = getIntent().getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);
        float d = getResources().getDisplayMetrics().density;
        LinearLayout col = new LinearLayout(this);
        col.setOrientation(LinearLayout.VERTICAL);
        col.setLayoutDirection(View.LAYOUT_DIRECTION_RTL);
        col.setBackgroundColor(Color.parseColor("#F3E6C8"));
        col.setPadding((int) (16 * d), (int) (16 * d), (int) (16 * d), (int) (16 * d));
        TextView t = new TextView(this);
        t.setText("من يظهر في الودجت؟");
        t.setTextColor(Color.parseColor("#7A1F14"));
        t.setTextSize(18);
        t.setTypeface(null, android.graphics.Typeface.BOLD);
        col.addView(t);
        col.addView(row(d, null, "تبديل تلقائي بين المستشارين", -1, wid));
        for (int i = 0; i < AskWidget.IDS.length; i++) col.addView(row(d, AskWidget.IDS[i], AskWidget.NAMES[i], i, wid));
        ScrollView sv = new ScrollView(this);
        sv.addView(col);
        setContentView(sv);
    }

    private View row(float d, String id, String name, final int pin, final int wid) {
        LinearLayout r = new LinearLayout(this);
        r.setOrientation(LinearLayout.HORIZONTAL);
        r.setGravity(Gravity.CENTER_VERTICAL);
        r.setLayoutDirection(View.LAYOUT_DIRECTION_RTL);
        r.setPadding(0, (int) (8 * d), 0, (int) (8 * d));
        ImageView im = new ImageView(this);
        if (id != null) im.setImageResource(getResources().getIdentifier("w_bust_" + id, "drawable", getPackageName()));
        r.addView(im, new LinearLayout.LayoutParams((int) (48 * d), (int) (46 * d)));
        TextView n = new TextView(this);
        n.setText(name);
        n.setTextColor(Color.parseColor("#2B1A0C"));
        n.setTextSize(16);
        n.setPadding((int) (12 * d), 0, 0, 0);
        r.addView(n);
        r.setOnClickListener(v -> {
            AskWidget.sp(this).edit().putInt("pin", pin).putInt("shift", 0).apply();
            AppWidgetManager m = AppWidgetManager.getInstance(this);
            if (wid != AppWidgetManager.INVALID_APPWIDGET_ID) m.updateAppWidget(wid, AskWidget.build(this));
            setResult(RESULT_OK, new Intent().putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, wid));
            finish();
        });
        return r;
    }
}
