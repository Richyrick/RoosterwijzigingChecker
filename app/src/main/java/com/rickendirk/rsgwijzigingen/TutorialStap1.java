/*
 *    This file is part of RSG-Wijzigingen.
 *
 *     RSG-Wijzigingen is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     RSG-Wijzigingen is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with RSG-Wijzigingen.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.rickendirk.rsgwijzigingen;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.CheckBox;
import android.widget.EditText;


import org.codepond.wizardroid.WizardStep;
import org.codepond.wizardroid.persistence.ContextVariable;

public class TutorialStap1 extends WizardStep{
    /**
     * Tell WizarDroid that these are context variables.
     * These values will be automatically bound to any field annotated with {@link ContextVariable}.
     * NOTE: Context Variable names are unique and therefore must
     * have the same name wherever you wish to use them.
     */
    private boolean clusters;
    @ContextVariable
    private String klas;


    EditText klasET;
    CheckBox clustersCB;

    //You must have an empty constructor for every step
    public TutorialStap1() {
    }

    //Set your layout here
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.tutorial_stap1, container, false);
        //Get reference to the textboxes
        final EditText klasET = (EditText) v.findViewById(R.id.editTextKlas);

        klasET.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int start, int before, int i2) {
                notifyCompleted();
                //TODO: Goede oplossing vinden hiervoor
                InputMethodManager imm = (InputMethodManager) getActivity().
                        getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.showSoftInput(klasET, InputMethodManager.SHOW_IMPLICIT);

                if (charSequence.length() > 0){
                    char char1 = charSequence.charAt(0);
                    boolean isLetter = Character.isLetter(char1);
                    if (isLetter){
                        klasET.setError("1e teken moet een cijfer zijn");
                    }
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        //and set default values by using Context Variables
        //klasET.setText(firstname);
        //lastnameEt.setText(lastname);

        return v;
    }

    /**
     * Called whenever the wizard proceeds to the next step or goes back to the previous step
     */

    @Override
    public void onExit(int exitCode) {
        switch (exitCode) {
            case WizardStep.EXIT_NEXT:
                //bindDataFields();
                saveDataFields();
                openSettings();
                break;
            case WizardStep.EXIT_PREVIOUS:
                //Do nothing...
                break;
        }
    }

    private void openSettings() {
        CheckBox checkBox = (CheckBox) getView().findViewById(R.id.autoZoekenCB);
        boolean moetNaarSettings = checkBox.isChecked();
        if (moetNaarSettings) {
            Intent settingsIntent = new Intent(getActivity(), SettingsActivity.class);
            startActivityForResult(settingsIntent, 1874);
        }
    }

    public void saveDataFields(){
        View v = getView();
        EditText klasET = (EditText) v.findViewById(R.id.editTextKlas);
        CheckBox clustersCB = (CheckBox) v.findViewById(R.id.checkBoxClusters);

        String klas = klasET.getText().toString();
        boolean clusters = clustersCB.isChecked();

        SharedPreferences.Editor spEditor = PreferenceManager
                .getDefaultSharedPreferences(getActivity()).edit();
        spEditor.putString("pref_klas", klas);
        spEditor.putBoolean("pref_cluster_enabled", clusters);
        spEditor.commit();
    }

    //Onderstaande code (nog) niet nodig, laten staan voor geval dat
/*
private void bindDataFields() {
//Do some work
//...
//The values of these fields will be automatically stored in the wizard context
//and will be populated in the next steps only if the same field names are used.
firstname = firstnameEt.getText().toString();
lastname = lastnameEt.getText().toString();
}
*/
}
