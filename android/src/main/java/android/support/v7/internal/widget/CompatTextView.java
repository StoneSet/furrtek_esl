package android.support.v7.internal.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Rect;
import android.support.v7.appcompat.R;
import android.text.method.TransformationMethod;
import android.util.AttributeSet;
import android.view.View;
import android.widget.TextView;
import java.util.Locale;

public class CompatTextView extends TextView {

    private static class AllCapsTransformationMethod implements TransformationMethod {
        private final Locale mLocale;

        public AllCapsTransformationMethod(Context context) {
            this.mLocale = context.getResources().getConfiguration().locale;
        }

        public CharSequence getTransformation(CharSequence charSequence, View view) {
            return charSequence != null ? charSequence.toString().toUpperCase(this.mLocale) : null;
        }

        public void onFocusChanged(View view, CharSequence charSequence, boolean z, int i, Rect rect) {
        }
    }

    public CompatTextView(Context context) {
        this(context, null);
    }

    public CompatTextView(Context context, AttributeSet attributeSet) {
        this(context, attributeSet, 0);
    }

    public CompatTextView(Context context, AttributeSet attributeSet, int i) {
        super(context, attributeSet, i);
        TypedArray obtainStyledAttributes = context.obtainStyledAttributes(attributeSet, R.styleable.CompatTextView, i, 0);
        boolean z = obtainStyledAttributes.getBoolean(0, false);
        obtainStyledAttributes.recycle();
        if (z) {
            setTransformationMethod(new AllCapsTransformationMethod(context));
        }
    }
}
