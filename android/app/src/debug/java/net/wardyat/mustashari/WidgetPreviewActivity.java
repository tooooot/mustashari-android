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
        // بيانات تجريبية تحاكي ردّ الخادم: متابعة مستحقّة + مقتطفات حقيقية من القائمة المتحقَّق منها
        AskWidget.sp(this).edit().putString("wdata", "{\"follow\":{\"p\":\"ibn_khaldun\",\"session_id\":7,\"topic\":\"هل أوسع مشروعي الآن أم…\",\"days\":3,\"ago\":\"قبل 3 أيام\",\"at\":\"2026-10-19T10:00:00+00:00\"},"
            + "\"quotes\":[{\"p\":\"ibn_al_qayyim\",\"text\":\"فإن الذنب كلما صغر في عين العبد عظم عند الله\",\"book\":\"الداء والدواء\",\"tr\":false},{\"p\":\"george_clason\",\"text\":\"ادفع لنفسك أولاً.\",\"book\":\"أغنى رجل في بابل\",\"tr\":true}],\"day\":20749}").putString("done", "").putInt("card", 0).apply();
        Runnable draw = () -> {
            root.removeAllViews();
            View v = AskWidget.build(this).apply(this, root);
            root.addView(v, new FrameLayout.LayoutParams((int) (320 * d), (int) (110 * d), Gravity.CENTER));
        };
        AskWidget.listener = () -> runOnUiThread(draw);
        draw.run();
        setContentView(root);
    }
}
