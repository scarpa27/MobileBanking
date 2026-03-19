package hr.cizmic.seebanking.activities;

import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

// base activity for all screens (handles system bars + double-tap back
public abstract class BaseActivity extends AppCompatActivity {

    // 2 sec window for double back press
    private static final long BACK_PRESS_INTERVAL_MS = 2000L;

    private long lastBackPressedAt = 0L;
    private Toast backToast;

    // setup view and apply padding for system bars
    @Override
    public void setContentView(int layoutResID) {
        super.setContentView(layoutResID);
        View root = findRootView();
        if (root != null) {
            applySystemBarInsets(root);
        }
    }

    @Override
    public void setContentView(View view) {
        super.setContentView(view);
        if (view != null) {
            applySystemBarInsets(view);
        }
    }

    @Override
    public void setContentView(View view, ViewGroup.LayoutParams params) {
        super.setContentView(view, params);
        if (view != null) {
            applySystemBarInsets(view);
        }
    }

    // double-tap back to exit (prevent accidental exits)
    @Override
    public void onBackPressed() {
        long now = System.currentTimeMillis();
        // 2nd press within 2 sec exits
        if (now - lastBackPressedAt < BACK_PRESS_INTERVAL_MS) {
            if (backToast != null) {
                backToast.cancel();
            }
            super.onBackPressed();
            return;
        }

        // 1st press shows warning
        lastBackPressedAt = now;
        if (backToast != null) {
            backToast.cancel();
        }
        backToast = Toast.makeText(this, "Press back again to exit", Toast.LENGTH_SHORT);
        backToast.show();
    }

    // get root view from content
    private View findRootView() {
        View content = findViewById(android.R.id.content);
        if (content instanceof ViewGroup) {
            ViewGroup contentGroup = (ViewGroup) content;
            if (contentGroup.getChildCount() > 0) {
                return contentGroup.getChildAt(0);
            }
        }
        return content;
    }

    // add padding for status/nav bar so content isn't hidden
    private void applySystemBarInsets(View root) {
        // save original padding
        final int initialLeft = root.getPaddingLeft();
        final int initialTop = root.getPaddingTop();
        final int initialRight = root.getPaddingRight();
        final int initialBottom = root.getPaddingBottom();

        // apply system bar padding
        ViewCompat.setOnApplyWindowInsetsListener(root, (view, insets) -> {
            Insets bars = insets.getInsets(
                    WindowInsetsCompat.Type.systemBars() | WindowInsetsCompat.Type.displayCutout()
            );
            // add system bar insets to original padding
            view.setPadding(
                    initialLeft,
                    initialTop + bars.top,
                    initialRight,
                    initialBottom + bars.bottom
            );
            return insets;
        });

        ViewCompat.requestApplyInsets(root);
    }
}
