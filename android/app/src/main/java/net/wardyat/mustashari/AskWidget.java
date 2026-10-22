package net.wardyat.mustashari;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.TypedValue;
import android.view.View;
import android.webkit.CookieManager;
import android.widget.RemoteViews;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import org.json.JSONArray;
import org.json.JSONObject;

/** ودجت واحد يتناوب بذكاء بين ثلاث بطاقات:
 *  متابعة (العالِم نفسه الذي شكوتَ له يسأل عن آخر مشكلة، بعد يومين، حتى تفتحها) على الصندوق المزخرف،
 *  حكمة اليوم (مقتطف موثّق قصير مع مصدره) على الرقّ، ودعوة هادئة «اكتب همّك» من عالِم يتبدّل.
 *  لا إشعارات ولا صوت ولا عتاب. مسّ الصدر ينتقل للبطاقة التالية، ومسّ الصندوق يفتح التطبيق. */
public class AskWidget extends AppWidgetProvider {
    static final String NEXT = "net.wardyat.mustashari.WIDGET_NEXT";
    static final String API = "https://mustashari.wardyat.net/api/widget";
    static final long SLOT = 4L * 3600 * 1000;
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

    static int idxOf(String id) { for (int i = 0; i < IDS.length; i++) if (IDS[i].equals(id)) return i; return 0; }

    /** المستشار الداعي في بطاقة الدعوة: ثابت إن اختاره المستخدم، وإلا يتبدّل كل 4 ساعات. */
    static int inviteIndex(Context c) {
        int pin = sp(c).getInt("pin", -1);
        int shift = sp(c).getInt("shift", 0);
        if (pin >= 0) return Math.floorMod(pin + shift, IDS.length);
        return Math.floorMod((int) (System.currentTimeMillis() / SLOT) + shift, IDS.length);
    }

    static JSONObject data(Context c) {
        try { return new JSONObject(sp(c).getString("wdata", "{}")); } catch (Exception e) { return new JSONObject(); }
    }

    /** المتابعة المستحقّة: موجودة وغير مفتوحة سابقاً. */
    static JSONObject follow(Context c, JSONObject d) {
        JSONObject f = d.optJSONObject("follow");
        if (f == null) return null;
        String key = f.optString("session_id") + "|" + f.optString("at");
        return key.equals(sp(c).getString("done", "")) ? null : f;
    }

