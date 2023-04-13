package fpt.edu.stafflink.components;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.Nullable;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import fpt.edu.stafflink.R;
import fpt.edu.stafflink.utilities.GenericUtils;

public class CustomSelectComponent<T> extends LinearLayout {
    Spinner customSelectComponentMainElement;
    TextView customSelectComponentError;
    T selectedOption;
    private List<T> options;
    private String mainField;
    private boolean nullable;
    private CharSequence nullValue;
    private CharSequence error;
    private boolean enabled;

    public CustomSelectComponent(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);

        View view = inflate(context, R.layout.component_select_custom, this);
        customSelectComponentMainElement = view.findViewById(R.id.customSelectComponentMainElement);
        customSelectComponentError = view.findViewById(R.id.customSelectComponentError);

        this.setAttributes(attrs);
    }

    public void setOptions(List<T> options) {
        if (this.nullable) {
            this.options = new ArrayList<>();
            this.options.add(null);
            this.options.addAll(options);
        } else {
            this.options = options;
        }
        this.initiateSpinner();
    }

    public List<T> getOptions() {
        return this.options;
    }

    public void setMainField(String mainField) {
        this.mainField = mainField;
        this.initiateSpinner();
    }

    public String getMainField() {
        return this.mainField;
    }

    public void setData(List<T> options, String mainField) {
        this.mainField = mainField;
        this.setOptions(options);
    }

    public void setSelectedOption(T selectedOption) {
        this.selectedOption = selectedOption;
        int position = GenericUtils.getIndexOf(selectedOption, this.options);
        if (position != -1) {
            customSelectComponentMainElement.setSelection(position);
        } else if (this.nullable) {
            customSelectComponentMainElement.setSelection(0);
        }
    }

    public T getSelectedOption() {
        return this.selectedOption;
    }

    public void setNullable(boolean nullable) {
        this.nullable = nullable;
        if (this.options != null && this.options.size() > 0 && this.options.get(0) != null) {
            this.options.add(0, null);
            this.initiateSpinner();
        }
    }

    public boolean isNullable() {
        return this.nullable;
    }

    public void setNullValue(CharSequence nullValue) {
        this.nullValue = nullValue;
        this.initiateSpinner();
    }

    public CharSequence getNullValue() {
        return this.nullValue;
    }

    public void setError(CharSequence error) {
        this.error = error;
        customSelectComponentError.setText(error);
    }

    public CharSequence getError() {
        return error;
    }

    public void setEditable(boolean enabled) {
        this.enabled = enabled;
        customSelectComponentMainElement.setEnabled(enabled);
    }

    public boolean isEditable() {
        return this.enabled;
    }

    private void initiateSpinner() {
        if (StringUtils.isEmpty(this.mainField) || this.options == null || this.options.size() == 0) {
            return;
        }
        List<String> optionValues = options.stream()
                .map(option -> {
                    if (option == null) {
                        return this.nullValue.toString();
                    } else {
                        return GenericUtils.getFieldValue(option, this.mainField);
                    }
                })
                .collect(Collectors.toList());

        ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, optionValues);
        customSelectComponentMainElement.setAdapter(adapter);
        customSelectComponentMainElement.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                selectedOption = options.get(i);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
    }

    private void setAttributes(AttributeSet attrs) {
        TypedArray typedArray = getContext().obtainStyledAttributes(attrs, R.styleable.CustomSelectComponent, 0, 0);
        try {
            boolean nullable = typedArray.getBoolean(R.styleable.CustomSelectComponent_nullable, false);
            this.setNullable(nullable);

            if (typedArray.hasValue(R.styleable.CustomSelectComponent_nullValue)) {
                this.setNullValue(typedArray.getText(R.styleable.CustomSelectComponent_nullValue));
            }

            boolean enabled = typedArray.getBoolean(R.styleable.CustomSelectComponent_android_enabled, true);
            this.setEditable(enabled);
        } finally {
            typedArray.recycle();
        }
    }
}
