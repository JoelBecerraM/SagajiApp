package mx.com.sagaji.android;

import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import com.atcloud.android.util.Message;

/**
 * Created by jbecerra.
 */
public class ConfiguracionFragment extends PreferenceFragment {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.preferences);

        EditTextPreference filialPreference = (EditTextPreference) getPreferenceScreen().findPreference("filial");
        filialPreference.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                boolean matched = newValue.toString().matches("\\d{2}");
                if (!matched) {
                    Message.precaucion(getActivity(), "El número de filial es inválido, el formato esperado es solo números "
                            + " compuesto de dos dígitos.");
                }
                return matched;
            }
        });

        EditTextPreference intermediarioPreference = (EditTextPreference) getPreferenceScreen().findPreference("intermediario");
        intermediarioPreference.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                /*boolean matched = newValue.toString().matches("\\[A-Z]{3}[0-9]{2}");
                if (!matched) {
                    Message.precaucion(getActivity(), "El número de intermediario es inválido, el formato esperado es tres letras "
                            + " y dos dígitos.");
                }
                return matched;*/
                return true;
            }
        });
    }
}