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
 *  يتبدّل المستشار كل 4 ساعات، ولمسُ الوجه يبدّله فوراً، ولمسُ الصندوق يفتح حواراً جديداً معه.
 *  لا إشعارات ولا صوت. الجمل دعوة فقط، بلا عتاب ولا «اشتقنا». */
public class AskWidget extends AppWidgetProvider {
    static final String NEXT = "net.wardyat.mustashari.WIDGET_NEXT";
    static final String[] IDS = {"ibn_al_qayyim", "ibn_sina", "ibn_taymiyyah", "ibn_khaldun", "salah_al_din", "isaac_newton", "george_clason", "eugen_sandow"};
    static final String[] NAMES = {"ابن القيم", "ابن سينا", "ابن تيمية", "ابن خلدون", "صلاح الدين", "نيوتن", "كلاسون", "ساندو"};
    static final String[][] LINES = {
        {"ما الذي يثقل قلبك اليوم؟ حدّثني به.", "إن ضاق صدرك فبُح لي بما فيه.", "قل لي همّك، فالهمّ إذا قيل خفّ."},
        {"كيف حال جسدك ونومك هذه الأيام؟", "أخبرني بما تشكو منه، ونتدبّره معاً.", "الجسد أمانة. ما الذي يتعبك؟"},
        {"أمرٌ التبس عليك؟ اعرضه عليّ.", "إن حيّرك أمر في دينك أو دنياك فاسأل.", "ما الذي تريد أن تفهمه على وجهه؟"},
        {"في عملك أو مجتمعك أمرٌ تفكر فيه؟", "حدّثني عمّا يشغلك من شأن الناس والعمل.", "أي قرارٍ تقف أمامه اليوم؟"},
        {"موقفٌ يحتاج حزماً وحكمة؟ أخبرني.", "هل أنت أمام قرار صعب؟ تشاور معي.", "ما التحدي الذي تواجهه الآن؟"},
        {"مسألة تحتاج تفكيراً هادئاً؟ هاتها.", "ما السؤال الذي يشغل عقلك؟", "لنفكّر معاً في ما يحيّرك."},
        {"أمر مالك يقلقك؟ حدّثني به.", "كيف تدبّر مالك وعملك هذه الأيام؟", "هل تفكر في بداية أو ادخار؟ أخبرني."},
        {"كيف حال همّتك وقوتك اليوم؟", "تريد أن تنشط وتبني عادة؟ اسألني.", "ما الذي يمنعك من الحركة؟ قل لي."}
    };

    static SharedPreferences sp(Context c) { return c.getSharedPreferences("ask_widget", Context.MODE_PRIVATE); }

    static int index(Context c) {
        int slot = (int) (System.currentTimeMillis() / (4L * 3600 * 1000));
        return Math.floorMod(slot + sp(c).getInt("shift", 0), IDS.length);
    }

    static RemoteViews build(Context c) {
        int i = index(c);
        String[] ls = LINES[i];
        int day = (int) (System.currentTimeMillis() / (4L * 3600 * 1000));
        RemoteViews v = new RemoteViews(c.getPackageName(), R.layout.widget_ask);
        v.setImageViewResource(R.id.face, c.getResources().getIdentifier("w_face_" + IDS[i], "drawable", c.getPackageName()));
        v.setTextViewText(R.id.name, NAMES[i] + " · مستشار بالذكاء الاصطناعي");
        v.setTextViewText(R.id.line, ls[Math.floorMod(day, ls.length)]);
        Intent open = new Intent(c, MainActivity.class).putExtra("mpath", "chat.html?p=" + IDS[i]).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent po = PendingIntent.getActivity(c, 11, open, PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);
        v.setOnClickPendingIntent(R.id.box, po);
        v.setOnClickPendingIntent(R.id.line, po);
        Intent next = new Intent(c, AskWidget.class).setAction(NEXT);
        v.setOnClickPendingIntent(R.id.face, PendingIntent.getBroadcast(c, 12, next, PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT));
        return v;
    }

    @Override public void onUpdate(Context c, AppWidgetManager m, int[] ids) {
        for (int id : ids) m.updateAppWidget(id, build(c));
    }

    @Override public void onReceive(Context c, Intent it) {
        if (NEXT.equals(it.getAction())) {
            sp(c).edit().putInt("shift", sp(c).getInt("shift", 0) + 1).apply();
            AppWidgetManager m = AppWidgetManager.getInstance(c);
            onUpdate(c, m, m.getAppWidgetIds(new ComponentName(c, AskWidget.class)));
            return;
        }
        super.onReceive(c, it);
    }
}
