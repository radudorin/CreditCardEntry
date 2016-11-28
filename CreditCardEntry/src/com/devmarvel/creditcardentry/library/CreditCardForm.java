package com.devmarvel.creditcardentry.library;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.os.Build;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.v4.os.ParcelableCompat;
import android.support.v4.os.ParcelableCompatCreatorCallbacks;
import android.util.AttributeSet;
import android.util.SparseArray;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.devmarvel.creditcardentry.R;
import com.devmarvel.creditcardentry.fields.CreditEntryFieldBase;
import com.devmarvel.creditcardentry.internal.CreditCardEntry;

public class CreditCardForm extends RelativeLayout {

    private CreditCardEntry entry;
    private boolean includeHelper;
    private int textHelperColor;
    private boolean useDefaultColors;
    private boolean animateOnError;
    private String cardNumberHint = "1234 5678 9012 3456";
    private CreditCardFormListener mListener;

    public CreditCardForm(Context context) {
        this(context, null);
    }

    public CreditCardForm(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CreditCardForm(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        if (!isInEditMode()) {

            // If the attributes are available, use them to color the icon
            if (attrs != null) {

                TypedArray typedArray = null;
                try {
                    typedArray = context.getTheme().obtainStyledAttributes(
                            attrs,
                            R.styleable.CreditCardForm,
                            0,
                            0
                    );

                    this.cardNumberHint = typedArray.getString(R.styleable.CreditCardForm_card_number_hint);
                    this.includeHelper = typedArray.getBoolean(R.styleable.CreditCardForm_include_helper, true);
                    this.textHelperColor = typedArray.getColor(R.styleable.CreditCardForm_helper_text_color, getResources().getColor(R.color.text_helper_color));
                    this.useDefaultColors = typedArray.getBoolean(R.styleable.CreditCardForm_default_text_colors, false);
                    this.animateOnError = typedArray.getBoolean(R.styleable.CreditCardForm_animate_on_error, true);
                } finally {
                    if (typedArray != null) typedArray.recycle();
                }
            }

            // defaults if not set by user
            if (cardNumberHint == null) cardNumberHint = "1234 5678 9012 3456";
        }

        init(context, attrs, defStyle);
    }

    public void setListener(CreditCardFormListener listener) {
        mListener = listener;
    }

    private void init(Context context, AttributeSet attrs, int style) {
        // the wrapper layout
        LinearLayout layout;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            layout = new LinearLayout(context);
        } else {
            layout = new LinearLayout(context);
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            //ignore RTL layout direction
            layout.setLayoutDirection(LAYOUT_DIRECTION_LTR);
        }

        Resources r = getResources();

        layout.setId(R.id.cc_form_layout);
        LayoutParams params = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        params.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
        params.addRule(RelativeLayout.ALIGN_PARENT_TOP);
        params.addRule(LinearLayout.HORIZONTAL);
        params.setMargins(0, 0, 0, 0);
        layout.setLayoutParams(params);
        layout.setPadding(0, 0, 0, 0);

        // set up the card image container and images
        FrameLayout cardImageFrame = new FrameLayout(context);
        LinearLayout.LayoutParams frameParams = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        frameParams.gravity = Gravity.CENTER_VERTICAL;
        cardImageFrame.setLayoutParams(frameParams);
        cardImageFrame.setFocusable(true);
        cardImageFrame.setFocusableInTouchMode(true);
        cardImageFrame.setPadding(10, 0, 0, 0);

        int width = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 44, r.getDisplayMetrics());
        int height = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 30, r.getDisplayMetrics());


        ImageView cardFrontImage = new ImageView(context);
        LayoutParams layoutParams = new LayoutParams(width, height);
        cardFrontImage.setLayoutParams(layoutParams);
        cardFrontImage.setImageResource(CardType.INVALID.frontResource);
        cardImageFrame.addView(cardFrontImage);

        ImageView cardBackImage = new ImageView(context);
        layoutParams = new RelativeLayout.LayoutParams(width, height);
        cardBackImage.setLayoutParams(layoutParams);
        cardBackImage.setImageResource(CardType.INVALID.backResource);
        cardBackImage.setVisibility(View.GONE);
        cardImageFrame.addView(cardBackImage);
        layout.addView(cardImageFrame);

        // add the data entry form
        int margin = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 10, r.getDisplayMetrics());
        LinearLayout.LayoutParams entryParams = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        entryParams.gravity = Gravity.CENTER_VERTICAL;
        entryParams.setMargins(margin, 0, 0, 0);
        entry = new CreditCardEntry(context, attrs, style);
        entry.setId(R.id.cc_entry);

        // this obnoxious 6 for bottom padding is to make the damn text centered on the image... if you know a better way... PLEASE HELP
        entry.setPadding(0, 0, 0, 6);
        entry.setLayoutParams(entryParams);

        // set any passed in attrs
        entry.setCardImageView(cardFrontImage);
        entry.setBackCardImage(cardBackImage);
        entry.setCardNumberHint(cardNumberHint);

        entry.setAnimateOnError(animateOnError);

        this.addView(layout);

        // set up optional helper text view
        if (includeHelper) {
            TextView textHelp = new TextView(context);
            textHelp.setId(R.id.text_helper);
            textHelp.setText(getResources().getString(R.string.CreditCardNumberHelp));
            if (useDefaultColors) {
                textHelp.setTextColor(this.textHelperColor);
            }
            layoutParams = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
            layoutParams.addRule(RelativeLayout.BELOW, layout.getId());
            layoutParams.addRule(RelativeLayout.CENTER_HORIZONTAL);
            layoutParams.setMargins(0, 15, 0, 20);
            textHelp.setLayoutParams(layoutParams);
            entry.setTextHelper(textHelp);
            this.addView(textHelp);
        }

        layout.addView(entry);
    }

    public void setOnCardValidCallback(CardValidCallback callback) {
        entry.setOnCardValidCallback(callback);
    }

    public CreditEntryFieldBase getCreditEntryFieldBase() {
        return entry.getCreditEntryFieldBase();
    }

    /**
     * all internal components will be attached this same focus listener
     */
    @Override
    public void setOnFocusChangeListener(OnFocusChangeListener l) {
        entry.setOnFocusChangeListener(l);
    }

    @Override
    public OnFocusChangeListener getOnFocusChangeListener() {
        return entry.getOnFocusChangeListener();
    }

    @SuppressWarnings("unused")
    public boolean isCreditCardValid() {
        return entry.isCreditCardValid();
    }

    @SuppressWarnings("unused")
    public CreditCard getCreditCard() {
        return entry.getCreditCard();
    }

    /**
     * request focus for the credit card field
     */
    @SuppressWarnings("unused")
    public void focusCreditCard() {
        entry.focusCreditCard();
    }

    /**
     * clear and reset the entire form
     */
    @SuppressWarnings("unused")
    public void clearForm() {
        entry.clearAll();
    }

    /**
     * @param cardNumber     the card number to show
     * @param focusNextField true to go to next field (only works if the number is valid)
     */
    public void setCardNumber(String cardNumber, boolean focusNextField) {
        entry.setCardNumber(cardNumber, focusNextField);
    }

    @Override
    protected void dispatchSaveInstanceState(@NonNull SparseArray<Parcelable> container) {
        dispatchFreezeSelfOnly(container);
    }

    @Override
    protected void dispatchRestoreInstanceState(@NonNull SparseArray<Parcelable> container) {
        dispatchThawSelfOnly(container);
    }

    @Override
    public void onRestoreInstanceState(Parcelable state) {
        SavedState ss = (SavedState) state;
        super.onRestoreInstanceState(ss.getSuperState());
        for (int i = 0; i < getChildCount(); i++) {
            getChildAt(i).restoreHierarchyState(ss.childrenStates);
        }
    }

    @Override
    protected Parcelable onSaveInstanceState() {
        Parcelable superState = super.onSaveInstanceState();
        SavedState ss = new SavedState(superState);
        ss.childrenStates = new SparseArray();
        for (int i = 0; i < getChildCount(); i++) {
            getChildAt(i).saveHierarchyState(ss.childrenStates);
        }
        return ss;
    }

    static class SavedState extends BaseSavedState {
        SparseArray childrenStates;

        SavedState(Parcelable superState) {
            super(superState);
        }

        private SavedState(Parcel in, ClassLoader classLoader) {
            super(in);
            childrenStates = in.readSparseArray(classLoader);
        }

        @Override
        public void writeToParcel(Parcel out, int flags) {
            super.writeToParcel(out, flags);
            out.writeSparseArray(childrenStates);
        }

        public static final Creator<SavedState> CREATOR
                = ParcelableCompat.newCreator(new ParcelableCompatCreatorCallbacks<SavedState>() {
            @Override
            public SavedState createFromParcel(Parcel in, ClassLoader loader) {
                return new SavedState(in, loader);
            }

            @Override
            public SavedState[] newArray(int size) {
                return new SavedState[size];
            }
        });
    }

    public interface CreditCardFormListener {
        void onCreditCardEntered();
    }

}