    static RemoteViews build(Context c) {
        JSONObject d = data(c);
        JSONObject f = follow(c, d);
        JSONArray qs = d.optJSONArray("quotes");
        boolean hasQ = qs != null && qs.length() > 0;
        // التناوب الذكي: عند وجود متابعة تتصدّر (ثلثا الفترات)، وإلا تتبادل الحكمة والدعوة
        int[] cycle = f != null ? (hasQ ? new int[]{0, 1, 0, 2} : new int[]{0, 2}) : (hasQ ? new int[]{1, 2} : new int[]{2});
        int slot = (int) (System.currentTimeMillis() / SLOT);
        int kind = cycle[Math.floorMod(slot + sp(c).getInt("card", 0), cycle.length)];

        String pid, label = "", text, path;
        boolean scroll = false;
        String done = null;
        if (kind == 0) {
            pid = f.optString("p");
            label = NAMES[idxOf(pid)] + " · " + f.optString("ago");
            String tp = f.optString("topic");   // الموضوع القصير يُذكر، والطويل يُختصر في «ما حدّثتني به» ليتسع السطر
            text = (tp.length() <= 18 && !tp.endsWith("…")) ? "ما جديد «" + tp + "»؟" : "ما جديد ما حدّثتني به؟";
            path = "chat.html?p=" + pid + "&s=" + f.optString("session_id");
            done = f.optString("session_id") + "|" + f.optString("at");
        } else if (kind == 1) {
            JSONObject q = qs.optJSONObject(Math.floorMod(d.optInt("day"), qs.length()));
            pid = q.optString("p");
            label = NAMES[idxOf(pid)] + (q.optBoolean("tr") ? " · ترجمة" : "") + " · " + q.optString("book");
            text = q.optString("text");
            path = "chat.html?p=" + pid;
            scroll = true;
        } else {
            int i = inviteIndex(c);
            pid = IDS[i];
            text = LINES[i][Math.floorMod(slot, LINES[i].length)];
            path = "chat.html?p=" + pid;
        }

        RemoteViews v = new RemoteViews(c.getPackageName(), R.layout.widget_ask);
        v.setImageViewResource(R.id.bust, c.getResources().getIdentifier("w_bust_" + pid, "drawable", c.getPackageName()));
        v.setInt(R.id.box, "setBackgroundResource", scroll ? R.drawable.w_scroll : R.drawable.w_ornate);
        v.setTextViewText(R.id.hint, text);
        v.setTextViewText(R.id.label, label);
        v.setTextViewTextSize(R.id.hint, TypedValue.COMPLEX_UNIT_SP, label.isEmpty() ? 16.5f : 15f);
        v.setViewVisibility(R.id.label, label.isEmpty() ? View.GONE : View.VISIBLE);

        Intent open = new Intent(c, MainActivity.class).putExtra("mpath", path).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        if (done != null) open.putExtra("fdone", done);
        PendingIntent po = PendingIntent.getActivity(c, 11, open, PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);
        v.setOnClickPendingIntent(R.id.box, po);
        v.setOnClickPendingIntent(R.id.coin, po);
        Intent next = new Intent(c, AskWidget.class).setAction(NEXT);
        v.setOnClickPendingIntent(R.id.bust, PendingIntent.getBroadcast(c, 12, next, PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT));
        return v;
    }

    /** للمعاينة في اختبار المحاكي فقط: يُستدعى بعد تبديل البطاقة. */
    static Runnable listener;

    static void updateAll(Context c) {
        AppWidgetManager m = AppWidgetManager.getInstance(c);
        for (int id : m.getAppWidgetIds(new ComponentName(c, AskWidget.class))) m.updateAppWidget(id, build(c));
    }

    /** يجلب المتابعة وحكمة اليوم من الخادم بكوكي الزائر نفسه في التطبيق؛ إن فشل بقي آخر ما خُزّن. */
    static void refresh(final Context c, final Runnable done) {
        new Thread(() -> {
            try {
                HttpURLConnection h = (HttpURLConnection) new URL(API).openConnection();
                h.setConnectTimeout(8000);
                h.setReadTimeout(8000);
                try {
                    String ck = CookieManager.getInstance().getCookie("https://mustashari.wardyat.net");
                    if (ck != null) h.setRequestProperty("Cookie", ck);
                } catch (Throwable ignored) {}
                if (h.getResponseCode() == 200) {
                    InputStream in = h.getInputStream();
                    ByteArrayOutputStream o = new ByteArrayOutputStream();
                    byte[] b = new byte[4096];
                    for (int n; (n = in.read(b)) > 0; ) o.write(b, 0, n);
                    String body = o.toString("UTF-8");
                    new JSONObject(body);   // تحقّق أنه JSON سليم قبل التخزين
                    sp(c).edit().putString("wdata", body).apply();
                }
            } catch (Exception ignored) {
            } finally {
                if (done != null) done.run();
            }
        }).start();
    }

    @Override public void onUpdate(Context c, AppWidgetManager m, int[] ids) {
        for (int id : ids) m.updateAppWidget(id, build(c));
        final PendingResult pr = goAsync();
        final Context app = c.getApplicationContext();
        refresh(app, () -> { updateAll(app); pr.finish(); });
    }

    @Override public void onReceive(Context c, Intent it) {
        if (NEXT.equals(it.getAction())) {
            sp(c).edit().putInt("card", sp(c).getInt("card", 0) + 1).putInt("shift", sp(c).getInt("shift", 0) + 1).apply();
            updateAll(c);
            if (listener != null) listener.run();
            return;
        }
        super.onReceive(c, it);
    }
}
