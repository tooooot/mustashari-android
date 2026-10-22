package net.wardyat.mustashari;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.widget.RemoteViews;

/** ودجت «اكتب همّك»: وجه مستشار وجملة دعوة هادئة وصندوق بتصميم صندوق المحادثة.
 *  يتبدّل المستشار كل 4 ساعات، ولمسُ صدر المستشار يبدّله فوراً، ولمسُ الصندوق يفتح حواراً جديداً معه.
 *  لا إشعارات ولا صوت. الجمل دعوة فقط، بلا عتاب ولا «اشتقنا». */
public class AskWidget extends AppWidgetProvider {
    static final String NEXT = "net.wardyat.mustashari.WIDGET_NEXT";
    static final String[] IDS = {"ibn_al_qayyim", "ibn_sina", "ibn_taymiyyah", "ibn_khaldun", "salah_al_din", "isaac_newton", "george_clason", "eugen_sandow"};
    static final String[] NAMES = {"ابن القيم", "ابن سينا", "ابن تيمية", "ابن خلدون", "صلاح الدين", "نيوتن", "كلاسون", "ساندو"};
    static final String[][] LINES = {
        {"ما الذي يثقل قلبك اليوم؟", "بُح لي بما في صدرك.", "قل همّك، فالهمّ إذا قيل خفّ."},
        {"كيف جسدك ونومك هذه الأيام؟", "ما الذي يتعبك؟ أخبرني.", "الجسد أمانة، حدّثني عنه."},
        {"أمرٌ التبس عليك؟", "ما الذي تريد أن تفهمه؟", "سلني فيما حيّرك."},
        {"ما الذي يشغلك في عملك؟", "قرارٌ تقف أمامه اليوم؟", "حدّثني عن شأنك مع الناس."},
        {"قرارٌ صعب؟ تشاور معي.", "ما التحدي الذي تواجهه؟", "أمرٌ يحتاج حزماً؟ أخبرني."},
        {"ما السؤال الذي يشغل عقلك؟", "مسألة تحتاج هدوء تفكير؟", "لنفكّر معاً فيما يحيّرك."},
        {"أمرٌ في مالك يقلقك؟", "كيف تدبّر مالك هذه الأيام؟", "تفكّر في ادخار؟ حدّثني."},
        {"كيف همّتك وقوتك اليوم؟", "تريد أن تبني عادة؟ اسألني.", "ما الذي يمنعك من الحركة؟"}
    };

    static SharedPreferences sp(Context c) { return c.getSharedPreferences("ask_widget", Context.MODE_PRIVATE); }

    static int index(Context c) {
        int pin = sp(c).getInt("pin", -1);
        if (pin >= 0) return Math.floorMod(pin + sp(c).getInt("shift", 0), IDS.length);   // ثابت: اللمس ينتقل منه للتالي
        int slot = (int) (System.currentTimeMillis() / (4L * 3600 * 1000));
        return Math.floorMod(slot + sp(c).getInt("shift", 0), IDS.length);
    }

    static RemoteViews build(Context c) {
        int i = index(c);
        String[] ls = LINES[i];
        int slot = (int) (System.currentTimeMillis() / (4L * 3600 * 1000));
        RemoteViews v = new RemoteViews(c.getPackageName(), R.layout.widget_ask);
        v.setImageViewResource(R.id.bust, c.getResources().getIdentifier("w_bust_" + IDS[i], "drawable", c.getPackageName()));
        v.setTextViewText(R.id.hint, ls[Math.floorMod(slot, ls.length)]);
        Intent open = new Intent(c, MainActivity.class).putExtra("mpath", "chat.html?p=" + IDS[i]).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent po = PendingIntent.getActivity(c, 11, open, PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);
        v.setOnClickPendingIntent(R.id.box, po);
        v.setOnClickPendingIntent(R.id.coin, po);
        Intent next = new Intent(c, AskWidget.class).setAction(NEXT);
        v.setOnClickPendingIntent(R.id.bust, PendingIntent.getBroadcast(c, 12, next, PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT));
        return v;
    }

    /** للمعاينة في اختبار المحاكي فقط: يُستدعى بعد تبديل المستشار. */
    static Runnable listener;

    @Override public void onUpdate(Context c, AppWidgetManager m, int[] ids) {
        for (int id : ids) m.updateAppWidget(id, build(c));
    }

    @Override public void onReceive(Context c, Intent it) {
        if (NEXT.equals(it.getAction())) {
            sp(c).edit().putInt("shift", sp(c).getInt("shift", 0) + 1).apply();
            AppWidgetManager m = AppWidgetManager.getInstance(c);
            onUpdate(c, m, m.getAppWidgetIds(new ComponentName(c, AskWidget.class)));
            if (listener != null) listener.run();
            return;
        }
        super.onReceive(c, it);
    }
}
