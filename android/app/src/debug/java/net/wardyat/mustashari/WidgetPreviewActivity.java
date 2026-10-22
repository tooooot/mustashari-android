package net.wardyat.mustashari;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;

/** للاختبار على المحاكي فقط (نسخة debug): يعرض الودجت كما سيظهر بعرض 4×2 تقريباً. */
public class WidgetPreviewActivity extends Activity {
    @Override protected void onCreate(Bundle b) {
        super.onCreate(b);
        FrameLayout root = new FrameLayout(this);
        root.setBackgroundColor(Color.parseColor("#33414E"));
        float d = getResources().getDisplayMetrics().density;
        View v = AskWidget.build(this).apply(this, root);
        FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams((int) (320 * d), (int) (128 * d), Gravity.CENTER);
        root.addView(v, lp);
        setContentView(root);
    }
}
