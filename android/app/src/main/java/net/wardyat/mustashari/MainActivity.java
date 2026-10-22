package net.wardyat.mustashari;

import android.content.Intent;
import android.os.Bundle;
import com.getcapacitor.BridgeActivity;

public class MainActivity extends BridgeActivity {
    @Override public void onCreate(Bundle b) {
        super.onCreate(b);
        openFromWidget(getIntent());
    }

    @Override protected void onNewIntent(Intent it) {
        super.onNewIntent(it);
        openFromWidget(it);
    }

    /** الودجت يرسل مساراً داخل الموقع (chat.html?p=…) فنفتحه. */
    private void openFromWidget(Intent it) {
        String path = it == null ? null : it.getStringExtra("mpath");
        if (path == null || getBridge() == null) return;
        it.removeExtra("mpath");
        getBridge().getWebView().post(() -> getBridge().getWebView().loadUrl("https://mustashari.wardyat.net/" + path));
    }
}
